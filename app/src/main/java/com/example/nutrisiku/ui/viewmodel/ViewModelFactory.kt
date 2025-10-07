package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.nutrisiku.data.HistoryRepository
import com.example.nutrisiku.data.NutritionRepository
import com.example.nutrisiku.data.UserRepository

// GANTI kelas factory Anda dengan yang ini
// PERBAIKAN: Menggunakan ViewModelProvider.Factory modern, tidak lagi AbstractSavedStateViewModelFactory
class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        // Dapatkan SavedStateHandle dari extras. Ini disediakan secara otomatis oleh NavHost.
        val savedStateHandle = extras.createSavedStateHandle()

        // Inisialisasi repository seperti biasa
        val userRepository = UserRepository(application)
        val historyRepository = HistoryRepository(application)
        val nutritionRepository = NutritionRepository(application)

        // Buat instance ViewModel yang sesuai
        return when {
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(application, historyRepository) as T
            }
            modelClass.isAssignableFrom(DetectionViewModel::class.java) -> {
                DetectionViewModel(application, nutritionRepository, historyRepository) as T
            }
            modelClass.isAssignableFrom(ManualInputViewModel::class.java) -> {
                ManualInputViewModel(application, historyRepository) as T
            }
            // ViewModel ini sekarang akan menerima SavedStateHandle yang benar
            modelClass.isAssignableFrom(HistoryDetailViewModel::class.java) -> {
                HistoryDetailViewModel(application, savedStateHandle, historyRepository, nutritionRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}
