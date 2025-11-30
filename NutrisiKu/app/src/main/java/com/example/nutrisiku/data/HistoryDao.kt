package com.example.nutrisiku.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) untuk entitas [HistoryEntity].
 * Menyediakan metode untuk berinteraksi dengan tabel riwayat di database Room.
 */
@Dao
interface HistoryDao {

    /**
     * Menyisipkan atau mengganti satu entri riwayat ke dalam database.
     * Jika entri dengan primary key yang sama sudah ada, entri tersebut akan diganti.
     *
     * @param history Entitas riwayat yang akan disisipkan.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    /**
     * Mengambil semua entri riwayat dari database, diurutkan berdasarkan timestamp
     * terbaru (paling baru di atas).
     *
     * @return [Flow] yang berisi daftar [HistoryEntity]. Flow akan otomatis
     * memancarkan data baru setiap kali ada perubahan pada tabel.
     */
    @Query("SELECT * FROM history_table ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    /**
     * Mengambil satu entri riwayat berdasarkan ID uniknya.
     *
     * @param id Primary key dari entri riwayat yang dicari.
     * @return [Flow] yang berisi [HistoryEntity] yang cocok, atau null jika tidak ditemukan.
     */
    @Query("SELECT * FROM history_table WHERE id = :id")
    fun getHistoryById(id: Int): Flow<HistoryEntity?>

    /**
     * Memperbarui entri riwayat yang sudah ada di database.
     *
     * @param history Entitas riwayat dengan data yang sudah diperbarui.
     */
    @Update
    suspend fun updateHistory(history: HistoryEntity)

    /**
     * Menghapus satu entri riwayat dari database.
     *
     * @param history Entitas riwayat yang akan dihapus.
     */
    @Delete
    suspend fun deleteHistory(history: HistoryEntity)

    /**
     * Menghitung total kalori dari semua entri riwayat yang dibuat hari ini.
     *
     * @param startOfDay Timestamp (dalam milidetik) yang menandakan awal hari (pukul 00:00).
     * @return [Flow] yang berisi jumlah total kalori sebagai [Int], atau null jika tidak ada entri hari ini.
     */
    @Query("SELECT SUM(totalCalories) FROM history_table WHERE timestamp >= :startOfDay")
    fun getTodaysTotalCalories(startOfDay: Long): Flow<Int?>
}
