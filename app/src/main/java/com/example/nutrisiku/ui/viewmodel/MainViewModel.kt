package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisiku.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(application)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow("onboarding") // Default ke onboarding
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            // Cek apakah nama pengguna sudah tersimpan di DataStore
            val user = userRepository.userDataFlow.first()
            if (user.name.isNotEmpty()) {
                // Jika sudah ada, set layar awal ke Home
                _startDestination.value = "home"
            }
            // Setelah selesai memeriksa, berhenti loading
            _isLoading.value = false
        }
    }
}