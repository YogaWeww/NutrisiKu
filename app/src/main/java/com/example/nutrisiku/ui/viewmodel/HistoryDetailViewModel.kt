package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.FoodNutrition
import com.example.nutrisiku.data.HistoryEntity
import com.example.nutrisiku.data.HistoryRepository
import com.example.nutrisiku.data.NutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryDetailViewModel(
    application: Application,
    // GANTI 'arguments: Bundle?' DENGAN 'savedStateHandle: SavedStateHandle'
    private val savedStateHandle: SavedStateHandle,
    private val historyRepository: HistoryRepository,
    private val nutritionRepository: NutritionRepository
) : AndroidViewModel(application) {

    // Ambil historyId langsung dari SavedStateHandle
    private val historyId: Int = savedStateHandle.get<Int>("historyId") ?: -1

    private val _historyDetail = MutableStateFlow<HistoryEntity?>(null)
    val historyDetail = _historyDetail.asStateFlow()

    private val nutritionData: Map<String, FoodNutrition> = nutritionRepository.getNutritionData()

    init {
        Log.d("HistoryDetailVM", "Attempting to load history with ID: $historyId")

        if (historyId != -1) {
            viewModelScope.launch {
                historyRepository.getHistoryById(historyId).collect { entity ->
                    _historyDetail.value = entity
                    if (entity != null) {
                        Log.d("HistoryDetailVM", "Successfully loaded data for ID $historyId")
                    } else {
                        Log.w("HistoryDetailVM", "No data found for ID $historyId")
                    }
                }
            }
        } else {
            Log.e("HistoryDetailVM", "Invalid or missing History ID.")
        }
    }

    // ... (sisa kode ViewModel tidak perlu diubah) ...
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

                    val foodInfo = nutritionData.values.find { food -> food.nama_tampilan == oldItem.name }

                    val newCalories = foodInfo?.let { nutrition ->
                        (nutrition.kalori_per_100g / 100.0 * newPortion).toInt()
                    } ?: oldItem.calories

                    updatedItems[itemIndex] = oldItem.copy(
                        portion = newPortion,
                        calories = newCalories
                    )
                }
                val newTotalCalories = updatedItems.sumOf { item -> item.calories }
                it.copy(foodItems = updatedItems, totalCalories = newTotalCalories)
            }
        }
    }

    fun updateHistory() {
        viewModelScope.launch {
            _historyDetail.value?.let {
                historyRepository.update(it)
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

