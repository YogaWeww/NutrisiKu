package com.example.nutrisiku.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.screen.*
import com.example.nutrisiku.ui.viewmodel.*

@Composable
fun NutrisiKuApp(
    startDestination: String,
    // PERUBAHAN: Jadikan NavController sebagai parameter dengan nilai default
    navController: NavHostController = rememberNavController()
) {
    val application = LocalContext.current.applicationContext as Application
    val factory = ViewModelFactory(application)

    // Inisialisasi ViewModel tetap sama
    val mainViewModel: MainViewModel = viewModel(factory = factory)
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)
    val detectionViewModel: DetectionViewModel = viewModel(factory = factory)
    val manualInputViewModel: ManualInputViewModel = viewModel(factory = factory)

    // --- PERBAIKAN LOGIKA NAVIGASI UTAMA ---
    // Buat aksi navigasi yang bisa digunakan kembali untuk tujuan level atas
    val navigateToHome = {
        navController.navigate(Screen.Home.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val navigateToHistory = {
        navController.navigate(Screen.History.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val navigateToProfile = {
        navController.navigate(Screen.Profile.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    // Aksi navigasi lainnya tetap sama
    val navigateToDetection = {
        detectionViewModel.clearDetectionState()
        navController.navigate(Screen.Detection.route)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // Rute Onboarding dan ProfileInput tetap sama
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishClick = {
                    mainViewModel.setOnboardingCompleted()
                    navController.navigate(Screen.ProfileInput.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ProfileInput.route) {
            ProfileInputScreen(
                viewModel = profileViewModel,
                onConfirmClick = {
                    profileViewModel.saveUserData()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.ProfileInput.route) { inclusive = true }
                    }
                }
            )
        }

        // --- PERUBAHAN PADA LAYAR UTAMA ---
        composable(Screen.Home.route) {
            HomeScreen(
                profileViewModel = profileViewModel,
                historyViewModel = historyViewModel,
                navigateToProfile = navigateToProfile, // Gunakan aksi navigasi yang benar
                navigateToDetection = navigateToDetection,
                navigateToHistory = navigateToHistory, // Gunakan aksi navigasi yang benar
                // Tambahkan aksi untuk navigasi ke detail
                navigateToHistoryDetail = { historyId ->
                    navController.navigate(Screen.HistoryDetail.createRoute(historyId))
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                profileViewModel = profileViewModel,
                historyViewModel = historyViewModel,
                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                onBackClick = { navController.navigateUp() },
                navigateToHome = navigateToHome, // Gunakan aksi navigasi yang benar
                navigateToDetection = navigateToDetection,
                navigateToHistory = navigateToHistory // Gunakan aksi navigasi yang benar
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = historyViewModel,
                onBackClick = { navController.navigateUp() },
                onHistoryItemClick = { historyId ->
                    navController.navigate(Screen.HistoryDetail.createRoute(historyId))
                },
                navigateToHome = navigateToHome, // Gunakan aksi navigasi yang benar
                navigateToDetection = navigateToDetection
            )
        }

        // Rute lainnya disesuaikan untuk menggunakan aksi navigasi yang benar
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                viewModel = profileViewModel,
                onBackClick = { navController.navigateUp() },
                onSaveClick = {
                    profileViewModel.saveUserData()
                    navController.navigateUp()
                },
                navigateToHome = navigateToHome,
                navigateToDetection = navigateToDetection,
                navigateToHistory = navigateToHistory
            )
        }

        composable(
            route = Screen.HistoryDetail.route,
            arguments = listOf(navArgument("historyId") { type = NavType.IntType })
        ) {
            val viewModel: HistoryDetailViewModel = viewModel(factory = factory)
            HistoryDetailScreen(
                viewModel = viewModel,
                onBackClick = { navController.navigateUp() },
                onDeleteClick = {
                    viewModel.deleteHistory()
                    navController.popBackStack()
                },
                onEditClick = { historyId ->
                    navController.navigate(Screen.EditHistory.createRoute(historyId))
                },
                navigateToHome = navigateToHome,
                navigateToDetection = navigateToDetection,
                navigateToHistory = navigateToHistory
            )
        }

        composable(
            route = Screen.EditHistory.route,
            arguments = listOf(navArgument("historyId") { type = NavType.IntType })
        ) {
            val viewModel: HistoryDetailViewModel = viewModel(factory = factory)
            EditHistoryScreen(
                viewModel = viewModel,
                onBackClick = { navController.navigateUp() },
                onSaveClick = {
                    viewModel.updateOrDeleteHistory { wasDeleted ->
                        if (wasDeleted) {
                            navController.popBackStack(Screen.History.route, inclusive = false)
                        } else {
                            navController.navigateUp()
                        }
                    }
                }
            )
        }

        // Rute deteksi dan manual input tetap sama
        composable(Screen.Detection.route) {
            DetectionScreen(
                viewModel = detectionViewModel,
                onBackClick = { navController.navigateUp() },
                onManualClick = { navController.navigate(Screen.ManualInput.route) },
                navigateToResult = { navController.navigate(Screen.DetectionResult.route) }
            )
        }

        composable(Screen.DetectionResult.route) {
            DetectionResultScreen(
                viewModel = detectionViewModel,
                onBackClick = { navController.navigateUp() },
                onSaveClick = {
                    detectionViewModel.saveDetectionToHistory()
                    // Perbaiki navigasi setelah menyimpan
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id)
                    }
                }
            )
        }

        composable(Screen.ManualInput.route) {
            ManualInputScreen(
                viewModel = manualInputViewModel,
                onBackClick = {
                    manualInputViewModel.clearState()
                    navController.navigateUp()
                },
                onSaveSuccess = {
                    // Perbaiki navigasi setelah menyimpan
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id)
                    }
                    manualInputViewModel.clearState()
                }
            )
        }
    }
}

