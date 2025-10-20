package com.example.nutrisiku.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.UserRepository
import com.example.nutrisiku.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel utama aplikasi yang bertanggung jawab untuk logika awal aplikasi,
 * seperti menentukan layar pertama yang harus ditampilkan (onboarding atau home).
 *
 * @param userRepository Repository untuk mengakses data pengguna dan status onboarding.
 */
class MainViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            // Gabungkan dua Flow: status onboarding dan data profil pengguna.
            userRepository.onboardingCompleted.combine(userRepository.userDataFlow) { onboardingCompleted, user ->
                // Tentukan rute awal berdasarkan kondisi:
                when {
                    !onboardingCompleted -> Screen.Onboarding.route // 1. Onboarding belum selesai -> Tampilkan Onboarding
                    user.name.isNotEmpty() -> Screen.Home.route    // 2. Onboarding selesai & profil valid -> Tampilkan Home
                    else -> Screen.ProfileInput.route               // 3. Onboarding selesai & profil kosong -> Tampilkan ProfileInput
                }
            }.collect { destination ->
                _startDestination.value = destination
                _isLoading.value = false
            }
        }
    }

    /**
     * Menandai bahwa proses onboarding telah selesai.
     */
    fun setOnboardingCompleted() {
        viewModelScope.launch {
            userRepository.setOnboardingCompleted()
        }
    }
}

