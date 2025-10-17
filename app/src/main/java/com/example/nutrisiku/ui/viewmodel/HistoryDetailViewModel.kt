package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.HistoryEntity
import com.example.nutrisiku.data.HistoryRepository
import com.example.nutrisiku.data.FoodNutrition
import com.example.nutrisiku.data.NutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk layar Detail Riwayat dan Edit Riwayat.
 * Mengelola state untuk satu entri riwayat, menangani pembaruan, dan penghapusan.
 *
 * @param application Konteks aplikasi.
 * @param savedStateHandle Menangani state yang tersimpan dan argumen navigasi (seperti historyId).
 * @param historyRepository Repository untuk berinteraksi dengan data riwayat di database.
 * @param nutritionRepository Repository untuk mendapatkan data nutrisi makanan.
 */
class HistoryDetailViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val historyRepository: HistoryRepository,
    private val nutritionRepository: NutritionRepository
) : AndroidViewModel(application) {

    // Mendapatkan ID riwayat dari argumen navigasi.
    private val historyId: Int = savedStateHandle.get<Int>("historyId") ?: -1

    private val _historyDetail = MutableStateFlow<HistoryEntity?>(null)
    val historyDetail = _historyDetail.asStateFlow()

    // PERBAIKAN: Mengakses properti, bukan memanggil fungsi.
    private val nutritionData: Map<String, FoodNutrition> by lazy {
        nutritionRepository.nutritionData
    }

    // PENINGKATAN: Buat map kedua yang dioptimalkan untuk pencarian berdasarkan nama tampilan.
    // Ini jauh lebih efisien daripada menggunakan .find() pada list setiap saat.
    private val nutritionDataByName: Map<String, FoodNutrition> by lazy {
        nutritionData.values.associateBy { it.nama_tampilan }
    }


    init {
        if (historyId != -1) {
            viewModelScope.launch {
                historyRepository.getHistoryById(historyId).collect { entity ->
                    _historyDetail.value = entity
                }
            }
        }
    }

    fun onNameChange(itemIndex: Int, newName: String) {
        _historyDetail.update { currentDetail ->
            currentDetail?.copy(
                foodItems = currentDetail.foodItems.mapIndexed { index, item ->
                    if (index == itemIndex) item.copy(name = newName) else item
                }
            )
        }
    }

    fun onPortionChange(itemIndex: Int, newPortionString: String) {
        _historyDetail.update { currentDetail ->
            currentDetail?.let {
                val updatedItems = it.foodItems.toMutableList()
                if (itemIndex in updatedItems.indices) {
                    val oldItem = updatedItems[itemIndex]
                    val newPortion = newPortionString.toIntOrNull() ?: 0

                    // PENINGKATAN: Gunakan map yang efisien untuk lookup.
                    val foodInfo = nutritionDataByName[oldItem.name]

                    val newCalories = foodInfo?.let { nutrition ->
                        (nutrition.kalori_per_100g / 100.0 * newPortion).toInt()
                    } ?: oldItem.calories

                    updatedItems[itemIndex] = oldItem.copy(
                        portion = newPortion,
                        calories = newCalories
                    )
                }
                val newTotalCalories = updatedItems.sumOf { item -> item.calories * item.quantity }
                it.copy(foodItems = updatedItems, totalCalories = newTotalCalories)
            }
        }
    }

    fun onCaloriesChange(itemIndex: Int, newCaloriesString: String) {
        _historyDetail.update { currentDetail ->
            currentDetail?.let {
                val updatedItems = it.foodItems.toMutableList()
                if (itemIndex in updatedItems.indices) {
                    val oldItem = updatedItems[itemIndex]
                    val newCalories = newCaloriesString.toIntOrNull() ?: 0
                    updatedItems[itemIndex] = oldItem.copy(calories = newCalories)
                }
                val newTotalCalories = updatedItems.sumOf { item -> item.calories * item.quantity }
                it.copy(foodItems = updatedItems, totalCalories = newTotalCalories)
            }
        }
    }


    fun onQuantityChange(itemIndex: Int, newQuantity: Int) {
        if (newQuantity <= 0) return

        _historyDetail.update { currentDetail ->
            currentDetail?.let {
                val updatedItems = it.foodItems.toMutableList()
                if (itemIndex in updatedItems.indices) {
                    val oldItem = updatedItems[itemIndex]
                    updatedItems[itemIndex] = oldItem.copy(quantity = newQuantity)
                }
                val newTotalCalories = updatedItems.sumOf { item -> item.calories * item.quantity }
                it.copy(foodItems = updatedItems, totalCalories = newTotalCalories)
            }
        }
    }

    fun onDeleteItem(itemIndex: Int) {
        _historyDetail.update { currentDetail ->
            currentDetail?.let {
                val updatedItems = it.foodItems.toMutableList()
                if (itemIndex in updatedItems.indices) {
                    updatedItems.removeAt(itemIndex)
                }
                val newTotalCalories = updatedItems.sumOf { item -> item.calories * item.quantity }
                it.copy(foodItems = updatedItems, totalCalories = newTotalCalories)
            }
        }
    }

    fun onSessionLabelChange(newLabel: String) {
        _historyDetail.update { currentDetail ->
            currentDetail?.copy(sessionLabel = newLabel)
        }
    }

    fun updateOrDeleteHistory(onComplete: (wasDeleted: Boolean) -> Unit) {
        viewModelScope.launch {
            _historyDetail.value?.let { detail ->
                if (detail.foodItems.isEmpty()) {
                    historyRepository.delete(detail)
                    Log.d("HistoryDetailVM", "History item deleted because it was empty.")
                    onComplete(true)
                } else {
                    historyRepository.update(detail)
                    Log.d("HistoryDetailVM", "History item updated.")
                    onComplete(false)
                }
            }
        }
    }

    fun deleteHistory() {
        viewModelScope.launch {
            _historyDetail.value?.let {
                historyRepository.delete(it)
            }
        }
    }
}
