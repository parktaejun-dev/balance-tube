package com.balancetube.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Channel Response
@JsonClass(generateAdapter = true)
data class ChannelResponse(
    @Json(name = "items")
    val items: List<ChannelItem>
)

@JsonClass(generateAdapter = true)
data class ChannelItem(
    @Json(name = "contentDetails")
    val contentDetails: ChannelContentDetails
)

@JsonClass(generateAdapter = true)
data class ChannelContentDetails(
    @Json(name = "relatedPlaylists")
    val relatedPlaylists: RelatedPlaylists
)

@JsonClass(generateAdapter = true)
data class RelatedPlaylists(
    @Json(name = "watchHistory")
    val watchHistory: String? = null,
    @Json(name = "likes")
    val likes: String? = null
)

// Playlist Items Response
@JsonClass(generateAdapter = true)
data class PlaylistItemsResponse(
    @Json(name = "items")
    val items: List<PlaylistItem>,
    @Json(name = "nextPageToken")
    val nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class PlaylistItem(
    @Json(name = "snippet")
    val snippet: PlaylistItemSnippet,
    @Json(name = "contentDetails")
    val contentDetails: PlaylistItemContentDetails
)

@JsonClass(generateAdapter = true)
data class PlaylistItemSnippet(
    @Json(name = "title")
    val title: String,
    @Json(name = "channelTitle")
    val channelTitle: String,
    @Json(name = "thumbnails")
    val thumbnails: Thumbnails,
    @Json(name = "publishedAt")
    val publishedAt: String,
    @Json(name = "resourceId")
    val resourceId: ResourceId
)

@JsonClass(generateAdapter = true)
data class PlaylistItemContentDetails(
    @Json(name = "videoId")
    val videoId: String,
    @Json(name = "videoPublishedAt")
    val videoPublishedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ResourceId(
    @Json(name = "videoId")
    val videoId: String
)

@JsonClass(generateAdapter = true)
data class Thumbnails(
    @Json(name = "default")
    val default: Thumbnail? = null,
    @Json(name = "medium")
    val medium: Thumbnail? = null,
    @Json(name = "high")
    val high: Thumbnail? = null
)

@JsonClass(generateAdapter = true)
data class Thumbnail(
    @Json(name = "url")
    val url: String
)

// Videos Response (for getting duration)
@JsonClass(generateAdapter = true)
data class VideosResponse(
    @Json(name = "items")
    val items: List<VideoItem>
)

@JsonClass(generateAdapter = true)
data class VideoItem(
    @Json(name = "id")
    val id: String,
    @Json(name = "contentDetails")
    val contentDetails: VideoContentDetails,
    @Json(name = "snippet")
    val snippet: VideoSnippet? = null,
    @Json(name = "statistics")
    val statistics: VideoStatistics? = null
)

@JsonClass(generateAdapter = true)
data class VideoContentDetails(
    @Json(name = "duration")
    val duration: String // ISO 8601 format: PT1H2M10S
)

@JsonClass(generateAdapter = true)
data class VideoSnippet(
    @Json(name = "title")
    val title: String,
    @Json(name = "channelTitle")
    val channelTitle: String,
    @Json(name = "thumbnails")
    val thumbnails: Thumbnails,
    @Json(name = "publishedAt")
    val publishedAt: String
)

@JsonClass(generateAdapter = true)
data class VideoStatistics(
    @Json(name = "viewCount")
    val viewCount: String? = null
)

// Search Response
@JsonClass(generateAdapter = true)
data class SearchResponse(
    @Json(name = "items")
    val items: List<SearchItem>,
    @Json(name = "nextPageToken")
    val nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class SearchItem(
    @Json(name = "id")
    val id: SearchId,
    @Json(name = "snippet")
    val snippet: SearchSnippet
)

@JsonClass(generateAdapter = true)
data class SearchId(
    @Json(name = "videoId")
    val videoId: String? = null
)

@JsonClass(generateAdapter = true)
data class SearchSnippet(
    @Json(name = "title")
    val title: String,
    @Json(name = "channelTitle")
    val channelTitle: String,
    @Json(name = "thumbnails")
    val thumbnails: Thumbnails,
    @Json(name = "publishedAt")
    val publishedAt: String
)
