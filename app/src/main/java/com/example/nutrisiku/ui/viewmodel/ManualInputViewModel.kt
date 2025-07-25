package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.HistoryEntity
import com.example.nutrisiku.data.HistoryFoodItem
import com.example.nutrisiku.data.HistoryRepository
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

data class ManualInputUiState(
    val foodName: String = "",
    val portion: String = "",
    val calories: String = "",
    val selectedBitmap: Bitmap? = null,
    val errorMessage: String? = null,
    val isSaveSuccess: Boolean = false
)

class ManualInputViewModel(
    application: Application,
    private val historyRepository: HistoryRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ManualInputUiState())
    val uiState = _uiState.asStateFlow()

    // ... (fungsi on...Change tetap sama) ...
    fun onFoodNameChange(name: String) { _uiState.update { it.copy(foodName = name) } }
    fun onPortionChange(portion: String) { _uiState.update { it.copy(portion = portion) } }
    fun onCaloriesChange(calories: String) { _uiState.update { it.copy(calories = calories) } }
    fun onImageSelected(bitmap: Bitmap) { _uiState.update { it.copy(selectedBitmap = bitmap) } }

    fun clearState() {
        _uiState.value = ManualInputUiState()
    }

    fun errorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun saveManualEntry() {
        viewModelScope.launch {
            Log.d("DEBUG_DB", "saveManualEntry: Fungsi dipanggil.") // Log 1: Memastikan fungsi berjalan
            val currentState = _uiState.value

            if (currentState.foodName.isBlank() || currentState.calories.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Nama makanan dan kalori wajib diisi.") }
                Log.e("DEBUG_DB", "saveManualEntry: Gagal, validasi tidak terpenuhi.") // Log 2: Gagal validasi
                return@launch
            }

            val imagePath = if (currentState.selectedBitmap != null) {
                saveBitmapToInternalStorage(currentState.selectedBitmap)
            } else {
                ""
            }

            val historyFoodItem = HistoryFoodItem(
                name = currentState.foodName,
                portion = currentState.portion.toIntOrNull() ?: 0,
                calories = currentState.calories.toIntOrNull() ?: 0
            )

            val historyEntity = HistoryEntity(
                timestamp = System.currentTimeMillis(),
                sessionLabel = getSessionLabel(),
                imagePath = imagePath ?: "",
                totalCalories = historyFoodItem.calories,
                foodItems = listOf(historyFoodItem)
            )

            Log.d("DEBUG_DB", "saveManualEntry: Siap untuk insert ke database.") // Log 3: Tepat sebelum insert
            historyRepository.insert(historyEntity)
            Log.d("DEBUG_DB", "saveManualEntry: Insert ke database berhasil.") // Log 4: Setelah insert

            _uiState.update { it.copy(isSaveSuccess = true) }
        }
    }

    private suspend fun saveBitmapToInternalStorage(bitmap: Bitmap): String? {
        return withContext(Dispatchers.IO) {
            val filename = "manual_${System.currentTimeMillis()}.jpg"
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
}