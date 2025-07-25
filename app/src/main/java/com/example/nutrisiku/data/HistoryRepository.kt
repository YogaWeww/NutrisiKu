package com.example.nutrisiku.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

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
}

