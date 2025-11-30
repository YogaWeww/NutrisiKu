package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
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

// Data class untuk satu item makanan dalam daftar input manual
data class ManualFoodItem(
    val id: Long = System.currentTimeMillis(), // ID unik untuk setiap item
    val foodName: String = "",
    val portion: String = "",
    val calories: String = "",
    val quantity: Int = 1
)

data class ManualInputUiState(
    val foodItems: List<ManualFoodItem> = listOf(ManualFoodItem()), // Mulai dengan satu item kosong
    val selectedBitmap: Bitmap? = null,
    val errorMessage: String? = null,
    val isSaveSuccess: Boolean = false,
    val sessionLabel: String = "Sarapan",
    val isSaveButtonEnabled: Boolean = false
)

class ManualInputViewModel(
    application: Application,
    private val historyRepository: HistoryRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ManualInputUiState())
    val uiState = _uiState.asStateFlow()

    // Fungsi untuk memperbarui properti dari item makanan tertentu
    fun onFoodItemChange(index: Int, updatedItem: ManualFoodItem) {
        _uiState.update { currentState ->
            val updatedList = currentState.foodItems.toMutableList()
            if (index in updatedList.indices) {
                updatedList[index] = updatedItem
            }
            currentState.copy(foodItems = updatedList)
        }
        validateState()
    }

    // Fungsi untuk menambah item makanan baru ke daftar
    fun addFoodItem() {
        _uiState.update { currentState ->
            currentState.copy(foodItems = currentState.foodItems + ManualFoodItem())
        }
        validateState()
    }

    // Fungsi untuk menghapus item makanan dari daftar
    fun removeFoodItem(index: Int) {
        _uiState.update { currentState ->
            val updatedList = currentState.foodItems.toMutableList()
            if (index in updatedList.indices) {
                updatedList.removeAt(index)
            }
            // Jika daftar menjadi kosong, tambahkan satu item kosong kembali
            if (updatedList.isEmpty()) {
                updatedList.add(ManualFoodItem())
            }
            currentState.copy(foodItems = updatedList)
        }
        validateState()
    }

    fun onImageSelected(bitmap: Bitmap) { _uiState.update { it.copy(selectedBitmap = bitmap) } }
    fun onSessionLabelChange(newLabel: String) { _uiState.update { it.copy(sessionLabel = newLabel) } }
    fun clearState() { _uiState.value = ManualInputUiState() }
    fun errorMessageShown() { _uiState.update { it.copy(errorMessage = null) } }

    fun saveManualEntry() {
        viewModelScope.launch {
            val currentState = _uiState.value

            // Validasi: Pastikan tidak ada item yang kosong
            val allItemsValid = currentState.foodItems.all {
                it.foodName.isNotBlank() && it.portion.isNotBlank() && it.calories.isNotBlank()
            }

            if (!allItemsValid) {
                _uiState.update { it.copy(errorMessage = "Semua kolom pada setiap makanan wajib diisi.") }
                return@launch
            }

            val imagePath = if (currentState.selectedBitmap != null) {
                saveBitmapToInternalStorage(currentState.selectedBitmap)
            } else { "" }

            val historyFoodItems = currentState.foodItems.map {
                HistoryFoodItem(
                    name = it.foodName,
                    portion = it.portion.toIntOrNull() ?: 0,
                    calories = it.calories.toIntOrNull() ?: 0,
                    quantity = it.quantity
                )
            }

            val totalCalories = historyFoodItems.sumOf { it.calories * it.quantity }

            val historyEntity = HistoryEntity(
                timestamp = System.currentTimeMillis(),
                sessionLabel = currentState.sessionLabel,
                imagePath = imagePath ?: "",
                totalCalories = totalCalories,
                foodItems = historyFoodItems
            )

            historyRepository.insert(historyEntity)
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

    private fun validateState() {
        _uiState.update { currentState ->
            val allItemsValid = currentState.foodItems.all {
                it.foodName.isNotBlank() && it.portion.isNotBlank() && it.calories.isNotBlank()
            }
            currentState.copy(isSaveButtonEnabled = allItemsValid)
        }
    }
}

