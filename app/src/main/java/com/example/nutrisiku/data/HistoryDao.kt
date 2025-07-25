package com.example.nutrisiku.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("SELECT * FROM history_table ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    // PERUBAHAN: Fungsi baru untuk mendapatkan satu item
    @Query("SELECT * FROM history_table WHERE id = :id")
    fun getHistoryById(id: Int): Flow<HistoryEntity?>
}