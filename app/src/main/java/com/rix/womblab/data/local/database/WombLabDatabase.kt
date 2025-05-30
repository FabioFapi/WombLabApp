package com.rix.womblab.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.rix.womblab.data.local.dao.EventDao
import com.rix.womblab.data.local.dao.FavoriteDao
import com.rix.womblab.data.local.dao.NotificationDao
import com.rix.womblab.data.local.entities.EventEntity
import com.rix.womblab.data.local.entities.FavoriteEntity
import com.rix.womblab.data.local.entities.NotificationEntity

@Database(
    entities = [
        EventEntity::class,
        FavoriteEntity::class,
        NotificationEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WombLabDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        const val DATABASE_NAME = "womblab_database"

        @Volatile
        private var INSTANCE: WombLabDatabase? = null

        fun getDatabase(context: Context): WombLabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WombLabDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}