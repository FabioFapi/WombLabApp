package com.rix.womblab.di

import android.content.Context
import androidx.room.Room
import com.rix.womblab.data.local.dao.EventDao
import com.rix.womblab.data.local.dao.FavoriteDao
import com.rix.womblab.data.local.database.WombLabDatabase
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
    fun provideWombLabDatabase(
        @ApplicationContext context: Context
    ): WombLabDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            WombLabDatabase::class.java,
            WombLabDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideEventDao(database: WombLabDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    fun provideFavoriteDao(database: WombLabDatabase): FavoriteDao {
        return database.favoriteDao()
    }
}