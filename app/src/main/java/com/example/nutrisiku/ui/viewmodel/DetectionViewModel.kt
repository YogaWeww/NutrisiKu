package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
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

data class DetectedFoodItem(
    val name: String,
    val standardPortion: Int,
    val calories: Int,
    val originalResult: DetectionResult,
    val isLocked: Boolean = false
)

data class DetectionUiState(
    val selectedBitmap: Bitmap? = null,
    val detectedItems: List<DetectedFoodItem> = emptyList(),
    val totalCalories: Int = 0,
    val isLoading: Boolean = false,
    val isConfirmEnabled: Boolean = false
)

class DetectionViewModel(
    application: Application,
    private val nutritionRepository: NutritionRepository,
    private val historyRepository: HistoryRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DetectionUiState())
    val uiState = _uiState.asStateFlow()

    private val foodDetector = FoodDetector(application)
    private val nutritionData: Map<String, FoodNutrition> = nutritionRepository.getNutritionData()

    private val _realtimeResults = MutableStateFlow<List<DetectionResult>>(emptyList())
    val realtimeResults = _realtimeResults.asStateFlow()

    private val _realtimeUiState = MutableStateFlow(DetectionUiState())
    val realtimeUiState = _realtimeUiState.asStateFlow()

    private var lastAnalyzedTimestamp = 0L
    private var currentFrameBitmap: Bitmap? = null

    fun clearDetectionState() {
        _realtimeUiState.value = DetectionUiState()
        _realtimeResults.value = emptyList()
        currentFrameBitmap = null
    }

    fun analyzeFrame(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >= 500) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val bitmap = imageProxy.toBitmap().rotate(rotationDegrees.toFloat())
            currentFrameBitmap = bitmap

            viewModelScope.launch(Dispatchers.Default) {
                val results = foodDetector.detect(bitmap)
                _realtimeResults.value = results
                processRealtimeDetections(results)
            }
            lastAnalyzedTimestamp = currentTimestamp
        }
        imageProxy.close()
    }

    // --- PERBAIKAN UTAMA DI SINI ---
    private fun processRealtimeDetections(results: List<DetectionResult>) {
        // 1. Ambil item yang sudah dikunci dari state saat ini
        val lockedItems = _realtimeUiState.value.detectedItems.filter { it.isLocked }
        val lockedItemNames = lockedItems.map { it.name }

        // 2. Ubah hasil deteksi mentah dari model menjadi list item yang bisa ditampilkan
        val newDetectedItems = results.mapNotNull { result ->
            val foodInfo = nutritionData[result.label]
            if (foodInfo != null) {
                val calories = (foodInfo.kalori_per_100g / 100.0 * foodInfo.porsi_standar_g).toInt()
                DetectedFoodItem(
                    name = foodInfo.nama_tampilan,
                    standardPortion = foodInfo.porsi_standar_g,
                    calories = calories,
                    originalResult = result,
                    isLocked = false
                )
            } else {
                null
            }
        }

        // 3. Filter item baru, buang yang namanya sama dengan item yang sudah dikunci
        val uniqueNewItems = newDetectedItems.filter { it.name !in lockedItemNames }

        // 4. Gabungkan: item yang sudah dikunci + item baru yang unik
        val finalDisplayList = lockedItems + uniqueNewItems

        // 5. Update UI state dengan daftar final
        _realtimeUiState.update {
            it.copy(detectedItems = finalDisplayList)
        }
    }


    fun toggleLockState(itemToToggle: DetectedFoodItem) {
        _realtimeUiState.update { currentState ->
            val updatedItems = currentState.detectedItems.map {
                if (it.name == itemToToggle.name) {
                    it.copy(isLocked = !it.isLocked)
                } else {
                    it
                }
            }
            val lockedItems = updatedItems.filter { it.isLocked }
            val newTotalCalories = lockedItems.sumOf { it.calories }
            currentState.copy(
                detectedItems = updatedItems,
                totalCalories = newTotalCalories,
                isConfirmEnabled = lockedItems.isNotEmpty()
            )
        }
    }

    fun confirmRealtimeDetection() {
        val confirmedItems = _realtimeUiState.value.detectedItems.filter { it.isLocked }
        val totalCalories = _realtimeUiState.value.totalCalories

        _uiState.update {
            it.copy(
                selectedBitmap = currentFrameBitmap,
                detectedItems = confirmedItems,
                totalCalories = totalCalories,
                isLoading = false
            )
        }
    }

    fun onImageSelected(bitmap: Bitmap) {
        _uiState.update { it.copy(selectedBitmap = bitmap, isLoading = true) }
        viewModelScope.launch {
            val detectionResults = foodDetector.detect(bitmap)
            processDetections(detectionResults)
        }
    }

    private fun processDetections(results: List<DetectionResult>) {
        val foodItems = mutableListOf<DetectedFoodItem>()
        var totalCalories = 0
        results.forEach { result ->
            val foodInfo = nutritionData[result.label]
            if (foodInfo != null) {
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
                val imagePath = saveBitmapToInternalStorage(bitmapToSave)
                if (imagePath != null) {
                    val historyEntity = HistoryEntity(
                        timestamp = System.currentTimeMillis(),
                        sessionLabel = getSessionLabel(),
                        imagePath = imagePath,
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

    private suspend fun saveBitmapToInternalStorage(bitmap: Bitmap): String? {
        return withContext(Dispatchers.IO) {
            val filename = "detection_${System.currentTimeMillis()}.jpg"
            val file = File(getApplication<Application>().filesDir, filename)
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                file.absolutePath
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

    fun updatePortion(itemIndex: Int, newPortion: Int) {
        _uiState.update { currentState ->
            val updatedItems = currentState.detectedItems.toMutableList()
            if (itemIndex in updatedItems.indices) {
                val oldItem = updatedItems[itemIndex]
                val foodInfo = nutritionData[oldItem.originalResult.label]
                if (foodInfo != null) {
                    val newCalories = (foodInfo.kalori_per_100g / 100.0 * newPortion).toInt()
                    updatedItems[itemIndex] = oldItem.copy(
                        standardPortion = newPortion,
                        calories = newCalories
                    )
                    val newTotalCalories = updatedItems.sumOf { it.calories }
                    currentState.copy(
                        detectedItems = updatedItems,
                        totalCalories = newTotalCalories
                    )
                } else {
                    currentState
                }
            } else {
                currentState
            }
        }
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}