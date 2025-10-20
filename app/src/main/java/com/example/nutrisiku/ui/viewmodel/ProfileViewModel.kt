package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.R
import com.example.nutrisiku.data.UserData
import com.example.nutrisiku.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State untuk layar Profil dan Edit Profil.
 * Menggunakan String untuk input agar fleksibel di TextField.
 *
 * @property name Nama pengguna.
 * @property age Usia pengguna (sebagai String).
 * @property weight Berat badan pengguna (sebagai String).
 * @property height Tinggi badan pengguna (sebagai String).
 * @property gender Jenis kelamin pengguna.
 * @property activityLevel Tingkat aktivitas harian.
 * @property imagePath Path lokal ke gambar profil.
 * @property tdee Total Daily Energy Expenditure (kebutuhan kalori harian).
 * @property isConfirmButtonEnabled Status apakah tombol konfirmasi/simpan bisa diklik.
 * @property showProfileImageOptions Status untuk menampilkan/menyembunyikan menu opsi gambar profil.
 */
data class EditProfileUiState(
    val name: String = "",
    val age: String = "",
    val weight: String = "",
    val height: String = "",
    val gender: String = "",
    val activityLevel: String = "",
    val imagePath: String = "",
    val tdee: Int = 0,
    val isConfirmButtonEnabled: Boolean = false,
    val showProfileImageOptions: Boolean = false
)

/**
 * ViewModel untuk mengelola data dan logika di ProfileScreen dan EditProfileScreen.
 *
 * @param application Konteks aplikasi, diperlukan untuk mengakses string resources.
 * @param userRepository Repository untuk mengakses data pengguna.
 */
class ProfileViewModel(
    private val application: Application,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        // Langsung berlangganan perubahan data dari repository.
        // Blok ini akan otomatis memperbarui UI setiap kali data pengguna di DataStore berubah.
        viewModelScope.launch {
            userRepository.userDataFlow.collect { userData ->
                _uiState.update {
                    it.copy(
                        name = userData.name,
                        age = if (userData.age > 0) userData.age.toString() else "",
                        weight = if (userData.weight > 0) userData.weight.toString() else "",
                        height = if (userData.height > 0) userData.height.toString() else "",
                        gender = userData.gender.ifEmpty { application.getString(R.string.gender_male) },
                        activityLevel = userData.activityLevel.ifEmpty { application.getString(R.string.activity_level_2) },
                        imagePath = userData.imagePath,
                        tdee = userData.tdee.toInt()
                    )
                }
                // Validasi state setiap kali data dari repository berubah.
                validateState()
            }
        }
    }

    // --- Handler untuk perubahan input dari UI ---

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
        validateState()
    }
    fun onAgeChange(age: String) {
        _uiState.update { it.copy(age = age) }
        validateState()
    }
    fun onWeightChange(weight: String) {
        _uiState.update { it.copy(weight = weight) }
        validateState()
    }
    fun onHeightChange(height: String) {
        _uiState.update { it.copy(height = height) }
        validateState()
    }
    fun onGenderChange(gender: String) {
        _uiState.update { it.copy(gender = gender) }
    }
    fun onActivityLevelChange(activityLevel: String) {
        _uiState.update { it.copy(activityLevel = activityLevel) }
    }

    /**
     * Menangani logika saat gambar profil diubah oleh pengguna.
     */
    fun onProfileImageChanged(bitmap: Bitmap) {
        viewModelScope.launch {
            val imagePath = userRepository.saveProfilePicture(bitmap)
            if (imagePath != null) {
                _uiState.update { it.copy(imagePath = imagePath) }
                // Langsung simpan perubahan path gambar ke DataStore
                saveUserData()
            }
        }
    }

    // --- Handler untuk menu opsi gambar profil ---

    fun onProfileImageClicked() { _uiState.update { it.copy(showProfileImageOptions = true) } }
    fun onDismissProfileImageOptions() { _uiState.update { it.copy(showProfileImageOptions = false) } }
    fun onDeleteProfileImage() {
        viewModelScope.launch {
            userRepository.deleteProfilePicture()
            onDismissProfileImageOptions()
        }
    }

    /**
     * Menghitung TDEE dan menyimpan seluruh data pengguna ke repository.
     */
    fun saveUserData() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val calculatedTdee = calculateTdee(
                weight = currentState.weight.toFloatOrNull() ?: 0f,
                height = currentState.height.toFloatOrNull() ?: 0f,
                age = currentState.age.toIntOrNull() ?: 0,
                gender = currentState.gender,
                activityLevel = currentState.activityLevel
            )
            userRepository.saveUserData(
                UserData(
                    name = currentState.name,
                    age = currentState.age.toIntOrNull() ?: 0,
                    weight = currentState.weight.toDoubleOrNull() ?: 0.0,
                    height = currentState.height.toDoubleOrNull() ?: 0.0,
                    gender = currentState.gender,
                    activityLevel = currentState.activityLevel,
                    imagePath = currentState.imagePath,
                    tdee = calculatedTdee
                )
            )
        }
    }

    /**
     * Menghitung Total Daily Energy Expenditure (TDEE) berdasarkan data pengguna.
     * RUMUS: Mifflin-St Jeor
     */
    private fun calculateTdee(weight: Float, height: Float, age: Int, gender: String, activityLevel: String): Float {
        if (weight <= 0f || height <= 0f || age <= 0) return 0f

        val bmr = if (gender == application.getString(R.string.gender_male)) {
            (10 * weight) + (6.25f * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25f * height) - (5 * age) - 161
        }

        // PERBAIKAN: Gunakan string resources untuk perbandingan yang tangguh.
        val activityMultiplier = when (activityLevel) {
            application.getString(R.string.activity_level_1) -> 1.2f
            application.getString(R.string.activity_level_2) -> 1.375f
            application.getString(R.string.activity_level_3) -> 1.55f
            application.getString(R.string.activity_level_4) -> 1.725f
            application.getString(R.string.activity_level_5) -> 1.9f
            else -> 1.2f
        }

        return bmr * activityMultiplier
    }

    /**
     * Memvalidasi state saat ini untuk menentukan apakah tombol simpan/konfirmasi harus aktif.
     */
    private fun validateState() {
        _uiState.update { currentState ->
            val isDataValid = currentState.name.isNotBlank() &&
                    (currentState.age.toIntOrNull() ?: 0) > 0 &&
                    (currentState.weight.toDoubleOrNull() ?: 0.0) > 0 &&
                    (currentState.height.toDoubleOrNull() ?: 0.0) > 0
            currentState.copy(isConfirmButtonEnabled = isDataValid)
        }
    }
}
