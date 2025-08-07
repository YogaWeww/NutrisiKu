package com.example.nutrisiku.ui.screen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.screen.components.DateHeader
import com.example.nutrisiku.ui.screen.components.HistoryEntryCard
import com.example.nutrisiku.ui.screen.components.NutrisiKuBottomNavBar
import com.example.nutrisiku.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- 1. Halaman Utama Riwayat ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBackClick: () -> Unit,
    onHistoryItemClick: (Int) -> Unit,
    navigateToHome: () -> Unit,
    navigateToDetection: () -> Unit
) {
    val historyList by viewModel.historyList.collectAsState()

    val groupedHistory = historyList.groupBy {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(it.timestamp))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Riwayat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            NutrisiKuBottomNavBar(
                currentRoute = Screen.History.route,
                onHomeClick = navigateToHome,
                onDetectionClick = navigateToDetection,
                onHistoryClick = { /* Sudah di Riwayat */ }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (groupedHistory.isEmpty()) {
                item {
                    Text("Anda belum memiliki riwayat deteksi.")
                }
            } else {
                groupedHistory.forEach { (date, items) ->
                    stickyHeader {
                        val totalCaloriesForDay = items.sumOf { it.totalCalories }
                        DateHeader(date = date, totalCalorie = "Total: $totalCaloriesForDay KKAL")
                    }
                    items(items, key = { it.id }) { historyItem ->
                        HistoryEntryCard(
                            imagePath = historyItem.imagePath,
                            session = historyItem.sessionLabel,
                            totalCalorie = historyItem.totalCalories,
                            onClick = {
                                // PERBAIKAN: Tambahkan pengecekan ID di sini
                                if (historyItem.id > 0) {
                                    Log.d("DEBUG_NAV", "HistoryScreen: Mengirim ID valid = ${historyItem.id}")
                                    onHistoryItemClick(historyItem.id)
                                } else {
                                    Log.e("DEBUG_NAV", "HistoryScreen: Mencoba menavigasi dengan ID tidak valid = ${historyItem.id}")
                                }
                            }
                        )
                    }
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