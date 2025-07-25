package com.example.nutrisiku.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.screen.CameraScreen
import com.example.nutrisiku.ui.screen.DetectionResultScreen
import com.example.nutrisiku.ui.screen.DetectionScreen
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
import com.example.nutrisiku.ui.viewmodel.ProfileViewModel

@Composable
fun NutrisiKuApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Buat instance ViewModel yang akan dibagikan ke beberapa layar
    val profileViewModel: ProfileViewModel = viewModel()
    // Buat instance DetectionViewModel yang akan dibagikan
    val detectionViewModel: DetectionViewModel = viewModel()
    // Buat instance HistoryViewModel
    val historyViewModel: HistoryViewModel = viewModel()

    val historyDetailViewModel: HistoryDetailViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
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
                viewModel = profileViewModel, // Teruskan ViewModel ke HomeScreen
                navigateToProfile = { navController.navigate(Screen.Profile.route) },
                navigateToDetection = { navController.navigate(Screen.Detection.route) },
                navigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = profileViewModel, // Teruskan ViewModel
                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                onBackClick = { navController.navigateUp() },
                // Menambahkan parameter navigasi untuk BottomNavBar
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
            arguments = listOf(navArgument("historyId") { type = NavType.IntType }),
        ) {
            HistoryDetailScreen(
                viewModel = historyDetailViewModel,
                onBackClick = { navController.navigateUp() },
                // Menambahkan parameter navigasi untuk BottomNavBar
                navigateToHome = { navController.navigate(Screen.Home.route) },
                navigateToDetection = { navController.navigate(Screen.Detection.route) },
                navigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.Detection.route) {
            DetectionScreen(
                viewModel = detectionViewModel, // Teruskan ViewModel
                onBackClick = { navController.navigateUp() },
                // Navigasi ke hasil setelah gambar dipilih
                navigateToResult = { navController.navigate(Screen.DetectionResult.route) }
            )
        }

        composable(Screen.DetectionResult.route) {
            DetectionResultScreen(
                viewModel = detectionViewModel, // Teruskan ViewModel
                onBackClick = { navController.navigateUp() },
                onSaveClick = {
                    // TODO: Simpan ke riwayat
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ManualInput.route) {
            ManualInputScreen(
                onBackClick = { navController.navigateUp() },
                onSaveClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Detection.route) {
            DetectionScreen(
                viewModel = detectionViewModel,
                onBackClick = { navController.navigateUp() },
                // PERUBAHAN: Arahkan ke layar kamera baru
                onCameraClick = { navController.navigate(Screen.Camera.route) },
                navigateToResult = { navController.navigate(Screen.DetectionResult.route) }
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