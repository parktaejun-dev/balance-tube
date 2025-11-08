package com.balancetube.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String? = null,
    val durationSeconds: Int? = null,
    val publishedAt: Long? = null,
    val category: String? = null // Knowledge, Entertainment, Lifestyle, etc.
)
