package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GameRecordEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CandySnakeDatabase : RoomDatabase() {
    abstract fun gameRecordDao(): GameRecordDao

    companion object {
        @Volatile
        private var instance: CandySnakeDatabase? = null

        fun getInstance(context: Context): CandySnakeDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CandySnakeDatabase::class.java,
                    "candy_snake_local.db",
                ).build().also { instance = it }
            }
        }
    }
}
