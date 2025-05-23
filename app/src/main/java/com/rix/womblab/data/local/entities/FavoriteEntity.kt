package com.rix.womblab.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val eventId: String,
    val userId: String,
    val addedAt: LocalDateTime = LocalDateTime.now()
)