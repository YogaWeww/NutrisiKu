package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.HistoryEntity
import com.example.nutrisiku.data.HistoryFoodItem
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

    private val historyId: Int = savedStateHandle.get<Int>("historyId") ?: -1

    private val _historyDetail = MutableStateFlow<HistoryEntity?>(null)
    val historyDetail = _historyDetail.asStateFlow()

    private val nutritionData: Map<String, FoodNutrition> by lazy {
        nutritionRepository.nutritionData
    }

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

    /**
     * Fungsi helper privat untuk memodifikasi daftar item makanan dan menghitung ulang total kalori.
     * Ini mengurangi duplikasi kode di fungsi-fungsi handler event.
     *
     * @param action Lambda yang berisi logika modifikasi pada daftar item makanan.
     */
    private fun updateFoodItemsAndRecalculate(action: (MutableList<HistoryFoodItem>) -> Unit) {
        _historyDetail.update { currentDetail ->
            currentDetail?.let { detail ->
                val updatedItems = detail.foodItems.toMutableList()
                action(updatedItems)
                val newTotalCalories = updatedItems.sumOf { it.calories * it.quantity }
                detail.copy(foodItems = updatedItems, totalCalories = newTotalCalories)
            }
        }
    }

    /**
     * Memperbarui nama item makanan pada indeks tertentu.
     */
    fun onNameChange(itemIndex: Int, newName: String) {
        // Aksi ini tidak memengaruhi total kalori, jadi bisa ditangani secara terpisah.
        _historyDetail.update { currentDetail ->
            currentDetail?.copy(
                foodItems = currentDetail.foodItems.mapIndexed { index, item ->
                    if (index == itemIndex) item.copy(name = newName) else item
                }
            )
        }
    }

    /**
     * Memperbarui porsi item makanan dan menghitung ulang kalorinya.
     */
    fun onPortionChange(itemIndex: Int, newPortionString: String) {
        updateFoodItemsAndRecalculate { items ->
            if (itemIndex in items.indices) {
                val oldItem = items[itemIndex]
                val newPortion = newPortionString.toIntOrNull() ?: 0
                val foodInfo = nutritionDataByName[oldItem.name]
                val newCalories = foodInfo?.let { (it.kalori_per_100g / 100.0 * newPortion).toInt() } ?: oldItem.calories
                items[itemIndex] = oldItem.copy(portion = newPortion, calories = newCalories)
            }
        }
    }

    /**
     * Memperbarui kalori item makanan secara manual.
     */
    fun onCaloriesChange(itemIndex: Int, newCaloriesString: String) {
        updateFoodItemsAndRecalculate { items ->
            if (itemIndex in items.indices) {
                val newCalories = newCaloriesString.toIntOrNull() ?: 0
                items[itemIndex] = items[itemIndex].copy(calories = newCalories)
            }
        }
    }

    /**
     * Memperbarui kuantitas item makanan.
     */
    fun onQuantityChange(itemIndex: Int, newQuantity: Int) {
        if (newQuantity <= 0) return
        updateFoodItemsAndRecalculate { items ->
            if (itemIndex in items.indices) {
                items[itemIndex] = items[itemIndex].copy(quantity = newQuantity)
            }
        }
    }

    /**
     * Menghapus item makanan dari daftar.
     */
    fun onDeleteItem(itemIndex: Int) {
        updateFoodItemsAndRecalculate { items ->
            if (itemIndex in items.indices) {
                items.removeAt(itemIndex)
            }
        }
    }

    /**
     * Memperbarui label sesi makan.
     */
    fun onSessionLabelChange(newLabel: String) {
        _historyDetail.update { it?.copy(sessionLabel = newLabel) }
    }

    /**
     * Menyimpan perubahan ke database. Jika semua item makanan dihapus,
     * entri riwayat akan dihapus seluruhnya.
     * @param onComplete Callback yang menginformasikan apakah entri dihapus (true) atau hanya diperbarui (false).
     */
    fun updateOrDeleteHistory(onComplete: (wasDeleted: Boolean) -> Unit) {
        viewModelScope.launch {
            _historyDetail.value?.let { detail ->
                if (detail.foodItems.isEmpty()) {
                    historyRepository.delete(detail)
                    onComplete(true)
                } else {
                    historyRepository.update(detail)
                    onComplete(false)
                }
            }
        }
    }

    /**
     * Menghapus seluruh entri riwayat dari database.
     */
    fun deleteHistory() {
        viewModelScope.launch {
            _historyDetail.value?.let { historyRepository.delete(it) }
        }
    }
}

