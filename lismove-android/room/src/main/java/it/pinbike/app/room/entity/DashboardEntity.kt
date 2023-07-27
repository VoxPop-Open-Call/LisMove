package it.lismove.app.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DashboardEntity (
    @PrimaryKey
    val userId: String,
    val co2: Double = 0.0,
    val distance: Double = 0.0,
    val euro: Double = 0.0,
    val sessionNumber: Int = 0,
    val messages: Int = 0,
    val nationalPoints: Int = 0,
    val sessionDistanceAvg: Double = 0.0
)

@Entity
data class DashboardUserDistanceStatsEntity(
    val userId: String,
    @PrimaryKey
    val day: String,
    val distance: Double
)