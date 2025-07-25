package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.DetectionResult
import com.example.nutrisiku.data.FoodDetector
import com.example.nutrisiku.data.FoodNutrition
import com.example.nutrisiku.data.HistoryEntity
import com.example.nutrisiku.data.HistoryFoodItem
import com.example.nutrisiku.data.HistoryRepository
import com.example.nutrisiku.data.NutritionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

// Data class untuk menampung satu item makanan yang sudah dihitung kalorinya
data class DetectedFoodItem(
    val name: String,
    val standardPortion: Int, // dalam gram
    val calories: Int,
    val originalResult: DetectionResult // Menyimpan hasil asli dari TFLite
)

// State class untuk UI hasil deteksi
data class DetectionUiState(
    val selectedBitmap: Bitmap? = null,
    val detectedItems: List<DetectedFoodItem> = emptyList(),
    val totalCalories: Int = 0,
    val isLoading: Boolean = false
)

class DetectionViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DetectionUiState())
    val uiState = _uiState.asStateFlow()

    // Inisialisasi semua repository yang dibutuhkan
    private val foodDetector = FoodDetector(application)
    private val nutritionRepository = NutritionRepository(application)
    private val historyRepository = HistoryRepository(application) // PERUBAHAN: Tambahkan HistoryRepository
    private val nutritionData: Map<String, FoodNutrition> = nutritionRepository.getNutritionData()

    // Fungsi yang dipanggil oleh UI saat gambar dipilih
    fun onImageSelected(bitmap: Bitmap) {
        _uiState.update { it.copy(selectedBitmap = bitmap, isLoading = true) }

        viewModelScope.launch {
            // Jalankan deteksi di background thread
            val detectionResults = foodDetector.detect(bitmap)

            // Proses hasil deteksi
            processDetections(detectionResults)
        }
    }

    private fun processDetections(results: List<DetectionResult>) {
        val foodItems = mutableListOf<DetectedFoodItem>()
        var totalCalories = 0

        results.forEach { result ->
            // Cari data nutrisi berdasarkan label dari model
            val foodInfo = nutritionData[result.label]
            if (foodInfo != null) {
                // Hitung kalori berdasarkan porsi standar
                val calories = (foodInfo.kalori_per_100g / 100.0 * foodInfo.porsi_standar_g).toInt()
                foodItems.add(
                    DetectedFoodItem(
                        name = foodInfo.nama_tampilan,
                        standardPortion = foodInfo.porsi_standar_g,
                        calories = calories,
                        originalResult = result
                    )
                )
                totalCalories += calories
            }
        }

        _uiState.update {
            it.copy(
                detectedItems = foodItems,
                totalCalories = totalCalories,
                isLoading = false
            )
        }
    }

    fun saveDetectionToHistory() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val bitmapToSave = currentState.selectedBitmap

            if (currentState.detectedItems.isNotEmpty() && bitmapToSave != null) {
                // PERUBAHAN: Panggil fungsi untuk menyimpan gambar dan dapatkan path-nya
                val imagePath = saveBitmapToInternalStorage(bitmapToSave)

                if (imagePath != null) {
                    val historyEntity = HistoryEntity(
                        timestamp = System.currentTimeMillis(),
                        sessionLabel = getSessionLabel(),
                        imagePath = imagePath, // Gunakan path yang sebenarnya
                        totalCalories = currentState.totalCalories,
                        foodItems = currentState.detectedItems.map {
                            HistoryFoodItem(
                                name = it.name,
                                portion = it.standardPortion,
                                calories = it.calories
                            )
                        }
                    )
                    historyRepository.insert(historyEntity)
                }
            }
        }
    }

    // PERUBAHAN: Fungsi helper baru untuk menyimpan Bitmap
    private suspend fun saveBitmapToInternalStorage(bitmap: Bitmap): String? {
        return withContext(Dispatchers.IO) { // Jalankan operasi file di background thread
            val filename = "detection_${System.currentTimeMillis()}.jpg"
            val file = File(getApplication<Application>().filesDir, filename)
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out) // Kompres gambar
                }
                file.absolutePath // Kembalikan path absolut dari file yang disimpan
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }
    private fun getSessionLabel(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 6..10 -> "Sarapan"
            in 11..15 -> "Makan Siang"
            in 18..21 -> "Makan Malam"
            else -> "Camilan"
        }
    }

    // PERUBAHAN: Fungsi baru untuk memperbarui porsi
    fun updatePortion(itemIndex: Int, newPortion: Int) {
        _uiState.update { currentState ->
            val updatedItems = currentState.detectedItems.toMutableList()
            if (itemIndex in updatedItems.indices) {
                val oldItem = updatedItems[itemIndex]

                // Cari data nutrisi asli
                val foodInfo = nutritionData[oldItem.originalResult.label]
                if (foodInfo != null) {
                    // Hitung ulang kalori berdasarkan porsi baru
                    val newCalories = (foodInfo.kalori_per_100g / 100.0 * newPortion).toInt()

                    // Ganti item lama dengan yang baru
                    updatedItems[itemIndex] = oldItem.copy(
                        standardPortion = newPortion,
                        calories = newCalories
                    )

                    // Hitung ulang total kalori
                    val newTotalCalories = updatedItems.sumOf { it.calories }

                    currentState.copy(
                        detectedItems = updatedItems,
                        totalCalories = newTotalCalories
                    )
                } else {
                    currentState // Kembalikan state lama jika data tidak ditemukan
                }
            } else {
                currentState // Kembalikan state lama jika indeks tidak valid
            }
        }
    }

    // TODO: Tambahkan fungsi untuk menyimpan hasil ke riwayat
}