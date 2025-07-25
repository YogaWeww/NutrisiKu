package com.example.nutrisiku.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    // PERUBAHAN: Fungsi untuk mengupdate entri
    @Update
    suspend fun updateHistory(history: HistoryEntity)

    // PERUBAHAN: Fungsi untuk menghapus entri
    @Delete
    suspend fun deleteHistory(history: HistoryEntity)

    // PERUBAHAN: Fungsi untuk mendapatkan total kalori hari ini
    @Query("SELECT SUM(totalCalories) FROM history_table WHERE timestamp >= :startOfDay")
    fun getTodaysTotalCalories(startOfDay: Long): Flow<Int?>
}