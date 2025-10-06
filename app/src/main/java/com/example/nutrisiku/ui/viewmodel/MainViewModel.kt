package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.UserRepository
import com.example.nutrisiku.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    // Inisialisasi UserRepository sesuai dengan kode Anda
    private val userRepository = UserRepository(application)

    // Variabel _isLoading dan _startDestination dari kode Anda dipertahankan
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            // Gabungkan dua data: status onboarding dan data profil
            userRepository.onboardingCompleted.combine(userRepository.userDataFlow) { onboardingCompleted, user ->
                // Tentukan layar awal berdasarkan kedua data tersebut
                if (!onboardingCompleted) {
                    // 1. Jika onboarding belum pernah selesai, tujuan PASTI ke OnboardingScreen
                    Screen.Onboarding.route
                } else if (user.name.isNotEmpty()) {
                    // 2. Jika onboarding sudah selesai DAN nama sudah diisi, tujuan ke HomeScreen
                    Screen.Home.route
                } else {
                    // 3. Jika onboarding sudah selesai TAPI nama masih kosong, tujuan ke ProfileInputScreen
                    Screen.ProfileInput.route
                }
            }.collect { destination ->
                // Simpan tujuan yang sudah ditentukan
                _startDestination.value = destination
                // Hentikan status loading
                _isLoading.value = false
            }
        }
    }

    // Fungsi ini akan kita panggil dari UI saat pengguna menyelesaikan onboarding
    fun setOnboardingCompleted() {
        viewModelScope.launch {
            userRepository.setOnboardingCompleted()
        }
    }
}