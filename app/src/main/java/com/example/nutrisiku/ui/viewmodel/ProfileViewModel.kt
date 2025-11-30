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
import kotlinx.coroutines.flow.first // Import first() operator
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
 * @property hasUnsavedChanges Menandakan apakah ada perubahan yang belum disimpan.
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
    val showProfileImageOptions: Boolean = false,
    val hasUnsavedChanges: Boolean = false
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
        // Langsung muat data awal dan mulai berlangganan perubahan
        reloadDataFromRepository()
        observeRepositoryChanges()
    }

    // Fungsi terpisah untuk mengamati perubahan dari repository
    private fun observeRepositoryChanges() {
        viewModelScope.launch {
            userRepository.userDataFlow.collect { userData ->
                // Hanya update jika tidak ada perubahan lokal yang belum disimpan
                // Ini mencegah data yang sedang diedit tertimpa oleh update dari repo
                // (kecuali saat discardChanges secara eksplisit memanggil reload)
                if (!_uiState.value.hasUnsavedChanges) {
                    updateStateFromUserData(userData)
                }
            }
        }
    }

    /**
     * Memuat data terbaru dari repository dan memperbarui state UI.
     * Juga mereset flag hasUnsavedChanges.
     */
    private fun reloadDataFromRepository() {
        viewModelScope.launch {
            val userData = userRepository.userDataFlow.first() // Ambil nilai terakhir yang disimpan
            updateStateFromUserData(userData)
        }
    }

    /**
     * Helper untuk memperbarui state UI dari objek UserData.
     */
    private fun updateStateFromUserData(userData: UserData) {
        _uiState.update { currentState ->
            val defaultGender = application.getString(R.string.gender_male)
            val defaultActivity = application.getString(R.string.activity_level_2)

            val newState = currentState.copy(
                name = userData.name,
                age = if (userData.age > 0) userData.age.toString() else "",
                weight = if (userData.weight > 0) userData.weight.toString() else "",
                height = if (userData.height > 0) userData.height.toString() else "",
                gender = userData.gender.ifEmpty { defaultGender },
                activityLevel = userData.activityLevel.ifEmpty { defaultActivity },
                imagePath = userData.imagePath,
                tdee = userData.tdee.toInt(),
                hasUnsavedChanges = false // Selalu reset saat memuat ulang dari repo
            )
            newState.copy(isConfirmButtonEnabled = validateInput(
                name = newState.name,
                age = newState.age,
                weight = newState.weight,
                height = newState.height
            ))
        }
    }


    // Fungsi helper untuk menandai ada perubahan
    private fun setHasUnsavedChanges() {
        if (!_uiState.value.hasUnsavedChanges) {
            _uiState.update { it.copy(hasUnsavedChanges = true) }
        }
    }

    /**
     * Membuang perubahan yang belum disimpan dengan memuat ulang data dari repository.
     */
    fun discardChanges() {
        // Cukup panggil reload, karena reload akan otomatis mereset flag dan state
        reloadDataFromRepository()
    }


    // --- Handler untuk perubahan input dari UI ---
    // Sekarang setiap handler juga memanggil setHasUnsavedChanges()

    fun onNameChange(name: String) {
        setHasUnsavedChanges()
        _uiState.update {
            val isValid = validateInput(name = name, age = it.age, weight = it.weight, height = it.height)
            it.copy(name = name, isConfirmButtonEnabled = isValid)
        }
    }
    fun onAgeChange(age: String) {
        setHasUnsavedChanges()
        _uiState.update {
            val isValid = validateInput(name = it.name, age = age, weight = it.weight, height = it.height)
            it.copy(age = age, isConfirmButtonEnabled = isValid)
        }
    }
    fun onWeightChange(weight: String) {
        setHasUnsavedChanges()
        _uiState.update {
            val isValid = validateInput(name = it.name, age = it.age, weight = weight, height = it.height)
            it.copy(weight = weight, isConfirmButtonEnabled = isValid)
        }
    }
    fun onHeightChange(height: String) {
        setHasUnsavedChanges()
        _uiState.update {
            val isValid = validateInput(name = it.name, age = it.age, weight = it.weight, height = height)
            it.copy(height = height, isConfirmButtonEnabled = isValid)
        }
    }
    fun onGenderChange(gender: String) {
        setHasUnsavedChanges()
        _uiState.update { it.copy(gender = gender) }
    }
    fun onActivityLevelChange(activityLevel: String) {
        setHasUnsavedChanges()
        _uiState.update { it.copy(activityLevel = activityLevel) }
    }

    fun onProfileImageChanged(bitmap: Bitmap) {
        setHasUnsavedChanges()
        viewModelScope.launch {
            val imagePath = userRepository.saveProfilePicture(bitmap)
            // SaveUserdata akan dipanggil setelah gambar disimpan
            if (imagePath != null) {
                _uiState.update { it.copy(imagePath = imagePath) }
                // Langsung simpan perubahan path gambar ke DataStore
                saveUserData()
            }
        }
    }

    fun onDeleteProfileImage() {
        setHasUnsavedChanges()
        viewModelScope.launch {
            // Kita panggil saveUserData dulu untuk menyimpan path kosong sblm menghapus
            saveUserData(resetUnsavedFlag = false) // Simpan path kosong
            userRepository.deleteProfilePicture() // Ini akan trigger collect lagi dan memuat ulang state
            onDismissProfileImageOptions()
        }
    }

    fun onProfileImageClicked() { _uiState.update { it.copy(showProfileImageOptions = true) } }
    fun onDismissProfileImageOptions() { _uiState.update { it.copy(showProfileImageOptions = false) } }

    /**
     * Menghitung TDEE dan menyimpan seluruh data pengguna ke repository.
     * @param resetUnsavedFlag Apakah flag hasUnsavedChanges harus direset setelah menyimpan.
     */
    fun saveUserData(resetUnsavedFlag: Boolean = true) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val weightKg = currentState.weight.toDoubleOrNull() ?: 0.0
            val heightCm = currentState.height.toDoubleOrNull() ?: 0.0
            val ageYears = currentState.age.toIntOrNull() ?: 0

            val calculatedTdee = calculateTdee(
                weight = weightKg.toFloat(),
                height = heightCm.toFloat(),
                age = ageYears,
                gender = currentState.gender,
                activityLevel = currentState.activityLevel
            )
            userRepository.saveUserData(
                UserData(
                    name = currentState.name,
                    age = ageYears,
                    weight = weightKg,
                    height = heightCm,
                    gender = currentState.gender,
                    activityLevel = currentState.activityLevel,
                    imagePath = currentState.imagePath,
                    tdee = calculatedTdee
                )
            )
            // Jika reset flag diminta, panggil reload agar state sinkron
            // Jika tidak (misal saat ganti gambar), biarkan collector yang handle
            if (resetUnsavedFlag) {
                reloadDataFromRepository() // Ini akan mereset hasUnsavedChanges
            }
        }
    }

    // Fungsi calculateTdee dan validateInput tetap sama
    private fun calculateTdee(weight: Float, height: Float, age: Int, gender: String, activityLevel: String): Float {
        if (weight <= 0f || height <= 0f || age <= 0) return 0f
        val bmr = if (gender == application.getString(R.string.gender_male)) {
            (10 * weight) + (6.25f * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25f * height) - (5 * age) - 161
        }
        val activityMultiplier = when (activityLevel) {
            application.getString(R.string.activity_level_1) -> 1.2f
            application.getString(R.string.activity_level_2) -> 1.375f
            application.getString(R.string.activity_level_3) -> 1.55f
            application.getString(R.string.activity_level_4) -> 1.725f
            application.getString(R.string.activity_level_5) -> 1.9f
            else -> 1.375f
        }
        return bmr * activityMultiplier
    }

    private fun validateInput(name: String, age: String, weight: String, height: String): Boolean {
        return name.isNotBlank() &&
                (age.toIntOrNull() ?: 0) > 0 &&
                (weight.toDoubleOrNull() ?: 0.0) > 0 &&
                (height.toDoubleOrNull() ?: 0.0) > 0
    }
}

