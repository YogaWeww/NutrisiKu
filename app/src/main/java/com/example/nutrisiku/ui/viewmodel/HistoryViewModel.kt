package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.HistoryEntity
import com.example.nutrisiku.data.HistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    // Inisialisasi HistoryRepository
    private val historyRepository = HistoryRepository(application)

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

    // TODO: Tambahkan fungsi untuk menghapus item riwayat
}
