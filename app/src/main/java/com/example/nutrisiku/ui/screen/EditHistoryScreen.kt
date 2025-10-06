package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.data.HistoryFoodItem
import com.example.nutrisiku.ui.viewmodel.HistoryDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHistoryScreen(
    viewModel: HistoryDetailViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val historyDetail by viewModel.historyDetail.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Riwayat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                // PERBAIKAN: Terapkan warna tema
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Check, contentDescription = "Simpan")
            }
        }
    ) { innerPadding ->
        val detail = historyDetail
        if (detail == null) {
            // Tampilkan loading
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // ... (Tampilkan gambar dan total kalori seperti di detail screen)
                itemsIndexed(detail.foodItems) { index, foodItem ->
                    EditableHistoryItem(
                        item = foodItem,
                        onNameChange = { newName ->
                            viewModel.onNameChange(index, newName)
                        },
                        onPortionChange = { newPortion ->
                            viewModel.onPortionChange(index, newPortion)
                        },
                        // PERUBAHAN: Tambahkan aksi untuk menghapus item
                        onDeleteItem = {
                            viewModel.onDeleteItem(index)
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                }
            }
        }
    }
}

@Composable
fun EditableHistoryItem(
    item: HistoryFoodItem,
    onNameChange: (String) -> Unit,
    onPortionChange: (String) -> Unit,
    onDeleteItem: () -> Unit // PERUBAHAN: Tambahkan parameter callback
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = item.name,
            onValueChange = onNameChange,
            label = { Text("Nama Makanan") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = item.portion.toString(),
            onValueChange = onPortionChange,
            label = { Text("gram") },
            modifier = Modifier.width(100.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        // PERUBAHAN: Tambahkan IconButton untuk hapus
        IconButton(onClick = onDeleteItem) {
            Icon(Icons.Default.Delete, contentDescription = "Hapus Item", tint = MaterialTheme.colorScheme.secondary)
        }
    }
}