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

/**
 * ViewModel untuk mengelola dan menyediakan data riwayat ke UI.
 * Bertanggung jawab untuk mengekspos daftar riwayat dan total kalori harian.
 *
 * @param application Konteks aplikasi.
 * @param historyRepository Repository untuk mendapatkan data riwayat dari database.
 */
class HistoryViewModel(
    application: Application,
    historyRepository: HistoryRepository
) : AndroidViewModel(application) {

    /**
     * Mengekspos daftar semua entri riwayat sebagai StateFlow.
     * StateFlow adalah holder data yang dapat diamati (observable) yang memancarkan
     * pembaruan state saat ini dan yang baru.
     *
     * `stateIn` mengubah Flow biasa dari repository menjadi StateFlow yang efisien,
     * yang cocok untuk diamati oleh UI.
     */
    val historyList: StateFlow<List<HistoryEntity>> = historyRepository.allHistory
        .stateIn(
            scope = viewModelScope,
            // SharingStarted.WhileSubscribed(5000L): Flow akan mulai aktif saat ada
            // pengamat (UI) yang terhubung, dan akan berhenti 5 detik setelah pengamat
            // terakhir terputus. Ini menghemat sumber daya baterai dan CPU.
            started = SharingStarted.WhileSubscribed(5000L),
            // Nilai awal yang ditampilkan sebelum data pertama dari database dimuat.
            initialValue = emptyList()
        )

    /**
     * Mengekspos total kalori yang dikonsumsi hari ini sebagai StateFlow.
     * Flow dari repository dapat mengembalikan null jika tidak ada data,
     * jadi kita menggunakan `map` untuk mengubah null menjadi 0 agar UI selalu menerima nilai yang aman.
     */
    val todaysCalories: StateFlow<Int> = historyRepository.getTodaysCalories()
        .map { it ?: 0 } // Jika hasilnya null (tidak ada entri hari ini), kembalikan 0.
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0
        )
}
