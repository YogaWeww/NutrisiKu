package com.example.nutrisiku.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

/**
 * Data class yang merepresentasikan informasi nutrisi untuk satu jenis makanan
 * dari file calorie_lookup.json.
 *
 * @property label Kunci unik yang cocok dengan output label dari model TFLite.
 * @property nama_tampilan Nama makanan yang lebih ramah pengguna untuk ditampilkan di UI.
 * @property kalori_per_100g Jumlah kalori per 100 gram.
 * @property porsi_standar_g Ukuran porsi standar dalam gram.
 */
data class FoodNutrition(
    val label: String,
    val nama_tampilan: String,
    val kalori_per_100g: Int,
    val porsi_standar_g: Int
)

/**
 * Repository untuk mengelola data nutrisi makanan.
 * Bertanggung jawab untuk memuat dan menyediakan informasi nutrisi dari file JSON lokal.
 *
 * @param context Konteks aplikasi, digunakan untuk mengakses folder assets.
 */
class NutritionRepository(private val context: Context) {

    /**
     * Properti publik yang menyimpan data nutrisi dalam bentuk Map untuk pencarian cepat.
     *
     * Menggunakan `by lazy` untuk optimisasi: file JSON hanya akan dibaca dan di-parse
     * satu kali saja saat properti ini pertama kali diakses. Panggilan selanjutnya
     * akan langsung mengembalikan data yang sudah ada di memori, menghindari operasi
     * I/O yang berulang dan tidak perlu.
     *
     * Kode lain dapat mengakses data ini melalui `nutritionRepository.nutritionData`.
     */
    val nutritionData: Map<String, FoodNutrition> by lazy {
        loadNutritionDataFromAssets()
    }

    /**
     * Fungsi privat yang melakukan pekerjaan berat memuat dan mem-parse file JSON.
     * @return Map yang berisi data nutrisi dengan 'label' sebagai kuncinya.
     */
    private fun loadNutritionDataFromAssets(): Map<String, FoodNutrition> {
        val jsonString: String
        try {
            // Membaca file JSON dari folder assets sebagai string.
            jsonString = context.assets.open("calorie_lookup.json").bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            // Jika gagal, kembalikan map kosong untuk mencegah crash.
            return emptyMap()
        }

        // Menggunakan Gson dan TypeToken untuk mem-parse string JSON menjadi List<FoodNutrition>.
        val listFoodType = object : TypeToken<List<FoodNutrition>>() {}.type
        val foods: List<FoodNutrition> = Gson().fromJson(jsonString, listFoodType)

        // Mengubah List menjadi Map untuk pencarian berdasarkan label yang sangat efisien (O(1)).
        return foods.associateBy { it.label }
    }

    // FUNGSI getNutritionData() DIHAPUS UNTUK MENGHINDARI KONFLIK JVM SIGNATURE
}

