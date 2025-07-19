package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.screen.components.DetectedItem
import com.example.nutrisiku.ui.screen.components.NutrisiKuBottomNavBar
import com.example.nutrisiku.ui.theme.NutrisiKuTheme

// --- 1. Halaman Utama Riwayat ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: () -> Unit,
    onHistoryItemClick: (Int) -> Unit,
    navigateToHome: () -> Unit, // Tambahkan ini
    navigateToDetection: () -> Unit // Tambahkan ini
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Riwayat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = { NutrisiKuBottomNavBar(
            onHomeClick = { navigateToHome },
            onDetectionClick = { navigateToDetection },
            onHistoryClick = { /* Navigasi ke Riwayat */ }
        ) }
    ) { innerPadding ->
        // Menggunakan LazyColumn untuk daftar yang efisien
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contoh data dummy yang dikelompokkan
            item { DateHeader("06 Juli 2025", "Total: 750 KKAL") }
            item { HistoryEntryCard(session = "Makan Siang", totalCalorie = 750, onClick = { onHistoryItemClick(1) }) }

            item { DateHeader("05 Juli 2025", "Total: 1500 KKAL") }
            item { HistoryEntryCard(session = "Makan Siang", totalCalorie = 750, onClick = { onHistoryItemClick(2) }) }
            item { HistoryEntryCard(session = "Makan Malam", totalCalorie = 750, onClick = { onHistoryItemClick(3) }) }
        }
    }
}

@Composable
fun DateHeader(date: String, totalCalorie: String) {
    Column {
        Text(text = date, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = totalCalorie, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryEntryCard(session: String, totalCalorie: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder untuk gambar
            Image(
                painter = painterResource(id = R.drawable.nasi_goreng_detected), // Ganti dengan gambar contoh
                contentDescription = session,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = session, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Total: $totalCalorie KKAL", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}

// --- 2. Halaman Detail Riwayat ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    onBackClick: () -> Unit,
    navigateToHome: () -> Unit, // Tambahkan ini
    navigateToDetection: () -> Unit, // Tambahkan ini
    navigateToHistory: () -> Unit // Tambahkan ini
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detail Riwayat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = { NutrisiKuBottomNavBar(
            onHomeClick = { navigateToHome },
            onDetectionClick = { navigateToDetection },
            onHistoryClick = { navigateToHistory }
        ) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Gambar hasil (mirip dengan DetectionResultScreen)
            Image(
                painter = painterResource(id = R.drawable.nasi_goreng_detected), // Ganti dengan gambar contoh
                contentDescription = "Detail Makanan",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Daftar item yang terdeteksi
            DetectedItem(name = "Nasi (100g)", calorie = "200 KKAL")
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            DetectedItem(name = "Telur (50g)", calorie = "75 KKAL")

            Spacer(modifier = Modifier.height(16.dp))

            // Total Kalori
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Kalori:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("750 KKAL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Aksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Aksi hapus */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { /* Aksi simpan perubahan */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- Previews ---
//@Preview(showBackground = true, device = "id:pixel_5")
//@Composable
//fun HistoryScreenPreview() {
//    NutrisiKuTheme {
//        HistoryScreen(onBackClick = {}, onHistoryItemClick = {})
//    }
//}

//@Preview(showBackground = true, device = "id:pixel_5")
//@Composable
//fun HistoryDetailScreenPreview() {
//    NutrisiKuTheme {
//        HistoryDetailScreen(onBackClick = {})
//    }
//}