package com.balancetube.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.balancetube.data.local.dao.VideoDao
import com.balancetube.data.local.dao.WatchEventDao
import com.balancetube.data.local.entity.VideoEntity
import com.balancetube.data.local.entity.WatchEventEntity

@Database(
    entities = [
        VideoEntity::class,
        WatchEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BalanceTubeDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun watchEventDao(): WatchEventDao
}
