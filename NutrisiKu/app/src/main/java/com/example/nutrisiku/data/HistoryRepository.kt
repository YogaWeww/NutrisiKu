package com.example.nutrisiku.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository untuk mengelola operasi data yang terkait dengan riwayat deteksi.
 * Bertindak sebagai perantara antara ViewModel dan sumber data (HistoryDao).
 *
 * Pola Repository ini mengisolasi sumber data dari sisa aplikasi dan menyediakan
 * API yang bersih untuk akses data.
 *
 * @property historyDao Data Access Object untuk tabel riwayat. Disuntikkan melalui constructor
 * untuk memfasilitasi pengujian dan arsitektur yang bersih.
 */
class HistoryRepository(private val historyDao: HistoryDao) {

    /**
     * Sebuah [Flow] yang memancarkan daftar semua entri riwayat setiap kali ada perubahan.
     */
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    /**
     * Menyisipkan satu entri riwayat ke dalam database.
     * Ini adalah fungsi suspend dan harus dipanggil dari coroutine.
     *
     * @param history Entitas riwayat yang akan disisipkan.
     */
    suspend fun insert(history: HistoryEntity) {
        historyDao.insertHistory(history)
    }

    /**
     * Mengambil satu entri riwayat berdasarkan ID-nya.
     *
     * @param id ID dari entri riwayat yang dicari.
     * @return [Flow] yang berisi [HistoryEntity] yang cocok, atau null jika tidak ditemukan.
     */
    fun getHistoryById(id: Int): Flow<HistoryEntity?> {
        return historyDao.getHistoryById(id)
    }

    /**
     * Memperbarui entri riwayat yang sudah ada.
     *
     * @param history Entitas riwayat dengan data yang sudah diperbarui.
     */
    suspend fun update(history: HistoryEntity) {
        historyDao.updateHistory(history)
    }

    /**
     * Menghapus satu entri riwayat dari database.
     *
     * @param history Entitas riwayat yang akan dihapus.
     */
    suspend fun delete(history: HistoryEntity) {
        historyDao.deleteHistory(history)
    }

    /**
     * Mengambil total kalori yang dikonsumsi hari ini.
     *
     * @return [Flow] yang berisi jumlah total kalori sebagai [Int], atau null jika tidak ada.
     */
    fun getTodaysCalories(): Flow<Int?> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        return historyDao.getTodaysTotalCalories(startOfDay)
    }
}
