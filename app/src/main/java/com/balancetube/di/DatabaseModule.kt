package com.balancetube.di

import android.content.Context
import androidx.room.Room
import com.balancetube.data.local.dao.VideoDao
import com.balancetube.data.local.dao.WatchEventDao
import com.balancetube.data.local.database.BalanceTubeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBalanceTubeDatabase(
        @ApplicationContext context: Context
    ): BalanceTubeDatabase {
        return Room.databaseBuilder(
            context,
            BalanceTubeDatabase::class.java,
            "balance_tube_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideVideoDao(database: BalanceTubeDatabase): VideoDao {
        return database.videoDao()
    }

    @Provides
    fun provideWatchEventDao(database: BalanceTubeDatabase): WatchEventDao {
        return database.watchEventDao()
    }
}
