package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
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
import java.util.UUID

data class DetectedFoodItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val standardPortion: Int,
    val caloriesPerPortion: Int,
    var quantity: Int = 1,
    val originalResult: DetectionResult,
    val isLocked: Boolean = false
)

data class DetectionUiState(
    val selectedBitmap: Bitmap? = null,
    val detectedItems: List<DetectedFoodItem> = emptyList(),
    val totalCalories: Int = 0,
    val isLoading: Boolean = false
)

data class RealtimeUiState(
    val groupedItems: List<DetectedFoodItem> = emptyList(),
    val rawDetections: List<DetectionResult> = emptyList(),
    val totalLockedCalories: Int = 0,
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

    private val _realtimeUiState = MutableStateFlow(RealtimeUiState())
    val realtimeUiState = _realtimeUiState.asStateFlow()

    private var lastAnalyzedTimestamp = 0L
    private var currentFrameBitmap: Bitmap? = null

    fun clearDetectionState() {
        _realtimeUiState.value = RealtimeUiState()
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
                processRealtimeDetections(results)
            }
            lastAnalyzedTimestamp = currentTimestamp
        }
        imageProxy.close()
    }

    private fun processRealtimeDetections(results: List<DetectionResult>) {
        viewModelScope.launch {
            val lockedItems = _realtimeUiState.value.groupedItems.filter { it.isLocked }
            val lockedItemNames = lockedItems.map { it.name }

            val newItems = results
                .filter { result -> nutritionData.containsKey(result.label) }
                .groupBy { result -> nutritionData[result.label]!!.nama_tampilan }
                .mapNotNull { (name, group) ->
                    if (name in lockedItemNames) {
                        null
                    } else {
                        val firstResult = group.first()
                        val foodInfo = nutritionData[firstResult.label]!!
                        val calories = (foodInfo.kalori_per_100g / 100.0 * foodInfo.porsi_standar_g).toInt()
                        DetectedFoodItem(
                            name = name,
                            standardPortion = foodInfo.porsi_standar_g,
                            caloriesPerPortion = calories,
                            quantity = group.size,
                            originalResult = firstResult,
                            isLocked = false
                        )
                    }
                }

            val finalDisplayList = (lockedItems + newItems).distinctBy { it.name }

            _realtimeUiState.update {
                it.copy(
                    groupedItems = finalDisplayList,
                    rawDetections = results
                )
            }
        }
    }


    fun toggleLockState(itemToToggle: DetectedFoodItem) {
        _realtimeUiState.update { currentState ->
            val updatedItems = currentState.groupedItems.map {
                if (it.name == itemToToggle.name) {
                    it.copy(isLocked = !it.isLocked)
                } else {
                    it
                }
            }
            val newLockedItems = updatedItems.filter { it.isLocked }
            val newTotalCalories = newLockedItems.sumOf { it.caloriesPerPortion * it.quantity }
            currentState.copy(
                groupedItems = updatedItems,
                totalLockedCalories = newTotalCalories,
                isConfirmEnabled = newLockedItems.isNotEmpty()
            )
        }
    }

    // --- PERBAIKAN: Kembalikan parameter 'isFromResultScreen' ---
    fun updateItemQuantity(itemToUpdate: DetectedFoodItem, newQuantity: Int, isFromResultScreen: Boolean) {
        val targetQuantity = newQuantity.coerceAtLeast(1)

        if (isFromResultScreen) {
            // Logika untuk layar hasil
            _uiState.update { currentState ->
                val updatedItems = currentState.detectedItems.map {
                    if (it.id == itemToUpdate.id) it.copy(quantity = targetQuantity) else it
                }
                val newTotalCalories = updatedItems.sumOf { it.caloriesPerPortion * it.quantity }
                currentState.copy(detectedItems = updatedItems, totalCalories = newTotalCalories)
            }
        } else {
            // Logika untuk layar real-time (tidak digunakan lagi tapi tetap ada)
            _realtimeUiState.update { currentState ->
                val updatedItems = currentState.groupedItems.map {
                    if (it.name == itemToUpdate.name) it.copy(quantity = targetQuantity) else it
                }
                val newTotalCalories = updatedItems.filter { it.isLocked }.sumOf { it.caloriesPerPortion * it.quantity }
                currentState.copy(groupedItems = updatedItems, totalLockedCalories = newTotalCalories)
            }
        }
    }

    fun confirmRealtimeDetection() {
        val confirmedItems = _realtimeUiState.value.groupedItems.filter { it.isLocked }
        val totalCalories = _realtimeUiState.value.totalLockedCalories

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
        val foodItems = results
            .groupBy { result -> nutritionData[result.label]?.nama_tampilan }
            .mapNotNull { (name, group) ->
                if (name != null) {
                    val firstResult = group.first()
                    val foodInfo = nutritionData[firstResult.label]!!
                    val calories = (foodInfo.kalori_per_100g / 100.0 * foodInfo.porsi_standar_g).toInt()
                    DetectedFoodItem(
                        name = name,
                        standardPortion = foodInfo.porsi_standar_g,
                        caloriesPerPortion = calories,
                        quantity = group.size,
                        originalResult = firstResult,
                        isLocked = true // Item dari galeri otomatis terkunci
                    )
                } else {
                    null
                }
            }

        val totalCalories = foodItems.sumOf { it.caloriesPerPortion * it.quantity }

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
                        foodItems = currentState.detectedItems.map {
                            HistoryFoodItem(
                                name = it.name,
                                portion = it.standardPortion,
                                calories = it.caloriesPerPortion,
                                quantity = it.quantity
                            )
                        },
                        totalCalories = currentState.totalCalories
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
                // Cari info nutrisi berdasarkan nama tampilan
                val foodInfo = nutritionData.values.find { it.nama_tampilan == oldItem.name }
                if (foodInfo != null) {
                    val newCalories = (foodInfo.kalori_per_100g / 100.0 * newPortion).toInt()
                    updatedItems[itemIndex] = oldItem.copy(
                        standardPortion = newPortion,
                        caloriesPerPortion = newCalories
                    )
                    val newTotalCalories = updatedItems.sumOf { it.caloriesPerPortion * it.quantity }
                    currentState.copy(
                        detectedItems = updatedItems,
                        totalCalories = newTotalCalories
                    )
                } else {
                    currentState // Item tidak ditemukan di data nutrisi
                }
            } else {
                currentState // Indeks di luar jangkauan
            }
        }
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}

