package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nutrisiku.R
import com.example.nutrisiku.data.HistoryEntity
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.screen.components.CalorieCard
import com.example.nutrisiku.ui.screen.components.HistoryEntryCard
import com.example.nutrisiku.ui.screen.components.NutrisiKuBottomNavBar
import com.example.nutrisiku.ui.viewmodel.HistoryViewModel
import com.example.nutrisiku.ui.viewmodel.ProfileViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    profileViewModel: ProfileViewModel,
    historyViewModel: HistoryViewModel,
    navigateToProfile: () -> Unit,
    navigateToDetection: () -> Unit,
    navigateToHistory: () -> Unit,
    // --- PERUBAHAN: Tambahkan parameter baru ---
    navigateToHistoryDetail: (Int) -> Unit
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val historyList by historyViewModel.historyList.collectAsState()
    val todaysCalories by historyViewModel.todaysCalories.collectAsState()
    val latestHistory = historyList.firstOrNull()

    Scaffold(
        bottomBar = {
            NutrisiKuBottomNavBar(
                currentRoute = Screen.Home.route,
                onHomeClick = { /* No action needed */ },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
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
                // --- PERUBAHAN: Teruskan kedua aksi navigasi ---
                onSeeAllClick = navigateToHistory,
                onLatestHistoryClick = {
                    latestHistory?.id?.let { navigateToHistoryDetail(it) }
                }
            )
        }
    }
}

@Composable
fun HeaderSection(
    name: String,
    imagePath: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (name.isNotEmpty()) "Halo, $name" else "Halo!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        AsyncImage(
            model = if (imagePath.isNotEmpty()) File(imagePath) else R.drawable.default_profile,
            contentDescription = "Foto Profil",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onProfileClick() },
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun DateCard() {
    val currentDate = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = "Tanggal",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DetectNowButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = "Deteksi",
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Deteksi Sekarang",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun HistorySection(
    latestHistory: HistoryEntity?,
    onSeeAllClick: () -> Unit,
    // --- PERUBAHAN: Tambahkan parameter baru ---
    onLatestHistoryClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Riwayat Deteksi:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (latestHistory != null) {
            // --- PERUBAHAN: Gunakan aksi klik yang baru ---
            HistoryEntryCard(
                imagePath = latestHistory.imagePath,
                session = latestHistory.sessionLabel,
                totalCalorie = latestHistory.totalCalories,
                onClick = onLatestHistoryClick // Arahkan ke detail
            )
        } else {
            Text("Belum ada riwayat deteksi.")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onSeeAllClick, // Aksi ini tetap mengarah ke daftar riwayat
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Lihat Semua Riwayat",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

