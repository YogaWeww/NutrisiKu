package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Data class untuk merepresentasikan satu item makanan yang terdeteksi di UI
data class DetectedFoodItem(
    val name: String,
    val standardPortion: Int, // Porsi dalam gram
    val caloriesPerPortion: Int, // Kalori untuk porsi standar
    val quantity: Int = 1, // Jumlah item ini
    val originalResult: DetectionResult, // Hasil mentah dari model
    val isLocked: Boolean = false
)

// UI State untuk layar deteksi (baik galeri maupun hasil real-time)
data class DetectionUiState(
    val selectedBitmap: Bitmap? = null,
    val detectedItems: List<DetectedFoodItem> = emptyList(),
    val totalCalories: Int = 0,
    val isLoading: Boolean = false,
    val sessionLabel: String = "Sarapan" // PERUBAHAN: Tambahkan state untuk label sesi
)

// UI State khusus untuk deteksi real-time
data class RealtimeUiState(
    val rawDetections: List<DetectionResult> = emptyList(), // Untuk menggambar bounding box
    val groupedItems: List<DetectedFoodItem> = emptyList(), // Untuk ditampilkan di daftar
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
    }

    fun analyzeFrame(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >= 500) { // Analisis setiap 500ms
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
        val lockedItems = _realtimeUiState.value.groupedItems.filter { it.isLocked }
        val lockedItemNames = lockedItems.map { it.name }

        // Kelompokkan hasil deteksi baru berdasarkan nama, hitung jumlahnya
        val newDetectionsGrouped = results
            .filter { nutritionData.containsKey(it.label) } // Hanya proses item yang ada di lookup table
            .groupBy { nutritionData[it.label]!!.nama_tampilan }
            .mapNotNull { (name, detections) ->
                if (name !in lockedItemNames) { // Jangan proses item baru jika namanya sudah dikunci
                    val firstDetection = detections.first()
                    val foodInfo = nutritionData[firstDetection.label]!!
                    val calories = (foodInfo.kalori_per_100g / 100.0 * foodInfo.porsi_standar_g).toInt()
                    DetectedFoodItem(
                        name = name,
                        standardPortion = foodInfo.porsi_standar_g,
                        caloriesPerPortion = calories,
                        quantity = detections.size,
                        originalResult = firstDetection // Simpan satu hasil asli untuk referensi
                    )
                } else {
                    null
                }
            }

        _realtimeUiState.update {
            it.copy(
                rawDetections = results, // Simpan semua hasil mentah untuk bounding box
                groupedItems = lockedItems + newDetectionsGrouped, // Gabungkan item terkunci dan item baru
            )
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
            val lockedItems = updatedItems.filter { it.isLocked }
            val newTotalCalories = lockedItems.sumOf { it.caloriesPerPortion * it.quantity }
            currentState.copy(
                groupedItems = updatedItems,
                totalLockedCalories = newTotalCalories,
                isConfirmEnabled = lockedItems.isNotEmpty()
            )
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
        val foodItemsGrouped = results
            .groupBy { nutritionData[it.label]?.nama_tampilan }
            .mapNotNull { (name, detections) ->
                if (name != null) {
                    val firstDetection = detections.first()
                    val foodInfo = nutritionData[firstDetection.label]!!
                    val calories = (foodInfo.kalori_per_100g / 100.0 * foodInfo.porsi_standar_g).toInt()
                    DetectedFoodItem(
                        name = name,
                        standardPortion = foodInfo.porsi_standar_g,
                        caloriesPerPortion = calories,
                        quantity = detections.size,
                        originalResult = firstDetection
                    )
                } else {
                    null
                }
            }

        val totalCalories = foodItemsGrouped.sumOf { it.caloriesPerPortion * it.quantity }

        _uiState.update {
            it.copy(
                detectedItems = foodItemsGrouped,
                totalCalories = totalCalories,
                isLoading = false
            )
        }
    }

    // --- PERUBAHAN ---
    // Fungsi baru untuk mengubah label sesi dari UI
    fun onSessionLabelChange(newLabel: String) {
        _uiState.update { it.copy(sessionLabel = newLabel) }
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
                        // Gunakan label dari state, bukan dari fungsi otomatis
                        sessionLabel = currentState.sessionLabel,
                        imagePath = imagePath,
                        totalCalories = currentState.totalCalories,
                        foodItems = currentState.detectedItems.map {
                            HistoryFoodItem(
                                name = it.name,
                                portion = it.standardPortion,
                                calories = it.caloriesPerPortion,
                                quantity = it.quantity
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

    // FUNGSI getSessionLabel() DIHAPUS DARI SINI

    fun updateItemQuantity(itemToUpdate: DetectedFoodItem, newQuantity: Int, isFromResultScreen: Boolean) {
        val safeNewQuantity = newQuantity.coerceAtLeast(1)
        if (isFromResultScreen) {
            _uiState.update { currentState ->
                val updatedItems = currentState.detectedItems.map {
                    if (it.name == itemToUpdate.name) {
                        it.copy(quantity = safeNewQuantity)
                    } else {
                        it
                    }
                }
                val newTotalCalories = updatedItems.sumOf { it.caloriesPerPortion * it.quantity }
                currentState.copy(detectedItems = updatedItems, totalCalories = newTotalCalories)
            }
        } else {
            _realtimeUiState.update { currentState ->
                val updatedItems = currentState.groupedItems.map {
                    if (it.name == itemToUpdate.name) {
                        it.copy(quantity = safeNewQuantity)
                    } else {
                        it
                    }
                }
                val newTotalCalories = updatedItems.filter { it.isLocked }.sumOf { it.caloriesPerPortion * it.quantity }
                currentState.copy(groupedItems = updatedItems, totalLockedCalories = newTotalCalories)
            }
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
                        caloriesPerPortion = newCalories
                    )
                    val newTotalCalories = updatedItems.sumOf { it.caloriesPerPortion * it.quantity }
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

