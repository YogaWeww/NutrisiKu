package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.HistoryEntity
import com.example.nutrisiku.data.HistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn

class HistoryDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle // Untuk menerima argumen dari navigasi
) : AndroidViewModel(application) {

    private val historyRepository = HistoryRepository(application)

    // Ambil historyId dari argumen navigasi
    private val historyId: Int = checkNotNull(savedStateHandle["historyId"])

    // Ambil data detail dari repository berdasarkan ID
    val historyDetail: StateFlow<HistoryEntity> = historyRepository.getHistoryById(historyId)
        .filterNotNull() // Pastikan data tidak null
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            // Nilai awal sebelum data dimuat
            initialValue = HistoryEntity(0, 0, "", "", 0, emptyList())
        )
}