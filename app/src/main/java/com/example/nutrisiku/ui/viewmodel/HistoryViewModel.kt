package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.HistoryEntity
import com.example.nutrisiku.data.HistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    application: Application,
    private val historyRepository: HistoryRepository
) : AndroidViewModel(application) {

    // Expose Flow dari Repository sebagai StateFlow agar UI bisa mengamatinya.
    // Setiap kali ada data baru yang disimpan ke database,
    // StateFlow ini akan otomatis emit nilai baru dan UI akan diperbarui.
    val historyList: StateFlow<List<HistoryEntity>> = historyRepository.allHistory
        .stateIn(
            scope = viewModelScope,
            // Mulai mengoleksi saat UI terlihat, berhenti 5 detik setelah UI tidak terlihat.
            started = SharingStarted.WhileSubscribed(5000L),
            // Nilai awal sebelum data pertama dari database dimuat.
            initialValue = emptyList()
        )

    // PERUBAHAN: StateFlow baru untuk total kalori hari ini
    val todaysCalories: StateFlow<Int> = historyRepository.getTodaysCalories()
        .map { it ?: 0 } // Jika null (tidak ada data), anggap 0
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0
        )
}
