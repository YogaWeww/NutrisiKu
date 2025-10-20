package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.components.CalorieCard
import com.example.nutrisiku.ui.components.DateCard
import com.example.nutrisiku.ui.components.DetectNowButton
import com.example.nutrisiku.ui.components.HeaderSection
import com.example.nutrisiku.ui.components.HistorySection
import com.example.nutrisiku.ui.components.NutrisiKuBottomNavBar
import com.example.nutrisiku.ui.viewmodel.HistoryViewModel
import com.example.nutrisiku.ui.viewmodel.ProfileViewModel

/**
 * Layar utama (dasbor) aplikasi. Menampilkan ringkasan profil, status kalori,
 * dan riwayat deteksi terakhir.
 *
 * @param profileViewModel ViewModel untuk mendapatkan data profil pengguna.
 * @param historyViewModel ViewModel untuk mendapatkan data riwayat.
 * @param navigateToProfile Aksi navigasi ke layar Profil.
 * @param navigateToDetection Aksi navigasi ke layar Deteksi.
 * @param navigateToHistory Aksi navigasi ke layar Riwayat.
 * @param navigateToHistoryDetail Aksi navigasi ke layar Detail Riwayat, membawa ID riwayat.
 */
@Composable
fun HomeScreen(
    profileViewModel: ProfileViewModel,
    historyViewModel: HistoryViewModel,
    navigateToProfile: () -> Unit,
    navigateToDetection: () -> Unit,
    navigateToHistory: () -> Unit,
    navigateToHistoryDetail: (Int) -> Unit
) {
    // Kumpulkan state dari ViewModel
    val profileState by profileViewModel.uiState.collectAsState()
    val historyList by historyViewModel.historyList.collectAsState()
    val todaysCalories by historyViewModel.todaysCalories.collectAsState()
    val latestHistory = historyList.firstOrNull()

    Scaffold(
        bottomBar = {
            NutrisiKuBottomNavBar(
                currentRoute = Screen.Home.route,
                onHomeClick = { /* Tidak perlu aksi, sudah di Home */ },
                onDetectionClick = navigateToDetection,
                onHistoryClick = navigateToHistory
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Susun komponen-komponen yang telah diimpor
            HeaderSection(
                name = profileState.name,
                imagePath = profileState.imagePath,
                onProfileClick = navigateToProfile
            )
            Spacer(modifier = Modifier.height(24.dp))
            DateCard()
            Spacer(modifier = Modifier.height(16.dp))
            CalorieCard(
                consumed = todaysCalories,
                total = profileState.tdee
            )
            Spacer(modifier = Modifier.height(16.dp))
            DetectNowButton(onClick = navigateToDetection)
            Spacer(modifier = Modifier.height(24.dp))
            HistorySection(
                latestHistory = latestHistory,
                onSeeAllClick = navigateToHistory,
                onLatestHistoryClick = {
                    latestHistory?.id?.let { navigateToHistoryDetail(it) }
                }
            )
            Spacer(modifier = Modifier.height(16.dp)) // Beri ruang di bagian bawah
        }
    }
}
