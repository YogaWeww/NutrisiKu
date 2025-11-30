package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.components.NutrisiKuBottomNavBar
import com.example.nutrisiku.ui.components.ReadOnlyHistoryItem
import com.example.nutrisiku.ui.viewmodel.HistoryDetailViewModel
import java.io.File

/**
 * Layar untuk menampilkan detail dari satu entri riwayat makanan.
 * Menyediakan opsi untuk mengedit atau menghapus entri tersebut.
 *
 * @param viewModel ViewModel yang menyediakan state dan logika untuk detail riwayat.
 * @param onBackClick Aksi yang dipanggil saat tombol kembali ditekan.
 * @param onDeleteClick Aksi yang dipanggil setelah pengguna mengkonfirmasi penghapusan.
 * @param onEditClick Aksi yang dipanggil saat tombol edit ditekan, membawa ID riwayat.
 * @param navigateToHome Aksi navigasi ke layar Home.
 * @param navigateToDetection Aksi navigasi ke layar Deteksi.
 * @param navigateToHistory Aksi navigasi ke layar Riwayat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    viewModel: HistoryDetailViewModel,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    navigateToHome: () -> Unit,
    navigateToDetection: () -> Unit,
    navigateToHistory: () -> Unit
) {
    val historyDetail by viewModel.historyDetail.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Dialog konfirmasi penghapusan
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_history_dialog_title)) },
            text = { Text(stringResource(R.string.delete_history_dialog_text)) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.button_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.history_detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        },
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.button_edit)) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.button_edit)) },
                    onClick = { historyDetail?.id?.let { onEditClick(it) } },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
                FloatingActionButton(
                    onClick = { showDeleteDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.button_delete))
                }
            }
        }
    ) { innerPadding ->
        val detail = historyDetail
        if (detail == null) {
            // Tampilkan loading indicator jika data belum siap
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp) // Tambah padding bawah
            ) {
                // Tampilkan gambar riwayat
                item {
                    AsyncImage(
                        model = if (detail.imagePath.isNotEmpty()) File(detail.imagePath) else R.drawable.logo_nutrisiku,
                        contentDescription = stringResource(R.string.history_detail_image_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.history_detail_food_items),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                // Daftar item makanan
                items(detail.foodItems) { foodItem ->
                    ReadOnlyHistoryItem(foodItem = foodItem)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                // Tampilan total kalori
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.total_calories_label), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(R.string.kcal_value, detail.totalCalories),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
