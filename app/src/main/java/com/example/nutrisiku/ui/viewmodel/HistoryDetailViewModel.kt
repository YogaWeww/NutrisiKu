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
import kotlinx.coroutines.flow.* // Pastikan semua impor flow ada
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

    // State internal untuk detail riwayat
    private val _historyDetail = MutableStateFlow<HistoryEntity?>(null)
    val historyDetail: StateFlow<HistoryEntity?> = _historyDetail.asStateFlow()

    // State untuk menandakan perubahan yang belum disimpan
    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    private val nutritionData: Map<String, FoodNutrition> by lazy {
        nutritionRepository.nutritionData
    }

    private val nutritionDataByName: Map<String, FoodNutrition> by lazy {
        nutritionData.values.associateBy { it.nama_tampilan }
    }

    // Simpan data asli saat pertama kali dimuat untuk perbandingan
    private var originalHistoryEntity: HistoryEntity? = null

    init {
        if (historyId != -1) {
            viewModelScope.launch {
                historyRepository.getHistoryById(historyId)
                    .filterNotNull() // Hanya proses jika data tidak null
                    .collect { entity ->
                        _historyDetail.value = entity
                        // Simpan data asli saat pertama kali dimuat
                        if (originalHistoryEntity == null) {
                            originalHistoryEntity = entity.copy() // Buat salinan
                        }
                        // Reset flag perubahan saat data baru dari repo masuk
                        _hasUnsavedChanges.value = false
                    }
            }
        }
    }

    // Fungsi helper untuk menandai ada perubahan
    private fun setHasUnsavedChanges() {
        if (!_hasUnsavedChanges.value) {
            _hasUnsavedChanges.value = true
        }
    }

    /**
     * Membuang perubahan yang belum disimpan dengan memuat ulang data dari repository.
     */
    fun discardChanges() {
        // Cukup setel state detail kembali ke data asli yang disimpan
        originalHistoryEntity?.let {
            _historyDetail.value = it.copy() // Kembalikan ke salinan asli
            _hasUnsavedChanges.value = false // Reset flag
        }
    }


    /**
     * Fungsi helper privat untuk memodifikasi daftar item makanan, menghitung ulang total kalori,
     * dan menandai adanya perubahan.
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
        // Tandai ada perubahan setelah modifikasi
        setHasUnsavedChanges()
    }

    fun onNameChange(itemIndex: Int, newName: String) {
        _historyDetail.update { currentDetail ->
            currentDetail?.copy(
                foodItems = currentDetail.foodItems.mapIndexed { index, item ->
                    if (index == itemIndex) item.copy(name = newName) else item
                }
            )
        }
        setHasUnsavedChanges()
    }

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

    fun onCaloriesChange(itemIndex: Int, newCaloriesString: String) {
        updateFoodItemsAndRecalculate { items ->
            if (itemIndex in items.indices) {
                val newCalories = newCaloriesString.toIntOrNull() ?: 0
                items[itemIndex] = items[itemIndex].copy(calories = newCalories)
            }
        }
    }

    fun onQuantityChange(itemIndex: Int, newQuantity: Int) {
        if (newQuantity <= 0) return
        updateFoodItemsAndRecalculate { items ->
            if (itemIndex in items.indices) {
                items[itemIndex] = items[itemIndex].copy(quantity = newQuantity)
            }
        }
    }

    fun onDeleteItem(itemIndex: Int) {
        updateFoodItemsAndRecalculate { items ->
            if (itemIndex in items.indices) {
                items.removeAt(itemIndex)
            }
        }
    }

    fun onSessionLabelChange(newLabel: String) {
        _historyDetail.update { it?.copy(sessionLabel = newLabel) }
        setHasUnsavedChanges()
    }

    fun updateOrDeleteHistory(onComplete: (wasDeleted: Boolean) -> Unit) {
        viewModelScope.launch {
            _historyDetail.value?.let { detail ->
                if (detail.foodItems.isEmpty()) {
                    historyRepository.delete(detail)
                    _hasUnsavedChanges.value = false // Reset flag setelah aksi
                    onComplete(true)
                } else {
                    historyRepository.update(detail)
                    originalHistoryEntity = detail.copy() // Update data asli setelah save
                    _hasUnsavedChanges.value = false // Reset flag setelah aksi
                    onComplete(false)
                }
            }
        }
    }

    fun deleteHistory() {
        viewModelScope.launch {
            _historyDetail.value?.let { historyRepository.delete(it) }
            // Tidak perlu reset flag di sini karena layar akan ditutup
        }
    }
}

