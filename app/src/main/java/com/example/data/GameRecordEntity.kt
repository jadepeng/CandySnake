package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val mode: String,
    val score: Int,
    val snakeLength: Int,
    val candiesEaten: Int,
    val powerUpsUsed: Int,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis(),
)

data class CareerStats(
    val totalGames: Int = 0,
    val bestScore: Int = 0,
    val maxLength: Int = 0,
    val totalCandies: Int = 0,
)
