package com.example.nutrisiku.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Data class untuk satu item makanan dalam riwayat
data class HistoryFoodItem(
    val name: String,
    val portion: Int,
    val calories: Int
)

// Entity yang merepresentasikan tabel di database Room
@Entity(tableName = "history_table")
@TypeConverters(FoodItemConverter::class)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val sessionLabel: String,
    val imagePath: String, // Path ke gambar yang disimpan di internal storage
    val totalCalories: Int,
    val foodItems: List<HistoryFoodItem>
)

// Type Converter untuk mengubah List<HistoryFoodItem> menjadi String JSON dan sebaliknya
class FoodItemConverter {
    @TypeConverter
    fun fromFoodItemList(foodItems: List<HistoryFoodItem>): String {
        return Gson().toJson(foodItems)
    }

    @TypeConverter
    fun toFoodItemList(jsonString: String): List<HistoryFoodItem> {
        val listType = object : TypeToken<List<HistoryFoodItem>>() {}.type
        return Gson().fromJson(jsonString, listType)
    }
}