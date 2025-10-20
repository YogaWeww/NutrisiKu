package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.R
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

/**
 * Data class untuk merepresentasikan satu item makanan dalam daftar input manual.
 * Menggunakan String untuk input pengguna agar lebih fleksibel sebelum divalidasi.
 *
 * @property id ID unik untuk setiap item, berguna untuk key di LazyColumn.
 * @property foodName Nama makanan yang diinput pengguna.
 * @property portion Porsi dalam gram (sebagai String).
 * @property calories Kalori per porsi (sebagai String).
 * @property quantity Jumlah item ini.
 */
data class ManualFoodItem(
    val id: Long = System.currentTimeMillis(),
    val foodName: String = "",
    val portion: String = "",
    val calories: String = "",
    val quantity: Int = 1
)

/**
 * UI State untuk layar Input Manual.
 *
 * @property foodItems Daftar item makanan yang sedang diinput oleh pengguna.
 * @property selectedBitmap Bitmap gambar yang dipilih (opsional).
 * @property errorMessage Pesan eror untuk ditampilkan di Snackbar.
 * @property isSaveSuccess Status apakah penyimpanan berhasil, untuk memicu navigasi.
 * @property sessionLabel Label sesi makan yang dipilih.
 * @property isSaveButtonEnabled Status apakah tombol simpan bisa diklik (berdasarkan validasi).
 */
data class ManualInputUiState(
    val foodItems: List<ManualFoodItem> = listOf(ManualFoodItem()),
    val selectedBitmap: Bitmap? = null,
    val errorMessage: String? = null,
    val isSaveSuccess: Boolean = false,
    val sessionLabel: String = "",
    val isSaveButtonEnabled: Boolean = false
)

/**
 * ViewModel untuk layar Input Manual.
 * Mengelola state dari daftar makanan yang diinput, validasi, dan proses penyimpanan.
 *
 * @param application Konteks aplikasi.
 * @param historyRepository Repository untuk menyimpan data ke database riwayat.
 */
class ManualInputViewModel(
    private val application: Application,
    private val historyRepository: HistoryRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ManualInputUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(sessionLabel = getAutomaticSessionLabel()) }
    }

    /**
     * Memperbarui properti dari item makanan tertentu berdasarkan indeks.
     */
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

    /**
     * Menambah item makanan baru yang kosong ke dalam daftar.
     */
    fun addFoodItem() {
        _uiState.update { currentState ->
            currentState.copy(foodItems = currentState.foodItems + ManualFoodItem())
        }
        validateState()
    }

    /**
     * Menghapus item makanan dari daftar berdasarkan indeks.
     */
    fun removeFoodItem(index: Int) {
        _uiState.update { currentState ->
            val updatedList = currentState.foodItems.toMutableList()
            if (index in updatedList.indices && updatedList.size > 1) {
                updatedList.removeAt(index)
            }
            currentState.copy(foodItems = updatedList)
        }
        validateState()
    }

    fun onImageSelected(bitmap: Bitmap) { _uiState.update { it.copy(selectedBitmap = bitmap) } }
    fun onSessionLabelChange(newLabel: String) { _uiState.update { it.copy(sessionLabel = newLabel) } }
    fun errorMessageShown() { _uiState.update { it.copy(errorMessage = null) } }

    /**
     * Me-reset state ViewModel ke kondisi awal.
     * Dipanggil setelah navigasi keluar dari layar ini.
     */
    fun clearState() {
        _uiState.value = ManualInputUiState(sessionLabel = getAutomaticSessionLabel())
    }

    /**
     * Memvalidasi dan menyimpan entri manual ke database riwayat.
     */
    fun saveManualEntry() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (!currentState.isSaveButtonEnabled) return@launch

            val imagePath = currentState.selectedBitmap?.let { saveBitmapToInternalStorage(it) } ?: ""

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
                imagePath = imagePath,
                totalCalories = totalCalories,
                foodItems = historyFoodItems
            )

            historyRepository.insert(historyEntity)
            _uiState.update { it.copy(isSaveSuccess = true) }
        }
    }

    /**
     * Menyimpan Bitmap ke penyimpanan internal aplikasi.
     * CATATAN: Fungsi ini duplikat dengan yang ada di DetectionViewModel.
     * Kandidat yang baik untuk di-refactor ke sebuah kelas utility atau repository terpisah.
     */
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

    /**
     * Memvalidasi state saat ini untuk menentukan apakah tombol simpan harus aktif.
     */
    private fun validateState() {
        _uiState.update { currentState ->
            val allItemsValid = currentState.foodItems.all {
                it.foodName.isNotBlank() &&
                        (it.portion.toIntOrNull() ?: 0) > 0 &&
                        (it.calories.toIntOrNull() ?: 0) > 0
            }
            currentState.copy(isSaveButtonEnabled = allItemsValid)
        }
    }

    /**
     * Menentukan label sesi makan secara otomatis berdasarkan waktu saat ini.
     */
    private fun getAutomaticSessionLabel(): String {
        val resources = getApplication<Application>().resources
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..10 -> resources.getString(R.string.session_breakfast)
            in 11..15 -> resources.getString(R.string.session_lunch)
            in 16..20 -> resources.getString(R.string.session_dinner)
            else -> resources.getString(R.string.session_snack)
        }
    }
}

