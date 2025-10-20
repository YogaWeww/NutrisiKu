package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.R
import com.example.nutrisiku.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

/**
 * Data class to represent a single detected food item in the UI.
 *
 * @property name The display name of the food (e.g., "Nasi Goreng").
 * @property standardPortion The standard portion size in grams.
 * @property caloriesPerPortion Calories for the standard portion.
 * @property quantity The number of this item detected.
 * @property originalResult The raw detection result from the TFLite model.
 * @property isLocked Whether this item has been locked by the user.
 */
data class DetectedFoodItem(
    val name: String,
    val standardPortion: Int,
    val caloriesPerPortion: Int,
    val quantity: Int = 1,
    val originalResult: DetectionResult,
    val isLocked: Boolean = false
)

/**
 * UI State for the detection result screen (after picking from gallery or confirming from camera).
 *
 * @property selectedBitmap The analyzed image Bitmap.
 * @property detectedItems The list of detected and grouped food items.
 * @property totalCalories The total calories of all detected items.
 * @property isLoading The loading status while detection is in progress.
 * @property sessionLabel The selected meal session label (e.g., "Sarapan").
 */
data class DetectionUiState(
    val selectedBitmap: Bitmap? = null,
    val detectedItems: List<DetectedFoodItem> = emptyList(),
    val totalCalories: Int = 0,
    val isLoading: Boolean = false,
    val sessionLabel: String = ""
)

/**
 * UI State specifically for real-time camera detection.
 *
 * @property rawDetections Raw detection results for drawing bounding boxes.
 * @property groupedItems Grouped detection results ready for UI display.
 * @property totalLockedCalories The sum of calories from user-locked items.
 * @property isConfirmEnabled Determines if the confirm button is clickable.
 * @property sourceBitmapWidth The width of the original analyzed bitmap, for scaling calculations.
 * @property sourceBitmapHeight The height of the original analyzed bitmap, for scaling calculations.
 */
data class RealtimeUiState(
    val rawDetections: List<DetectionResult> = emptyList(),
    val groupedItems: List<DetectedFoodItem> = emptyList(),
    val totalLockedCalories: Int = 0,
    val isConfirmEnabled: Boolean = false,
    val sourceBitmapWidth: Int = 1,
    val sourceBitmapHeight: Int = 1
)

/**
 * ViewModel responsible for all logic in the food detection flow.
 * Manages state for both real-time detection and single-image detection results.
 *
 * @param application The application context.
 * @param nutritionRepository Repository to get food nutrition data.
 * @param historyRepository Repository to save detection results to history.
 */
class DetectionViewModel(
    application: Application,
    private val nutritionRepository: NutritionRepository,
    private val historyRepository: HistoryRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DetectionUiState())
    val uiState = _uiState.asStateFlow()

    private val _realtimeUiState = MutableStateFlow(RealtimeUiState())
    val realtimeUiState = _realtimeUiState.asStateFlow()

    private val _events = Channel<String>()
    val events = _events.receiveAsFlow()

    private val foodDetector = FoodDetector(application)
    private val nutritionData: Map<String, FoodNutrition> = nutritionRepository.nutritionData

    // PERFORMANCE OPTIMIZATION: Create a second map for efficient lookup by display name.
    private val nutritionDataByName: Map<String, FoodNutrition> by lazy {
        nutritionData.values.associateBy { it.nama_tampilan }
    }

    private var lastAnalyzedTimestamp = 0L
    private var currentFrameBitmap: Bitmap? = null

    init {
        // Automatically set the initial session label based on the current time.
        _uiState.update { it.copy(sessionLabel = getAutomaticSessionLabel()) }
    }

    /**
     * Resets the real-time detection state.
     * Called every time the user enters the detection screen to start a new session.
     */
    fun startNewDetectionSession() {
        _realtimeUiState.value = RealtimeUiState()
    }

    /**
     * Analyzes a frame from the camera, runs detection, and updates the UI state.
     * Throttled to run every 500ms for efficiency.
     */
    fun analyzeFrame(imageProxy: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >= 500) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val bitmap = imageProxy.toBitmap().rotate(rotationDegrees.toFloat())
            currentFrameBitmap = bitmap

            viewModelScope.launch(Dispatchers.Default) {
                val results = foodDetector.detect(bitmap)
                processRealtimeDetections(results, bitmap.width, bitmap.height)
            }
            lastAnalyzedTimestamp = currentTimestamp
        }
        imageProxy.close()
    }

    /**
     * Processes real-time detection results, groups them, and updates the UI state.
     */
    private fun processRealtimeDetections(results: List<DetectionResult>, bitmapWidth: Int, bitmapHeight: Int) {
        val lockedItems = _realtimeUiState.value.groupedItems.filter { it.isLocked }
        val lockedItemNames = lockedItems.map { it.name }

        val newDetectionsGrouped = results
            .filter { nutritionData.containsKey(it.label) }
            .groupBy { nutritionData[it.label]!!.nama_tampilan }
            .mapNotNull { (name, detections) ->
                if (name !in lockedItemNames) {
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

        _realtimeUiState.update {
            it.copy(
                rawDetections = results,
                groupedItems = lockedItems + newDetectionsGrouped,
                sourceBitmapWidth = bitmapWidth,
                sourceBitmapHeight = bitmapHeight
            )
        }
    }

    /**
     * Locks or unlocks a food item in the real-time detection list.
     */
    fun toggleLockState(itemToToggle: DetectedFoodItem) {
        _realtimeUiState.update { currentState ->
            val updatedItems = currentState.groupedItems.map {
                if (it.name == itemToToggle.name) it.copy(isLocked = !it.isLocked) else it
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

    /**
     * Confirms the locked items and prepares them for the result screen.
     */
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

    /**
     * Processes an image selected from the gallery.
     */
    fun onImageSelected(bitmap: Bitmap) {
        _uiState.update { it.copy(selectedBitmap = bitmap, isLoading = true, sessionLabel = getAutomaticSessionLabel()) }
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
                    _events.send(getApplication<Application>().getString(R.string.save_history_success_message))
                } else {
                    _events.send(getApplication<Application>().getString(R.string.save_history_error_message))
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

    fun updateItemQuantity(itemToUpdate: DetectedFoodItem, newQuantity: Int) {
        val safeNewQuantity = newQuantity.coerceAtLeast(1)
        _uiState.update { currentState ->
            val updatedItems = currentState.detectedItems.map {
                if (it.name == itemToUpdate.name) it.copy(quantity = safeNewQuantity) else it
            }
            val newTotalCalories = updatedItems.sumOf { it.caloriesPerPortion * it.quantity }
            currentState.copy(detectedItems = updatedItems, totalCalories = newTotalCalories)
        }
    }

    fun updatePortion(itemIndex: Int, newPortion: Int) {
        _uiState.update { currentState ->
            val updatedItems = currentState.detectedItems.toMutableList()
            if (itemIndex in updatedItems.indices) {
                val oldItem = updatedItems[itemIndex]
                // PERFORMANCE OPTIMIZATION: Use the efficient map for lookup.
                val foodInfo = nutritionDataByName[oldItem.name]
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

    private fun getAutomaticSessionLabel(): String {
        val context = getApplication<Application>()
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.HOUR_OF_DAY)) {
            in 0..10 -> context.getString(R.string.session_breakfast)
            in 11..15 -> context.getString(R.string.session_lunch)
            in 16..20 -> context.getString(R.string.session_dinner)
            else -> context.getString(R.string.session_snack)
        }
    }

    override fun onCleared() {
        super.onCleared()
        foodDetector.close()
    }
}

private fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

