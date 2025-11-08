package com.balancetube.data.repository

import com.balancetube.data.local.dao.VideoDao
import com.balancetube.data.local.dao.WatchEventDao
import com.balancetube.data.local.entity.VideoEntity
import com.balancetube.data.local.entity.WatchEventEntity
import com.balancetube.data.remote.api.YouTubeApiService
import com.balancetube.domain.model.BalanceReport
import com.balancetube.domain.model.Category
import com.balancetube.domain.model.CategoryScore
import com.balancetube.domain.model.Period
import com.balancetube.domain.model.Video
import com.balancetube.util.AuthManager
import com.balancetube.util.CategoryClassifier
import com.balancetube.util.DurationParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BalanceTubeRepository @Inject constructor(
    private val youtubeApi: YouTubeApiService,
    private val videoDao: VideoDao,
    private val watchEventDao: WatchEventDao,
    private val authManager: AuthManager
) {

    /**
     * Sync watch history from YouTube API
     */
    suspend fun syncWatchHistory(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Get watch history playlist ID
            val channelResponse = youtubeApi.getMyChannel()
            val watchHistoryId = channelResponse.items.firstOrNull()
                ?.contentDetails?.relatedPlaylists?.watchHistory
                ?: return@withContext Result.failure(Exception("Watch history not available"))

            var pageToken: String? = null
            val allVideoIds = mutableSetOf<String>()

            // Fetch all watch history items
            do {
                val playlistResponse = youtubeApi.getPlaylistItems(
                    playlistId = watchHistoryId,
                    pageToken = pageToken
                )

                playlistResponse.items.forEach { item ->
                    val videoId = item.contentDetails.videoId
                    allVideoIds.add(videoId)

                    // Save watch event
                    val publishedAt = parseTimestamp(item.snippet.publishedAt)
                    watchEventDao.insertWatchEvent(
                        WatchEventEntity(
                            videoId = videoId,
                            watchedAt = publishedAt
                        )
                    )
                }

                pageToken = playlistResponse.nextPageToken
            } while (pageToken != null && allVideoIds.size < 200) // Limit to 200 most recent

            // Fetch video details in batches
            allVideoIds.chunked(50).forEach { videoIdBatch ->
                val videosResponse = youtubeApi.getVideos(
                    videoIds = videoIdBatch.joinToString(",")
                )

                val videoEntities = videosResponse.items.map { videoItem ->
                    val snippet = videoItem.snippet
                    val duration = DurationParser.parseIso8601Duration(
                        videoItem.contentDetails.duration
                    )

                    val category = if (snippet != null) {
                        CategoryClassifier.classify(snippet.title, snippet.channelTitle)
                    } else {
                        Category.ENTERTAINMENT
                    }

                    VideoEntity(
                        videoId = videoItem.id,
                        title = snippet?.title ?: "",
                        channelTitle = snippet?.channelTitle ?: "",
                        thumbnailUrl = snippet?.thumbnails?.medium?.url
                            ?: snippet?.thumbnails?.high?.url,
                        durationSeconds = duration,
                        publishedAt = snippet?.publishedAt?.let { parseTimestamp(it) },
                        category = category.name
                    )
                }

                videoDao.insertVideos(videoEntities)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get balance report for a specific period
     */
    suspend fun getBalanceReport(period: Period): Result<BalanceReport> = withContext(Dispatchers.IO) {
        try {
            val startTime = Instant.now()
                .minus(period.getDaysCount().toLong(), ChronoUnit.DAYS)
                .toEpochMilli()

            val watchedVideos = watchEventDao.getWatchedVideosAfter(startTime)

            // Calculate category scores
            val categoryDurations = mutableMapOf<Category, Int>()
            Category.values().forEach { categoryDurations[it] = 0 }

            watchedVideos.forEach { video ->
                val category = Category.fromString(video.category)
                val duration = video.durationSeconds ?: 0
                categoryDurations[category] = categoryDurations[category]!! + duration
            }

            // Find max score for normalization
            val maxDuration = categoryDurations.values.maxOrNull() ?: 1

            // Create category scores
            val categoryScores = categoryDurations.map { (category, duration) ->
                CategoryScore(
                    category = category,
                    rawScore = duration,
                    normalizedScore = if (maxDuration > 0) {
                        (duration.toFloat() / maxDuration) * 100f
                    } else {
                        0f
                    }
                )
            }

            // Find lowest category
            val lowestCategory = categoryScores
                .minByOrNull { it.normalizedScore }?.category
                ?: Category.KNOWLEDGE

            val report = BalanceReport(
                categoryScores = categoryScores,
                lowestCategory = lowestCategory,
                period = period
            )

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get video recommendations for a specific category
     */
    suspend fun getRecommendations(category: Category): Result<List<Video>> = withContext(Dispatchers.IO) {
        try {
            val keywords = CategoryClassifier.getSearchKeywords(category)
            val allVideos = mutableListOf<Video>()

            // Search with different keywords
            keywords.take(2).forEach { keyword ->
                val searchResponse = youtubeApi.searchVideos(
                    query = "$keyword beginner",
                    maxResults = 30
                )

                val videoIds = searchResponse.items.mapNotNull { it.id.videoId }
                if (videoIds.isEmpty()) return@forEach

                // Get video details
                val videosResponse = youtubeApi.getVideos(
                    videoIds = videoIds.joinToString(",")
                )

                videosResponse.items.forEach { videoItem ->
                    val snippet = videoItem.snippet ?: return@forEach
                    val duration = DurationParser.parseIso8601Duration(
                        videoItem.contentDetails.duration
                    )

                    // Filter: 2-14 minutes (120-840 seconds)
                    if (duration in 120..840) {
                        allVideos.add(
                            Video(
                                videoId = videoItem.id,
                                title = snippet.title,
                                channelTitle = snippet.channelTitle,
                                thumbnailUrl = snippet.thumbnails.medium?.url
                                    ?: snippet.thumbnails.high?.url,
                                durationSeconds = duration,
                                publishedAt = parseTimestamp(snippet.publishedAt),
                                category = category
                            )
                        )
                    }
                }
            }

            // Sort by view count (already sorted by API order=viewCount) and recency
            // Take top 5
            val recommendations = allVideos
                .sortedByDescending { it.publishedAt ?: 0 }
                .take(5)

            Result.success(recommendations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete all local data
     */
    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        videoDao.deleteAllVideos()
        watchEventDao.deleteAllWatchEvents()
    }

    private fun parseTimestamp(timestamp: String): Long {
        return try {
            Instant.parse(timestamp).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
