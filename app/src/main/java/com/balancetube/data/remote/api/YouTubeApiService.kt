package com.balancetube.data.remote.api

import com.balancetube.data.remote.model.ChannelResponse
import com.balancetube.data.remote.model.PlaylistItemsResponse
import com.balancetube.data.remote.model.SearchResponse
import com.balancetube.data.remote.model.VideosResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    /**
     * Get the user's channel information including watch history playlist ID
     */
    @GET("youtube/v3/channels")
    suspend fun getMyChannel(
        @Query("part") part: String = "contentDetails",
        @Query("mine") mine: Boolean = true
    ): ChannelResponse

    /**
     * Get playlist items (watch history)
     */
    @GET("youtube/v3/playlistItems")
    suspend fun getPlaylistItems(
        @Query("playlistId") playlistId: String,
        @Query("part") part: String = "snippet,contentDetails",
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null
    ): PlaylistItemsResponse

    /**
     * Get video details including duration
     */
    @GET("youtube/v3/videos")
    suspend fun getVideos(
        @Query("part") part: String = "contentDetails,snippet,statistics",
        @Query("id") videoIds: String // Comma-separated video IDs
    ): VideosResponse

    /**
     * Search for videos
     */
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 50,
        @Query("order") order: String = "viewCount",
        @Query("videoDuration") videoDuration: String = "medium", // 4-20 minutes
        @Query("pageToken") pageToken: String? = null
    ): SearchResponse
}
