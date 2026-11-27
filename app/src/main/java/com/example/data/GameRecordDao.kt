package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GameRecordEntity)

    @Query("SELECT COALESCE(MAX(score), 0) FROM game_records WHERE mode = :mode")
    fun getBestScoreByMode(mode: String): Flow<Int>

    @Query("SELECT * FROM game_records WHERE mode = :mode ORDER BY score DESC, timestamp DESC LIMIT 20")
    fun getTopRecordsByMode(mode: String): Flow<List<GameRecordEntity>>

    @Query(
        """
        SELECT
            COUNT(*) AS totalGames,
            COALESCE(MAX(score), 0) AS bestScore,
            COALESCE(MAX(snakeLength), 0) AS maxLength,
            COALESCE(SUM(candiesEaten), 0) AS totalCandies
        FROM game_records
        """
    )
    fun getCareerStats(): Flow<CareerStats>

    @Query("DELETE FROM game_records")
    suspend fun clearAll()
}
