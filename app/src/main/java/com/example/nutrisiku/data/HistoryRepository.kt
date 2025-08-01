package com.example.nutrisiku.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class HistoryRepository(context: Context) {
    private val historyDao: HistoryDao

    init {
        val database = NutrisiKuDatabase.getDatabase(context)
        historyDao = database.historyDao()
    }

    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    suspend fun insert(history: HistoryEntity) {
        historyDao.insertHistory(history)
    }

    // PERUBAHAN: Fungsi baru untuk mendapatkan satu item
    fun getHistoryById(id: Int): Flow<HistoryEntity?> {
        return historyDao.getHistoryById(id)
    }

    // PERUBAHAN: Fungsi baru untuk update
    suspend fun update(history: HistoryEntity) {
        historyDao.updateHistory(history)
    }

    // PERUBAHAN: Fungsi baru untuk menghapus
    suspend fun delete(history: HistoryEntity) {
        historyDao.deleteHistory(history)
    }

    // PERUBAHAN: Fungsi baru untuk mendapatkan kalori hari ini
    fun getTodaysCalories(): Flow<Int?> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        return historyDao.getTodaysTotalCalories(startOfDay)
    }

}
