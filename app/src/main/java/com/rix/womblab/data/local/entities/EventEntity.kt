package com.rix.womblab.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.rix.womblab.data.local.database.Converters
import java.time.LocalDateTime

@Entity(tableName = "events")
@TypeConverters(Converters::class)
data class EventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val excerpt: String,
    val url: String,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val allDay: Boolean,
    val timezone: String,
    val cost: String,
    val website: String?,
    val venueName: String?,
    val venueAddress: String?,
    val venueCity: String?,
    val organizerName: String?,
    val organizerEmail: String?,
    val categories: List<String>, // JSON serialized
    val featured: Boolean,
    val status: String,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)