package com.balancetube.domain.model

data class Video(
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String? = null,
    val durationSeconds: Int? = null,
    val publishedAt: Long? = null,
    val category: Category = Category.ENTERTAINMENT
)
