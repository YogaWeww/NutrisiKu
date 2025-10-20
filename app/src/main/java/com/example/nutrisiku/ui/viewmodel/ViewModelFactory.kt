package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.nutrisiku.data.HistoryRepository
import com.example.nutrisiku.data.NutrisiKuDatabase
import com.example.nutrisiku.data.NutritionRepository
import com.example.nutrisiku.data.UserRepository

/**
 * Factory kustom untuk membuat instance ViewModel dengan dependensi yang diperlukan.
 *
 * Menggunakan pola Dependency Injection (DI) manual untuk menyediakan repository
 * ke ViewModel yang membutuhkannya. Ini membuat ViewModel lebih mudah diuji dan
 * arsitektur aplikasi lebih bersih.
 *
 * @param application Instance Application yang digunakan untuk mendapatkan konteks
 * dan menginisialisasi database serta repository.
 */
class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()

        // Inisialisasi semua dependensi di satu tempat
        val database = NutrisiKuDatabase.getDatabase(application)
        val historyDao = database.historyDao()
        val userRepository = UserRepository(application)
        val historyRepository = HistoryRepository(historyDao)
        val nutritionRepository = NutritionRepository(application)

        // Buat instance ViewModel yang sesuai dengan kelas yang diminta,
        // dengan menyuntikkan dependensi yang benar.
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(application, userRepository) as T
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
            modelClass.isAssignableFrom(HistoryDetailViewModel::class.java) -> {
                HistoryDetailViewModel(application, savedStateHandle, historyRepository, nutritionRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}

