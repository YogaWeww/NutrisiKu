package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.R // Pastikan R diimpor
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
 * Data class untuk merepresentasikan satu item makanan dalam formulir input manual.
 * Menggunakan String untuk input agar fleksibel di TextField.
 *
 * @property id ID unik sementara untuk membedakan item dalam daftar UI.
 * @property foodName Nama makanan yang diinput pengguna.
 * @property portion Porsi dalam gram (sebagai String).
 * @property calories Kalori per porsi (sebagai String).
 * @property quantity Jumlah item ini.
 */
data class ManualFoodItem(
    val id: Long = System.nanoTime(), // Gunakan nanoTime untuk ID yang lebih unik
    val foodName: String = "",
    val portion: String = "",
    val calories: String = "",
    val quantity: Int = 1
)

/**
 * UI State untuk layar Input Manual.
 *
 * @property foodItems Daftar item makanan yang sedang diinput.
 * @property selectedBitmap Bitmap gambar opsional yang dipilih pengguna.
 * @property errorMessage Pesan error yang akan ditampilkan di Snackbar.
 * @property isSaveSuccess Status apakah penyimpanan berhasil (untuk trigger navigasi).
 * @property sessionLabel Label sesi makan yang dipilih.
 * @property isSaveButtonEnabled Status apakah tombol simpan bisa diklik (berdasarkan validasi).
 * @property hasUnsavedChanges Menandakan apakah ada perubahan yang belum disimpan.
 */
data class ManualInputUiState(
    val foodItems: List<ManualFoodItem> = listOf(ManualFoodItem()),
    val selectedBitmap: Bitmap? = null,
    val errorMessage: String? = null,
    val isSaveSuccess: Boolean = false,
    val sessionLabel: String = "", // Akan diinisialisasi di ViewModel
    val isSaveButtonEnabled: Boolean = false,
    val hasUnsavedChanges: Boolean = false // State baru
)

/**
 * ViewModel untuk layar Input Manual.
 * Mengelola daftar item makanan yang dinamis, validasi input, dan penyimpanan ke riwayat.
 *
 * @param application Konteks aplikasi.
 * @param historyRepository Repository untuk menyimpan data riwayat.
 */
