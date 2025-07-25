package com.example.nutrisiku.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

// Data class untuk merepresentasikan satu item dalam file JSON
data class FoodNutrition(
    val label: String,
    val nama_tampilan: String,
    val kalori_per_100g: Int,
    val porsi_standar_g: Int
)

class NutritionRepository(private val context: Context) {

    // Memuat dan mem-parsing data nutrisi dari file JSON di assets
    fun getNutritionData(): Map<String, FoodNutrition> {
        val jsonString: String
        try {
            jsonString = context.assets.open("calorie_lookup.json").bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return emptyMap()
        }

        val listFoodType = object : TypeToken<List<FoodNutrition>>() {}.type
        val foods: List<FoodNutrition> = Gson().fromJson(jsonString, listFoodType)

        // Mengubah List menjadi Map agar pencarian berdasarkan label lebih cepat (O(1))
        return foods.associateBy { it.label }
    }
}