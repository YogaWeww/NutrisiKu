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
import kotlin.math.roundToInt

// State class untuk menampung semua state yang dibutuhkan oleh UI Edit Profile
data class EditProfileUiState(
    val name: String = "",
    val age: String = "",
    val weight: String = "",
    val height: String = "",
    val gender: String = "Pria",
    val activityLevel: String = "Aktivitas Ringan",
    val tdee: Int = 0 // Untuk menyimpan hasil kalkulasi TDEE
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Inisialisasi UserRepository
    private val userRepository = UserRepository(application)

    // StateFlow untuk menampung data profil yang akan diobservasi oleh UI
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.userDataFlow.collect { userData ->
                // Hitung TDEE setiap kali data pengguna dimuat
                val calculatedTdee = calculateTdee(userData)
                _uiState.update {
                    it.copy(
                        name = userData.name,
                        age = if (userData.age > 0) userData.age.toString() else "",
                        weight = if (userData.weight > 0) userData.weight.toString() else "",
                        height = if (userData.height > 0) userData.height.toString() else "",
                        gender = userData.gender,
                        activityLevel = userData.activityLevel,
                        tdee = calculatedTdee // Simpan hasil TDEE ke state
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
            // TDEE akan dihitung ulang secara otomatis oleh 'collect' di init block
        }
    }

    private fun calculateTdee(userData: UserData): Int {
        if (userData.weight <= 0 || userData.height <= 0 || userData.age <= 0) {
            return 0
        }

        // Hitung BMR (Basal Metabolic Rate) menggunakan rumus Mifflin-St Jeor
        val bmr = if (userData.gender == "Pria") {
            (10 * userData.weight) + (6.25 * userData.height) - (5 * userData.age) + 5
        } else { // Wanita
            (10 * userData.weight) + (6.25 * userData.height) - (5 * userData.age) - 161
        }

        // Tentukan faktor aktivitas
        val activityFactor = when (userData.activityLevel) {
            "Jarang Olahraga" -> 1.2
            "Aktivitas Ringan" -> 1.375
            "Aktivitas Sedang" -> 1.55
            "Sangat Aktif" -> 1.725
            "Ekstra Aktif" -> 1.9
            else -> 1.2
        }

        // Hitung TDEE dan bulatkan ke integer terdekat
        return (bmr * activityFactor).roundToInt()
    }
}