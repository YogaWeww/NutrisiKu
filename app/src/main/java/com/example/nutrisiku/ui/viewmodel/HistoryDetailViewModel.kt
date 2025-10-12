package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.FoodNutrition
import com.example.nutrisiku.data.HistoryEntity
import com.example.nutrisiku.data.HistoryFoodItem
import com.example.nutrisiku.data.HistoryRepository
import com.example.nutrisiku.data.NutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryDetailViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val historyRepository: HistoryRepository,
    private val nutritionRepository: NutritionRepository
) : AndroidViewModel(application) {

    private val historyId: Int = savedStateHandle.get<Int>("historyId") ?: -1

    private val _historyDetail = MutableStateFlow<HistoryEntity?>(null)
    val historyDetail = _historyDetail.asStateFlow()

    private val nutritionData: Map<String, FoodNutrition> by lazy {
        nutritionRepository.getNutritionData()
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

                    val foodInfo = nutritionData.values.find { food -> food.nama_tampilan == oldItem.name }

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

    fun onQuantityChange(itemIndex: Int, newQuantity: Int) {
        if (newQuantity <= 0) return // Kuantitas tidak boleh kurang dari 1

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

    // --- PERUBAHAN LOGIKA UTAMA DI SINI ---
    fun updateOrDeleteHistory() {
        viewModelScope.launch {
            _historyDetail.value?.let { detail ->
                // Jika setelah diedit daftar makanannya kosong
                if (detail.foodItems.isEmpty()) {
                    // Hapus seluruh entri riwayat
                    historyRepository.delete(detail)
                    Log.d("HistoryDetailVM", "History item deleted because it was empty.")
                } else {
                    // Jika masih ada item, update seperti biasa
                    historyRepository.update(detail)
                    Log.d("HistoryDetailVM", "History item updated.")
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

