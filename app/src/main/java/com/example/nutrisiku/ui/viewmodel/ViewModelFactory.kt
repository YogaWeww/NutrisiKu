package com.example.nutrisiku.ui.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import com.example.nutrisiku.data.HistoryRepository
import com.example.nutrisiku.data.NutritionRepository
import com.example.nutrisiku.data.UserRepository

// GANTI kelas factory Anda dengan yang ini
class ViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val application: Application,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        val userRepository = UserRepository(application)
        val historyRepository = HistoryRepository(application)
        val nutritionRepository = NutritionRepository(application)

        return when {
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
            // TAMBAHKAN KASUS UNTUK HISTORY DETAIL VIEWMODEL
            modelClass.isAssignableFrom(HistoryDetailViewModel::class.java) -> {
                HistoryDetailViewModel(application, handle, historyRepository, nutritionRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}