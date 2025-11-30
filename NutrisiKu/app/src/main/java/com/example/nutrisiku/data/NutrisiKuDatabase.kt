package com.example.nutrisiku.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Kelas database utama untuk aplikasi NutrisiKu menggunakan Room.
 *
 * @property entities Daftar semua kelas entitas yang akan menjadi tabel di database.
 * @property version Versi database. Harus dinaikkan setiap kali ada perubahan skema.
 * @property exportSchema Disarankan untuk di-set `false` untuk proyek sederhana agar tidak
 * menghasilkan file skema JSON saat kompilasi.
 */
@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
@TypeConverters(FoodItemConverter::class)
abstract class NutrisiKuDatabase : RoomDatabase() {

    /**
     * Menyediakan akses ke Data Access Object (DAO) untuk tabel riwayat.
     * Room akan mengimplementasikan fungsi abstrak ini secara otomatis.
     */
    abstract fun historyDao(): HistoryDao

    /**
     * Companion object untuk memungkinkan akses ke metode pembuatan database tanpa
     * perlu membuat instance dari kelas `NutrisiKuDatabase`. Ini adalah inti dari
     * pola Singleton.
     */
    companion object {
        /**
         * Anotasi `@Volatile` memastikan bahwa nilai dari variabel INSTANCE akan selalu
         * up-to-date dan sama untuk semua thread eksekusi. Artinya, perubahan yang dibuat
         * oleh satu thread pada INSTANCE akan langsung terlihat oleh semua thread lain.
         */
        @Volatile
        private var INSTANCE: NutrisiKuDatabase? = null

        /**
         * Mendapatkan instance Singleton dari database.
         *
         * Metode ini menggunakan pola Singleton untuk memastikan hanya ada satu instance
         * database yang dibuat di seluruh aplikasi. Ini penting untuk mencegah race conditions
         * dan masalah performa.
         *
         * @param context Konteks aplikasi.
         * @return Instance tunggal dari [NutrisiKuDatabase].
         */
        fun getDatabase(context: Context): NutrisiKuDatabase {
            // Jika INSTANCE tidak null, kembalikan instance yang sudah ada.
            // Jika null, buat database baru di dalam blok synchronized.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutrisiKuDatabase::class.java,
                    "nutrisiku_database"
                ).build()
                INSTANCE = instance
                // Kembalikan instance yang baru dibuat
                instance
            }
        }
    }
}
