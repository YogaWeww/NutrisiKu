package com.example.nutrisiku.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.UserData
import com.example.nutrisiku.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val name: String = "",
    val age: String = "",
    val weight: String = "",
    val height: String = "",
    val gender: String = "Pria",
    val activityLevel: String = "Aktivitas Ringan",
    val imagePath: String = "",
    val tdee: Int = 0,
    val isConfirmButtonEnabled: Boolean = false,
    // --- STATE BARU UNTUK MENGONTROL MENU OPSI ---
    val showProfileImageOptions: Boolean = false
)

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.userDataFlow.collect { userData ->
                _uiState.update {
                    it.copy(
                        name = userData.name,
                        age = if (userData.age > 0) userData.age.toString() else "",
                        weight = if (userData.weight > 0) userData.weight.toString() else "",
                        height = if (userData.height > 0) userData.height.toString() else "",
                        gender = userData.gender,
                        activityLevel = userData.activityLevel,
                        imagePath = userData.imagePath,
                        tdee = userData.tdee.toInt()
                    )
                }
                validateState()
            }
        }
    }

    fun loadInitialData() {
        viewModelScope.launch {
            val savedUserData = userRepository.userDataFlow.first()
            _uiState.update {
                it.copy(
                    name = savedUserData.name,
                    age = if (savedUserData.age > 0) savedUserData.age.toString() else "",
                    weight = if (savedUserData.weight > 0) savedUserData.weight.toString() else "",
                    height = if (savedUserData.height > 0) savedUserData.height.toString() else "",
                    gender = savedUserData.gender,
                    activityLevel = savedUserData.activityLevel,
                    imagePath = savedUserData.imagePath,
                    tdee = savedUserData.tdee.toInt()
                )
            }
            validateState()
        }
    }

    fun onNameChange(name: String) { _uiState.update { it.copy(name = name) }; validateState() }
    fun onAgeChange(age: String) { _uiState.update { it.copy(age = age) }; validateState() }
    fun onWeightChange(weight: String) { _uiState.update { it.copy(weight = weight) }; validateState() }
    fun onHeightChange(height: String) { _uiState.update { it.copy(height = height) }; validateState() }
    fun onGenderChange(gender: String) { _uiState.update { it.copy(gender = gender) } }
    fun onActivityLevelChange(activityLevel: String) { _uiState.update { it.copy(activityLevel = activityLevel) } }

    fun onProfileImageChanged(bitmap: Bitmap) {
        viewModelScope.launch {
            val imagePath = userRepository.saveProfilePicture(bitmap)
            if (imagePath != null) {
                _uiState.update { it.copy(imagePath = imagePath) }
                saveUserData()
            }
        }
    }

    // --- FUNGSI-FUNGSI BARU UNTUK MENU OPSI ---
    fun onProfileImageClicked() {
        _uiState.update { it.copy(showProfileImageOptions = true) }
    }

    fun onDismissProfileImageOptions() {
        _uiState.update { it.copy(showProfileImageOptions = false) }
    }

    fun onDeleteProfileImage() {
        viewModelScope.launch {
            userRepository.deleteProfilePicture()
            onDismissProfileImageOptions() // Tutup menu setelah menghapus
        }
    }

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

    private fun calculateTdee(weight: Float, height: Float, age: Int, gender: String, activityLevel: String): Float {
        if (weight <= 0f || height <= 0f || age <= 0) return 0f

        val bmr = if (gender == "Pria") {
            (10 * weight) + (6.25f * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25f * height) - (5 * age) - 161
        }

        val activityMultiplier = when (activityLevel) {
            "Jarang Olahraga" -> 1.2f
            "Aktivitas Ringan" -> 1.375f
            "Aktivitas Sedang" -> 1.55f
            "Sangat Aktif" -> 1.725f
            "Ekstra Aktif" -> 1.9f
            else -> 1.2f
        }

        return bmr * activityMultiplier
    }

    private fun validateState() {
        val currentState = _uiState.value
        val isDataValid = currentState.name.isNotBlank() &&
                currentState.age.isNotBlank() &&
                currentState.weight.isNotBlank() &&
                currentState.height.isNotBlank()

        _uiState.update {
            it.copy(isConfirmButtonEnabled = isDataValid)
        }
    }
}

