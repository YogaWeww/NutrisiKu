package com.example.nutrisiku.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.components.EditableHistoryItem
import com.example.nutrisiku.ui.components.SessionDropdown
import com.example.nutrisiku.ui.viewmodel.HistoryDetailViewModel

/**
 * Layar untuk mengedit detail entri riwayat makanan.
 * Memungkinkan pengguna mengubah sesi, nama makanan, porsi, kalori, kuantitas,
 * dan menghapus item makanan.
 * Menangani konfirmasi sebelum keluar jika ada perubahan yang belum disimpan.
 *
 * @param viewModel ViewModel yang menyediakan state dan logika untuk detail riwayat.
 * @param onBackClick Aksi untuk kembali ke layar sebelumnya.
 * @param onSaveClick Aksi yang dipanggil saat tombol simpan ditekan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHistoryScreen(
    viewModel: HistoryDetailViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit // Tetap dipanggil dari FAB
) {
    val historyDetail by viewModel.historyDetail.collectAsState()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsState()
    var showDiscardConfirmationDialog by remember { mutableStateOf(false) }
    var itemIndexToDelete by remember { mutableStateOf<Int?>(null) }

    // Tangani penekanan tombol kembali sistem
    BackHandler(enabled = hasUnsavedChanges) {
        showDiscardConfirmationDialog = true
    }

    // Dialog konfirmasi buang perubahan
    if (showDiscardConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmationDialog = false },
            title = { Text(stringResource(R.string.discard_changes_dialog_title)) },
            text = { Text(stringResource(R.string.discard_changes_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.discardChanges() // Panggil discard di ViewModel
                        showDiscardConfirmationDialog = false
                        onBackClick() // Lanjutkan navigasi kembali
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.button_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirmationDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }


    // Dialog konfirmasi hapus item makanan
    if (itemIndexToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemIndexToDelete = null },
            title = { Text(stringResource(R.string.delete_food_item_dialog_title)) },
            text = { Text(stringResource(R.string.delete_food_item_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemIndexToDelete?.let { viewModel.onDeleteItem(it) }
                        itemIndexToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.button_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemIndexToDelete = null }) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.edit_history_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        // Cek perubahan sebelum navigasi kembali via ikon
                        if (hasUnsavedChanges) {
                            showDiscardConfirmationDialog = true
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick, // onSaveClick akan memanggil updateOrDeleteHistory di NavHost
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.button_save))
            }
        }
    ) { innerPadding ->
        val detail = historyDetail
        if (detail == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // Beri jarak antar item
            ) {
                // Dropdown Sesi
                item {
                    SessionDropdown(
                        selectedSession = detail.sessionLabel,
                        onSessionSelected = viewModel::onSessionLabelChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp)) // Kurangi spacer
                }

                // Daftar Item Makanan yang Bisa Diedit
                itemsIndexed(detail.foodItems, key = { _, item -> item.name + item.portion }) { index, foodItem ->
                    EditableHistoryItem(
                        item = foodItem,
                        onNameChange = { newName -> viewModel.onNameChange(index, newName) },
                        onPortionChange = { newPortion -> viewModel.onPortionChange(index, newPortion) },
                        onCaloriesChange = { newCalories -> viewModel.onCaloriesChange(index, newCalories) },
                        onQuantityChange = { newQuantity -> viewModel.onQuantityChange(index, newQuantity) },
                        onDeleteItem = { itemIndexToDelete = index }
                    )
                    // Jangan tambahkan divider setelah item terakhir
                    if (index < detail.foodItems.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
                // Spacer di akhir agar tidak tertutup FAB
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

