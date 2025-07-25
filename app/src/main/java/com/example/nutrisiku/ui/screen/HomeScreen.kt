package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.screen.components.CalorieCard
import com.example.nutrisiku.ui.screen.components.HistoryEntryCard
import com.example.nutrisiku.ui.screen.components.NutrisiKuBottomNavBar
import com.example.nutrisiku.ui.viewmodel.HistoryViewModel
import com.example.nutrisiku.ui.viewmodel.ProfileViewModel
import java.io.File
import com.example.nutrisiku.R

@Composable
fun HomeScreen(
    profileViewModel: ProfileViewModel,
    historyViewModel: HistoryViewModel,
    navigateToProfile: () -> Unit,
    navigateToDetection: () -> Unit,
    navigateToHistory: () -> Unit
) {
    // Ambil UI state dari ViewModel
    val profileState by profileViewModel.uiState.collectAsState()
    val historyList by historyViewModel.historyList.collectAsState()
    val todaysCalories by historyViewModel.todaysCalories.collectAsState() // Ambil kalori hari ini
    val latestHistory = historyList.firstOrNull()

    Scaffold(
        bottomBar = {
            NutrisiKuBottomNavBar(
                // PERBAIKAN: Beri tahu BottomNavBar bahwa rute saat ini adalah "home"
                currentRoute = Screen.Home.route,
                onHomeClick = { /* Kita sudah di Home, tidak perlu aksi */ },
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
            // Tampilkan nama pengguna dari ViewModel
            HeaderSection(
                name = profileState.name,
                imagePath = profileState.imagePath,
                onProfileClick = navigateToProfile
            )
            Spacer(modifier = Modifier.height(24.dp))
            DateCard()
            Spacer(modifier = Modifier.height(16.dp))
            // Di dalam HomeScreen.kt
            CalorieCard(
                consumed = todaysCalories,
                total = profileState.tdee
            )

            Spacer(modifier = Modifier.height(16.dp))
            DetectNowButton(onClick = navigateToDetection)
            Spacer(modifier = Modifier.height(24.dp))
            HistorySection(
                latestHistory = latestHistory, // Teruskan data riwayat terbaru
                onSeeAllClick = navigateToHistory
            )
        }
    }
}

@Composable
fun HeaderSection(
    name: String,
    imagePath: String, // Terima path gambar
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Halo $name",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        AsyncImage(
            model = if (imagePath.isNotEmpty()) File(imagePath) else R.drawable.logo_nutrisiku,
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
                text = "Minggu, 08 Juli 2025",
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
            imageVector = Icons.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}
// ... (Sisa Composable lain di HomeScreen seperti HeaderSection, CalorieCard, dll. tetap sama)
// Pastikan HistorySection dimodifikasi untuk menerima onSeeAllClick
@Composable
fun HistorySection(
    latestHistory: com.example.nutrisiku.data.HistoryEntity?, // Terima data riwayat
    onSeeAllClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Riwayat Deteksi:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (latestHistory != null) {
            HistoryEntryCard(
                imagePath = latestHistory.imagePath,
                session = latestHistory.sessionLabel,
                totalCalorie = latestHistory.totalCalories,
                onClick = onSeeAllClick
            )
        } else {
            // Tampilan jika riwayat masih kosong
            Text("Belum ada riwayat deteksi.")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onSeeAllClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Lihat Semua",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}