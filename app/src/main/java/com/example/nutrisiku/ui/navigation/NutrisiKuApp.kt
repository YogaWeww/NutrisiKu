package com.example.nutrisiku.ui

import android.app.Application
// --- PENAMBAHAN: Impor untuk animasi ---
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
// --- Akhir Penambahan ---
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
 * menangani event global seperti Snackbar, dan menambahkan animasi transisi antar layar.
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
    val application = LocalContext.current.applicationContext as Application
    val factory = ViewModelFactory(application)

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

    val navigateToTopLevel: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateToDetection = {
        detectionViewModel.startNewDetectionSession()
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
            // --- PENAMBAHAN ANIMASI UNTUK SETIAP LAYAR ---
            val enterFade = fadeIn(animationSpec = tween(300))
            val exitFade = fadeOut(animationSpec = tween(300))
            // Contoh animasi slide untuk layar detail (opsional)
            val enterSlide = slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300))
            val exitSlide = slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300))
            val popEnterSlide = slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300))
            val popExitSlide = slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300))


            composable(
                Screen.Onboarding.route,
                enterTransition = { enterFade },
                exitTransition = { exitFade }
            ) {
                OnboardingScreen(
                    onFinishClick = {
                        mainViewModel.setOnboardingCompleted()
                        navController.navigate(Screen.ProfileInput.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                Screen.ProfileInput.route,
                enterTransition = { enterFade },
                exitTransition = { exitFade }
            ) {
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

            composable(
                Screen.Home.route,
                enterTransition = { enterFade },
                exitTransition = { exitFade }
            ) {
                HomeScreen(
                    profileViewModel = profileViewModel,
                    historyViewModel = historyViewModel,
                    navigateToProfile = { navigateToTopLevel(Screen.Profile.route) },
                    navigateToDetection = navigateToDetection,
                    navigateToHistory = { navigateToTopLevel(Screen.History.route) },
                    navigateToHistoryDetail = { historyId ->
                        navController.navigate(Screen.HistoryDetail.createRoute(historyId))
                    }
                )
            }

            composable(
                Screen.Profile.route,
                enterTransition = { enterFade },
                exitTransition = { exitFade }
            ) {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    historyViewModel = historyViewModel,
                    onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                    onBackClick = { navigateToTopLevel(Screen.Home.route) }, // Kembali ke Home
                    navigateToHome = { navigateToTopLevel(Screen.Home.route) },
                    navigateToDetection = navigateToDetection,
                    navigateToHistory = { navigateToTopLevel(Screen.History.route) }
                )
            }

            composable(
                Screen.History.route,
                enterTransition = { enterFade },
                exitTransition = { exitFade }
            ) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onBackClick = { navigateToTopLevel(Screen.Home.route) }, // Kembali ke Home
                    onHistoryItemClick = { historyId ->
                        navController.navigate(Screen.HistoryDetail.createRoute(historyId))
                    },
                    navigateToHome = { navigateToTopLevel(Screen.Home.route) },
                    navigateToDetection = navigateToDetection
                )
            }

            // Layar Edit dan Detail menggunakan Slide
            composable(
                Screen.EditProfile.route,
                enterTransition = { enterSlide },
                exitTransition = { exitSlide },
                popEnterTransition = { popEnterSlide },
                popExitTransition = { popExitSlide }
            ) {
                EditProfileScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.navigateUp() },
                    onSaveClick = {
                        // Logika save dipanggil di dalam EditProfileScreen
                        navController.navigateUp()
                    }
                )
            }

            composable(
                route = Screen.HistoryDetail.route,
                arguments = listOf(navArgument("historyId") { type = NavType.IntType }),
                enterTransition = { enterSlide },
                exitTransition = { exitSlide },
                popEnterTransition = { popEnterSlide },
                popExitTransition = { popExitSlide }
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
                    navigateToDetection = navigateToDetection,
                    navigateToHistory = { navigateToTopLevel(Screen.History.route) }
                )
            }

            composable(
                route = Screen.EditHistory.route,
                arguments = listOf(navArgument("historyId") { type = NavType.IntType }),
                enterTransition = { enterSlide },
                exitTransition = { exitSlide },
                popEnterTransition = { popEnterSlide },
                popExitTransition = { popExitSlide }
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

            // Layar Deteksi dan turunannya kembali menggunakan Fade
            composable(
                Screen.Detection.route,
                enterTransition = { enterFade },
                exitTransition = { exitFade }
            ) {
                DetectionScreen(
                    viewModel = detectionViewModel,
                    onBackClick = { navigateToTopLevel(Screen.Home.route) }, // Kembali ke Home
                    onManualClick = { navController.navigate(Screen.ManualInput.route) },
                    navigateToResult = {
                        // confirmRealtimeDetection dipanggil di dalam DetectionScreen onClick
                        navController.navigate(Screen.DetectionResult.route)
                    }
                )
            }

            composable(
                Screen.DetectionResult.route,
                enterTransition = { enterFade },
                exitTransition = { exitFade }
            ) {
                DetectionResultScreen(
                    viewModel = detectionViewModel,
                    onBackClick = { navController.navigateUp() }, // Kembali ke Detection
                    onSaveClick = {
                        detectionViewModel.saveDetectionToHistory()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                Screen.ManualInput.route,
                enterTransition = { enterFade },
                exitTransition = { exitFade }
            ) {
                ManualInputScreen(
                    viewModel = manualInputViewModel,
                    onBackClick = { navController.navigateUp() }, // Kembali ke Detection
                    onSaveSuccess = {
                        // State direset di ViewModel
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

