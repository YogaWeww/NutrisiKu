package com.example.nutrisiku.ui.screen

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.screen.components.DateHeader
import com.example.nutrisiku.ui.screen.components.EmptyHistoryView
import com.example.nutrisiku.ui.screen.components.HistoryEntryCard
import com.example.nutrisiku.ui.screen.components.NutrisiKuBottomNavBar
import com.example.nutrisiku.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Layar yang menampilkan daftar riwayat deteksi makanan pengguna.
 * Entri dikelompokkan berdasarkan tanggal.
 *
 * @param viewModel ViewModel yang menyediakan daftar riwayat.
 * @param onBackClick Aksi untuk kembali ke layar sebelumnya.
 * @param onHistoryItemClick Aksi yang dipanggil saat sebuah item riwayat diklik, membawa ID-nya.
 * @param navigateToHome Aksi navigasi ke layar Home.
 * @param navigateToDetection Aksi navigasi ke layar Deteksi.
 */
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

    // Kelompokkan daftar riwayat berdasarkan tanggal
    val groupedHistory = historyList.groupBy {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(it.timestamp))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.history_screen_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        },
        bottomBar = {
            NutrisiKuBottomNavBar(
                currentRoute = Screen.History.route,
                onHomeClick = navigateToHome,
                onDetectionClick = navigateToDetection,
                onHistoryClick = { /* Sudah berada di layar ini */ }
            )
        }
    ) { innerPadding ->
        if (groupedHistory.isEmpty()) {
            // Tampilkan tampilan kosong jika tidak ada riwayat
            EmptyHistoryView(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedHistory.forEach { (date, items) ->
                    stickyHeader {
                        val totalCaloriesForDay = items.sumOf { it.totalCalories }
                        DateHeader(
                            date = date,
                            totalCalorie = stringResource(R.string.total_calories_value, totalCaloriesForDay)
                        )
                    }
                    items(items, key = { it.id }) { historyItem ->
                        HistoryEntryCard(
                            imagePath = historyItem.imagePath,
                            session = historyItem.sessionLabel,
                            totalCalorie = historyItem.totalCalories,
                            onClick = { onHistoryItemClick(historyItem.id) }
                        )
                    }
                }
            }
        }
    }
}
