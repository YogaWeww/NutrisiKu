package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.UserData
import com.example.nutrisiku.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State class untuk menampung semua state yang dibutuhkan oleh UI Edit Profile
data class EditProfileUiState(
    val name: String = "",
    val age: String = "",
    val weight: String = "",
    val height: String = "",
    val gender: String = "Pria",
    val activityLevel: String = "Aktivitas Ringan"
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Inisialisasi UserRepository
    private val userRepository = UserRepository(application)

    // StateFlow untuk menampung data profil yang akan diobservasi oleh UI
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        // Saat ViewModel dibuat, langsung ambil data pengguna dari DataStore
        viewModelScope.launch {
            userRepository.userDataFlow.collect { userData ->
                // Update UI state dengan data terbaru dari DataStore
                _uiState.update {
                    it.copy(
                        name = userData.name,
                        age = if (userData.age > 0) userData.age.toString() else "",
                        weight = if (userData.weight > 0) userData.weight.toString() else "",
                        height = if (userData.height > 0) userData.height.toString() else "",
                        gender = userData.gender,
                        activityLevel = userData.activityLevel
                    )
                }
            }
        }
    }

    // --- Fungsi untuk menangani perubahan input dari UI ---

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onAgeChange(newAge: String) {
        _uiState.update { it.copy(age = newAge) }
    }

    fun onWeightChange(newWeight: String) {
        _uiState.update { it.copy(weight = newWeight) }
    }

    fun onHeightChange(newHeight: String) {
        _uiState.update { it.copy(height = newHeight) }
    }

    fun onGenderChange(newGender: String) {
        _uiState.update { it.copy(gender = newGender) }
    }

    fun onActivityLevelChange(newActivityLevel: String) {
        _uiState.update { it.copy(activityLevel = newActivityLevel) }
    }

    // Fungsi untuk menyimpan data profil
    fun saveProfile() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val userData = UserData(
                name = currentState.name,
                age = currentState.age.toIntOrNull() ?: 0,
                weight = currentState.weight.toDoubleOrNull() ?: 0.0,
                height = currentState.height.toDoubleOrNull() ?: 0.0,
                gender = currentState.gender,
                activityLevel = currentState.activityLevel
            )
            userRepository.saveUserData(userData)
        }
    }
}