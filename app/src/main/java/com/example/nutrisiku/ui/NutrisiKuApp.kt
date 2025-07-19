package com.example.nutrisiku.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.screen.EditProfileScreen
import com.example.nutrisiku.ui.screen.HistoryDetailScreen
import com.example.nutrisiku.ui.screen.HistoryScreen
import com.example.nutrisiku.ui.screen.HomeScreen
import com.example.nutrisiku.ui.screen.OnboardingScreen
import com.example.nutrisiku.ui.screen.ProfileInputScreen
import com.example.nutrisiku.ui.screen.ProfileScreen
import com.example.nutrisiku.ui.screen.SplashScreen

@Composable
fun NutrisiKuApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // NavHost adalah komponen utama yang mengatur navigasi
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route, // Aplikasi dimulai dari Splash Screen
        modifier = modifier
    ) {
        // Setiap 'composable' mendefinisikan satu layar dalam grafik navigasi
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    // Setelah timeout, hapus splash dari back stack dan pindah ke onboarding
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
                onConfirmClick = {
                    // Setelah konfirmasi profil, pindah ke Home dan bersihkan back stack
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.ProfileInput.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
//              Teruskan aksi navigasi ke HomeScreen
                navigateToProfile = { navController.navigate(Screen.Profile.route) },
                navigateToDetection = { navController.navigate(Screen.Detection.route) },
                navigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBackClick = { navController.navigateUp() },
                onSaveClick = { navController.navigateUp() } // Kembali ke profil setelah simpan
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onBackClick = { navController.navigateUp() },
                onHistoryItemClick = { historyId ->
                    // Navigasi ke detail dengan mengirim ID
                    navController.navigate(Screen.HistoryDetail.createRoute(historyId))
                }
            )
        }

        composable(
            route = Screen.HistoryDetail.route,
            arguments = listOf(navArgument("historyId") { type = NavType.IntType }),
        ) {
            // Ambil argumen ID (untuk saat ini belum digunakan, tapi strukturnya sudah siap)
            val id = it.arguments?.getInt("historyId") ?: -1
            HistoryDetailScreen(
                onBackClick = { navController.navigateUp(), },
            )
        }

        // ... Tambahkan composable untuk alur deteksi
    }
}