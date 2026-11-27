package com.example.data

import com.example.game.GameMode
import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val dao: GameRecordDao) {
    val careerStats: Flow<CareerStats> = dao.getCareerStats()

    fun bestScoreFlow(mode: GameMode): Flow<Int> = dao.getBestScoreByMode(mode.storageKey)

    fun topRecordsFlow(mode: GameMode): Flow<List<GameRecordEntity>> =
        dao.getTopRecordsByMode(mode.storageKey)

    suspend fun saveRecord(
        mode: GameMode,
        score: Int,
        snakeLength: Int,
        candiesEaten: Int,
        powerUpsUsed: Int,
        durationSeconds: Int,
    ) {
        if (score <= 0 && candiesEaten <= 0) return
        dao.insertRecord(
            GameRecordEntity(
                mode = mode.storageKey,
                score = score,
                snakeLength = snakeLength,
                candiesEaten = candiesEaten,
                powerUpsUsed = powerUpsUsed,
                durationSeconds = durationSeconds,
            )
        )
    }

    suspend fun clearAllRecords() {
        dao.clearAll()
    }
}
