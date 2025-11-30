package com.example.nutrisiku.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Merepresentasikan satu item makanan spesifik yang disimpan dalam satu entri riwayat.
 *
 * @property name Nama makanan yang ditampilkan.
 * @property portion Ukuran porsi dalam gram.
 * @property calories Jumlah kalori untuk porsi tersebut.
 * @property quantity Jumlah item makanan ini (misal, 2 potong Tahu Goreng).
 */
data class HistoryFoodItem(
    val name: String,
    val portion: Int,
    val calories: Int,
    val quantity: Int
)

/**
 * Entitas Room yang merepresentasikan satu baris dalam tabel riwayat (`history_table`).
 * Setiap entri mewakili satu sesi makan yang disimpan oleh pengguna.
 *
 * @property id Kunci utama (primary key) yang dibuat secara otomatis oleh Room.
 * @property timestamp Waktu saat entri ini dibuat, dalam format milidetik (Unix time).
 * @property sessionLabel Label sesi makan (misal, "Sarapan", "Makan Siang").
 * @property imagePath Path absolut ke file gambar yang disimpan di penyimpanan internal aplikasi.
 * @property totalCalories Jumlah total kalori dari semua item makanan dalam entri ini.
 * @property foodItems Daftar objek [HistoryFoodItem] yang berisi rincian setiap makanan.
 */
@Entity(tableName = "history_table")
@TypeConverters(FoodItemConverter::class)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val sessionLabel: String,
    val imagePath: String,
    val totalCalories: Int,
    val foodItems: List<HistoryFoodItem>
)

/**
 * [TypeConverter] untuk Room agar dapat menyimpan dan membaca tipe data `List<HistoryFoodItem>`.
 * Room secara default hanya bisa menyimpan tipe data primitif. Konverter ini mengubah
 * daftar objek menjadi String JSON saat menyimpan, dan mengubah String JSON kembali
 * menjadi daftar objek saat membaca dari database.
 */
class FoodItemConverter {
    /**
     * Mengonversi `List<HistoryFoodItem>` menjadi representasi String JSON.
     */
    @TypeConverter
    fun fromFoodItemList(foodItems: List<HistoryFoodItem>): String {
        return Gson().toJson(foodItems)
    }

    /**
     * Mengonversi String JSON kembali menjadi `List<HistoryFoodItem>`.
     */
    @TypeConverter
    fun toFoodItemList(jsonString: String): List<HistoryFoodItem> {
        val listType = object : TypeToken<List<HistoryFoodItem>>() {}.type
        return Gson().fromJson(jsonString, listType)
    }
}
