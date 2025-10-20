package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * Layar yang memungkinkan pengguna untuk mengedit entri riwayat makanan yang sudah ada.
 * Pengguna dapat mengubah sesi makan, nama item, porsi, kalori, kuantitas, dan menghapus item.
 *
 * @param viewModel ViewModel yang menyediakan state dan logika untuk detail riwayat.
 * @param onBackClick Aksi yang dipanggil saat tombol kembali ditekan.
 * @param onSaveClick Aksi yang dipanggil saat tombol simpan (FAB) ditekan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHistoryScreen(
    viewModel: HistoryDetailViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val historyDetail by viewModel.historyDetail.collectAsState()
    var itemIndexToDelete by remember { mutableStateOf<Int?>(null) }

    // Dialog konfirmasi penghapusan item
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
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.button_save), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { innerPadding ->
        val detail = historyDetail
        if (detail == null) {
            // Tampilkan loading indicator jika data belum siap
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dropdown untuk memilih sesi makan
                item {
                    SessionDropdown(
                        selectedSession = detail.sessionLabel,
                        onSessionSelected = viewModel::onSessionLabelChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Daftar item makanan yang dapat diedit
                itemsIndexed(detail.foodItems) { index, foodItem ->
                    EditableHistoryItem(
                        item = foodItem,
                        onNameChange = { newName -> viewModel.onNameChange(index, newName) },
                        onPortionChange = { newPortion -> viewModel.onPortionChange(index, newPortion) },
                        onCaloriesChange = { newCalories -> viewModel.onCaloriesChange(index, newCalories) },
                        onQuantityChange = { newQuantity -> viewModel.onQuantityChange(index, newQuantity) },
                        onDeleteItem = { itemIndexToDelete = index }
                    )
                    if (index < detail.foodItems.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

