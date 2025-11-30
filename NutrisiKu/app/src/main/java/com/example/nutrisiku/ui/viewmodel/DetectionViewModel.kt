package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
 * Data class untuk merepresentasikan satu item makanan yang terdeteksi di UI.
 *
 * @property name Nama tampilan makanan (mis. "Nasi Goreng").
 * @property standardPortion Porsi standar dalam gram.
 * @property caloriesPerPortion Kalori untuk porsi standar.
 * @property quantity Jumlah item ini yang terdeteksi.
 * @property originalResult Hasil deteksi mentah dari model TFLite.
 * @property isLocked Status apakah item ini telah dikunci oleh pengguna.
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
 * UI State untuk layar hasil deteksi (setelah memilih dari galeri atau mengkonfirmasi dari kamera).
 *
 * @property selectedBitmap Bitmap gambar yang dianalisis.
 * @property detectedItems Daftar item makanan yang terdeteksi dan dikelompokkan.
 * @property totalCalories Total kalori dari semua item yang terdeteksi.
 * @property isLoading Status loading saat deteksi sedang berlangsung.
 * @property sessionLabel Label sesi makan yang dipilih (mis. "Sarapan").
 */
data class DetectionUiState(
    val selectedBitmap: Bitmap? = null,
    val detectedItems: List<DetectedFoodItem> = emptyList(),
    val totalCalories: Int = 0,
    val isLoading: Boolean = false,
    val sessionLabel: String = "Sarapan"
)

/**
 * UI State khusus untuk deteksi real-time dari kamera.
 *
 * @property rawDetections Hasil deteksi mentah untuk menggambar bounding box.
 * @property groupedItems Hasil deteksi yang sudah dikelompokkan dan siap ditampilkan di UI.
 * @property totalLockedCalories Jumlah kalori dari item yang sudah dikunci oleh pengguna.
 * @property isConfirmEnabled Menentukan apakah tombol konfirmasi bisa diklik.
 * @property sourceBitmapWidth Lebar bitmap asli yang dianalisis, untuk kalkulasi skala.
 * @property sourceBitmapHeight Tinggi bitmap asli yang dianalisis, untuk kalkulasi skala.
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
 * ViewModel yang bertanggung jawab atas semua logika di alur deteksi makanan.
 * Mengelola state untuk deteksi real-time dan hasil deteksi gambar tunggal.
 *
 * @param application Konteks aplikasi.
 * @param nutritionRepository Repository untuk mendapatkan data nutrisi makanan.
 * @param historyRepository Repository untuk menyimpan hasil deteksi ke riwayat.
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

    private var lastAnalyzedTimestamp = 0L
    private var currentFrameBitmap: Bitmap? = null

    init {
        // Setel label sesi awal secara otomatis berdasarkan waktu saat ini
        _uiState.update { it.copy(sessionLabel = getAutomaticSessionLabel()) }
    }

    /**
     * PERBAIKAN: Fungsi untuk me-reset state deteksi real-time.
     * Dipanggil setiap kali pengguna masuk ke layar deteksi untuk memulai sesi baru.
     */
    fun startNewDetectionSession() {
        _realtimeUiState.value = RealtimeUiState()
    }

    /**
     * Menganalisis frame dari kamera, menjalankan deteksi, dan memperbarui UI state.
     * Dibatasi untuk berjalan setiap 500ms untuk efisiensi.
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
     * Memproses hasil deteksi real-time, mengelompokkannya, dan memperbarui UI state.
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
     * Mengunci atau membuka kunci item makanan dalam daftar deteksi real-time.
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
     * Mengkonfirmasi item yang terkunci dan menyiapkannya untuk layar hasil.
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
     * Memproses gambar yang dipilih dari galeri.
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
                    _events.send("Berhasil disimpan ke riwayat")
                } else {
                    _events.send("Gagal menyimpan gambar")
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
                    currentState
                }
            } else {
                currentState
            }
        }
    }

    private fun getAutomaticSessionLabel(): String {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.HOUR_OF_DAY)) {
            in 0..10 -> "Sarapan"
            in 11..15 -> "Makan Siang"
            in 16..20 -> "Makan Malam"
            else -> "Camilan"
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

