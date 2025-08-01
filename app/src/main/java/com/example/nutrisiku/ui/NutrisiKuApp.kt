package com.example.nutrisiku.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.SavedStateRegistryOwner
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.screen.CameraScreen
import com.example.nutrisiku.ui.screen.DetectionResultScreen
import com.example.nutrisiku.ui.screen.DetectionScreen
import com.example.nutrisiku.ui.screen.EditHistoryScreen
import com.example.nutrisiku.ui.screen.EditProfileScreen
import com.example.nutrisiku.ui.screen.HistoryDetailScreen
import com.example.nutrisiku.ui.screen.HistoryScreen
import com.example.nutrisiku.ui.screen.HomeScreen
import com.example.nutrisiku.ui.screen.ManualInputScreen
import com.example.nutrisiku.ui.screen.OnboardingScreen
import com.example.nutrisiku.ui.screen.ProfileInputScreen
import com.example.nutrisiku.ui.screen.ProfileScreen
import com.example.nutrisiku.ui.screen.SplashScreen
import com.example.nutrisiku.ui.viewmodel.DetectionViewModel
import com.example.nutrisiku.ui.viewmodel.HistoryDetailViewModel
import com.example.nutrisiku.ui.viewmodel.HistoryViewModel
import com.example.nutrisiku.ui.viewmodel.ManualInputViewModel
import com.example.nutrisiku.ui.viewmodel.ProfileViewModel
import com.example.nutrisiku.ui.viewmodel.ViewModelFactory


@Composable
fun NutrisiKuApp(
    startDestination: String // Terima start destination sebagai parameter
) {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as Application

    // Dapatkan viewModelStoreOwner dari komposisi lokal
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided by LocalViewModelStoreOwner"
    }

    // SOLUSI: Cast owner ke SavedStateRegistryOwner.
    // Di level aplikasi, owner ini adalah Activity, yang mengimplementasikan interface ini.
    val factory = ViewModelFactory(viewModelStoreOwner as SavedStateRegistryOwner, application)

    // Gunakan factory yang sama untuk semua ViewModel
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)
    val historyViewModel: HistoryViewModel = viewModel(factory = factory)
    val detectionViewModel: DetectionViewModel = viewModel(factory = factory)
    val manualInputViewModel: ManualInputViewModel = viewModel(factory = factory)

    NavHost(
        navController = navController,
        startDestination = startDestination, // Gunakan start destination dari MainActivity
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishClick = {
                    navController.navigate(Screen.ProfileInput.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ProfileInput.route) {
            ProfileInputScreen(
                // Teruskan ViewModel ke layar
                viewModel = profileViewModel,
                onConfirmClick = {
                    // Panggil fungsi simpan di ViewModel sebelum navigasi
                    profileViewModel.saveProfile()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.ProfileInput.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                profileViewModel = profileViewModel, // Teruskan ProfileViewModel
                historyViewModel = historyViewModel, // Teruskan HistoryViewModel
                navigateToProfile = { navController.navigate(Screen.Profile.route) },
                navigateToDetection = { navController.navigate(Screen.Detection.route) },
                navigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                profileViewModel = profileViewModel, // Teruskan ProfileViewModel
                historyViewModel = historyViewModel, // PERUBAHAN: Teruskan HistoryViewModel
                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                onBackClick = { navController.navigateUp() },
                navigateToHome = { navController.navigate(Screen.Home.route) },
                navigateToDetection = { navController.navigate(Screen.Detection.route) },
                navigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                viewModel = profileViewModel, // Teruskan ViewModel
                onBackClick = { navController.navigateUp() },
                onSaveClick = {
                    profileViewModel.saveProfile() // Panggil fungsi simpan
                    navController.navigateUp() // Kembali ke profil
                },
                // Menambahkan parameter navigasi untuk BottomNavBar
                navigateToHome = { navController.navigate(Screen.Home.route) },
                navigateToDetection = { navController.navigate(Screen.Detection.route) },
                navigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = historyViewModel, // Teruskan ViewModel
                onBackClick = { navController.navigateUp() },
                onHistoryItemClick = { historyId ->
                    navController.navigate(Screen.HistoryDetail.createRoute(historyId))
                },
                // Menambahkan parameter navigasi untuk BottomNavBar
                navigateToHome = { navController.navigate(Screen.Home.route) },
                navigateToDetection = { navController.navigate(Screen.Detection.route) }
            )
        }

        composable(
            route = Screen.HistoryDetail.route,
            arguments = listOf(navArgument("historyId") { type = NavType.IntType })
        ) { backStackEntry ->
            // Sekarang kita bisa menggunakan factory yang sama karena sudah di-setup dengan benar
            val viewModel: HistoryDetailViewModel = viewModel(factory = factory)

            HistoryDetailScreen(
                viewModel = viewModel,
                onBackClick = { navController.navigateUp() },
                onDeleteClick = {
                    viewModel.deleteHistory()
                    navController.popBackStack()
                },
                onEditClick = { historyId ->
                    // Navigasi ke layar edit baru
                    navController.navigate(Screen.EditHistory.createRoute(historyId))
                },
                navigateToHome = { navController.navigate(Screen.Home.route) },
                navigateToDetection = { navController.navigate(Screen.Detection.route) },
                navigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }

        composable(
            route = Screen.EditHistory.route,
            arguments = listOf(navArgument("historyId") { type = NavType.IntType })
        ) { backStackEntry ->
            // Gunakan factory yang sama di sini juga
            val viewModel: HistoryDetailViewModel = viewModel(factory = factory)
            EditHistoryScreen(
                viewModel = viewModel,
                onBackClick = { navController.navigateUp() },
                onSaveClick = {
                    viewModel.updateHistory()
                    navController.popBackStack(Screen.History.route, false) // Kembali ke daftar riwayat
                }
            )
        }

        composable(Screen.Detection.route) {
            DetectionScreen(
                viewModel = detectionViewModel,
                onBackClick = { navController.navigateUp() },
                onCameraClick = { navController.navigate(Screen.Camera.route) },
                onManualClick = { navController.navigate(Screen.ManualInput.route) },
                navigateToResult = { navController.navigate(Screen.DetectionResult.route) }
            )
        }

        composable(Screen.DetectionResult.route) {
            DetectionResultScreen(
                viewModel = detectionViewModel, // Teruskan ViewModel
                onBackClick = { navController.navigateUp() },
                onSaveClick = {
                    detectionViewModel.saveDetectionToHistory() // Panggil fungsi simpan
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
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
                onSaveClick = {
                    // Navigasi terjadi di sini setelah ViewModel mengkonfirmasi sukses
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                    // Bersihkan state setelah navigasi
                    manualInputViewModel.clearState()
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                viewModel = detectionViewModel,
                navigateToResult = {
                    // Kembali ke hasil setelah foto diambil
                    navController.navigate(Screen.DetectionResult.route) {
                        // Hapus layar kamera dari back stack
                        popUpTo(Screen.Camera.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
