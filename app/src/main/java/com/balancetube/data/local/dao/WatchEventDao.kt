package com.balancetube.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.balancetube.data.local.entity.WatchEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchEvent(event: WatchEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchEvents(events: List<WatchEventEntity>)

    @Query("""
        SELECT we.* FROM watch_events we
        WHERE we.watchedAt >= :startTime
        ORDER BY we.watchedAt DESC
    """)
    suspend fun getWatchEventsAfter(startTime: Long): List<WatchEventEntity>

    @Query("""
        SELECT v.* FROM videos v
        INNER JOIN watch_events we ON v.videoId = we.videoId
        WHERE we.watchedAt >= :startTime
        ORDER BY we.watchedAt DESC
    """)
    suspend fun getWatchedVideosAfter(startTime: Long): List<com.balancetube.data.local.entity.VideoEntity>

    @Query("DELETE FROM watch_events")
    suspend fun deleteAllWatchEvents()

    @Query("SELECT COUNT(*) FROM watch_events")
    fun getWatchEventCount(): Flow<Int>
}
