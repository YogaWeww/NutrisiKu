package com.example.nutrisiku.ui

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.flow.collectLatest

/**
 * Komponen root untuk seluruh aplikasi NutrisiKu.
 * Bertanggung jawab untuk inisialisasi ViewModel, pengaturan Navigasi Jetpack,
 * dan menangani event global seperti Snackbar.
 *
 * @param startDestination Rute awal yang akan ditampilkan saat aplikasi pertama kali dibuka.
 * @param modifier Modifier untuk diterapkan pada komponen root.
 * @param navController Controller untuk mengelola navigasi antar layar.
 */
@Composable
fun NutrisiKuApp(
    startDestination: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Inisialisasi ViewModelFactory untuk menyediakan dependensi ke semua ViewModel
    val application = LocalContext.current.applicationContext as Application
    val factory = ViewModelFactory(application)

    // Inisialisasi semua ViewModel yang akan digunakan di seluruh aplikasi
    val mainViewModel: MainViewModel = viewModel(factory = factory)
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)
    val detectionViewModel: DetectionViewModel = viewModel(factory = factory)
    val manualInputViewModel: ManualInputViewModel = viewModel(factory = factory)

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        detectionViewModel.events.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // --- PERBAIKAN: DEFINISIKAN AKSI NAVIGASI DI SINI ---
    val navigateToTopLevel: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Aksi terpusat untuk navigasi ke layar deteksi
    val navigateToDetection = {
        // Panggil fungsi untuk membersihkan state sesi sebelumnya
        detectionViewModel.startNewDetectionSession()
        // Lakukan navigasi
        navController.navigate(Screen.Detection.route)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)
        ) {
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

            composable(Screen.Home.route) {
                HomeScreen(
                    profileViewModel = profileViewModel,
                    historyViewModel = historyViewModel,
                    navigateToProfile = { navigateToTopLevel(Screen.Profile.route) },
                    navigateToDetection = navigateToDetection, // Gunakan aksi terpusat
                    navigateToHistory = { navigateToTopLevel(Screen.History.route) },
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
                    navigateToHome = { navigateToTopLevel(Screen.Home.route) },
                    navigateToDetection = navigateToDetection, // Gunakan aksi terpusat
                    navigateToHistory = { navigateToTopLevel(Screen.History.route) }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onBackClick = { navController.navigateUp() },
                    onHistoryItemClick = { historyId ->
                        navController.navigate(Screen.HistoryDetail.createRoute(historyId))
                    },
                    navigateToHome = { navigateToTopLevel(Screen.Home.route) },
                    navigateToDetection = navigateToDetection // Gunakan aksi terpusat
                )
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.navigateUp() },
                    onSaveClick = {
                        profileViewModel.saveUserData()
                        navController.navigateUp()
                    },
                    navigateToHome = { navigateToTopLevel(Screen.Home.route) },
                    navigateToDetection = navigateToDetection, // Gunakan aksi terpusat
                    navigateToHistory = { navigateToTopLevel(Screen.History.route) }
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
                    navigateToHome = { navigateToTopLevel(Screen.Home.route) },
                    navigateToDetection = navigateToDetection, // Gunakan aksi terpusat
                    navigateToHistory = { navigateToTopLevel(Screen.History.route) }
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
                        viewModel.updateOrDeleteHistory {
                            navController.popBackStack(Screen.History.route, inclusive = false)
                        }
                    }
                )
            }

            composable(Screen.Detection.route) {
                DetectionScreen(
                    viewModel = detectionViewModel,
                    onBackClick = { navController.navigateUp() },
                    onManualClick = { navController.navigate(Screen.ManualInput.route) },
                    navigateToResult = {
                        detectionViewModel.confirmRealtimeDetection()
                        navController.navigate(Screen.DetectionResult.route)
                    }
                )
            }

            composable(Screen.DetectionResult.route) {
                DetectionResultScreen(
                    viewModel = detectionViewModel,
                    onBackClick = { navController.navigateUp() },
                    onSaveClick = {
                        detectionViewModel.saveDetectionToHistory()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
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
                        manualInputViewModel.clearState()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