class ManualInputViewModel(
    application: Application,
    private val historyRepository: HistoryRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ManualInputUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Setel label sesi awal secara otomatis
        _uiState.update { it.copy(sessionLabel = getAutomaticSessionLabel()) }
    }

    // Fungsi helper untuk menandai ada perubahan
    private fun setHasUnsavedChanges() {
        if (!_uiState.value.hasUnsavedChanges) {
            _uiState.update { it.copy(hasUnsavedChanges = true) }
        }
    }

    /**
     * Membuang perubahan yang belum disimpan dengan mereset state ke kondisi awal.
     */
    fun discardChanges() {
        // Reset ke state awal, termasuk label sesi otomatis
        _uiState.value = ManualInputUiState(sessionLabel = getAutomaticSessionLabel())
    }

    /**
     * Memperbarui properti dari item makanan tertentu dalam daftar.
     */
    fun onFoodItemChange(index: Int, updatedItem: ManualFoodItem) {
        setHasUnsavedChanges()
        _uiState.update { currentState ->
            val updatedList = currentState.foodItems.toMutableList()
            if (index in updatedList.indices) {
                updatedList[index] = updatedItem
            }
            // Validasi ulang setelah perubahan
            val allValid = validateItems(updatedList)
            currentState.copy(
                foodItems = updatedList,
                isSaveButtonEnabled = allValid
            )
        }
    }

    /**
     * Menambah item makanan baru yang kosong ke daftar.
     */
    fun addFoodItem() {
        setHasUnsavedChanges()
        _uiState.update { currentState ->
            currentState.copy(
                foodItems = currentState.foodItems + ManualFoodItem(),
                isSaveButtonEnabled = false // Item baru pasti belum valid
            )
        }
    }

    /**
     * Menghapus item makanan dari daftar pada indeks tertentu.
     */
    fun removeFoodItem(index: Int) {
        setHasUnsavedChanges()
        _uiState.update { currentState ->
            val updatedList = currentState.foodItems.toMutableList()
            if (index in updatedList.indices) {
                updatedList.removeAt(index)
            }
            // Jika daftar menjadi kosong setelah dihapus, tambahkan satu item kosong kembali
            if (updatedList.isEmpty()) {
                updatedList.add(ManualFoodItem())
            }
            // Validasi ulang setelah penghapusan
            val allValid = validateItems(updatedList)
            currentState.copy(
                foodItems = updatedList,
                isSaveButtonEnabled = allValid
            )
        }
    }

    fun onImageSelected(bitmap: Bitmap) {
        setHasUnsavedChanges()
        _uiState.update { it.copy(selectedBitmap = bitmap) }
    }
    fun onSessionLabelChange(newLabel: String) {
        setHasUnsavedChanges()
        _uiState.update { it.copy(sessionLabel = newLabel) }
    }

    /**
     * Mereset state ViewModel ke kondisi awal (dipanggil saat keluar layar).
     */
    fun clearState() {
        _uiState.value = ManualInputUiState(sessionLabel = getAutomaticSessionLabel())
    }

    fun errorMessageShown() { _uiState.update { it.copy(errorMessage = null) } }

    /**
     * Memvalidasi dan menyimpan entri manual ke riwayat.
     */
    fun saveManualEntry() {
        val currentState = _uiState.value
        // Validasi terakhir sebelum menyimpan
        if (!validateItems(currentState.foodItems)) {
            _uiState.update { it.copy(errorMessage = getApplication<Application>().getString(R.string.manual_input_error_empty_fields)) }
            return
        }

        viewModelScope.launch {
            val imagePath = currentState.selectedBitmap?.let { saveBitmapToInternalStorage(it) } ?: ""

            val historyFoodItems = currentState.foodItems.mapNotNull { item ->
                // Konversi string ke Int, pastikan valid
                val portionInt = item.portion.toIntOrNull()
                val caloriesInt = item.calories.toIntOrNull()
                if (portionInt != null && caloriesInt != null) {
                    HistoryFoodItem(
                        name = item.foodName.trim(), // Trim spasi ekstra
                        portion = portionInt,
                        calories = caloriesInt,
                        quantity = item.quantity
                    )
                } else {
                    null // Seharusnya tidak terjadi karena sudah divalidasi, tapi sebagai pengaman
                }
            }

            // Cek lagi jika hasil konversi ada yang null (tidak mungkin jika validasi benar)
            if (historyFoodItems.size != currentState.foodItems.size) {
                _uiState.update { it.copy(errorMessage = "Terjadi kesalahan saat memproses data.") }
                return@launch
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
            // Reset state dan set flag sukses untuk trigger navigasi
            _uiState.value = ManualInputUiState(sessionLabel = getAutomaticSessionLabel(), isSaveSuccess = true)
        }
    }

    /**
     * Menyimpan bitmap ke penyimpanan internal aplikasi.
     * TODO: Pertimbangkan memindahkan ini ke Repository atau UseCase jika digunakan di tempat lain.
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
     * Memvalidasi semua item makanan dalam daftar.
     * @return true jika semua item valid, false jika ada yang tidak valid.
     */
    private fun validateItems(items: List<ManualFoodItem>): Boolean {
        if (items.isEmpty()) return false // Tidak bisa menyimpan jika kosong
        return items.all {
            it.foodName.isNotBlank() &&
                    (it.portion.toIntOrNull() ?: 0) > 0 &&
                    (it.calories.toIntOrNull() ?: 0) > 0
        }
    }

    /**
     * Mendapatkan label sesi makan otomatis berdasarkan waktu saat ini.
     */
    private fun getAutomaticSessionLabel(): String {
        val cal = Calendar.getInstance()
        val context = getApplication<Application>()
        return when (cal.get(Calendar.HOUR_OF_DAY)) {
            in 0..10 -> context.getString(R.string.session_breakfast)
            in 11..15 -> context.getString(R.string.session_lunch)
            in 16..20 -> context.getString(R.string.session_dinner)
            else -> context.getString(R.string.session_snack)
        }
    }
}

