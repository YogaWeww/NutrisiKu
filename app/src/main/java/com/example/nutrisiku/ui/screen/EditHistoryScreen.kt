package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSaveClick) {
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
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun EditableHistoryItem(
    item: HistoryFoodItem,
    onNameChange: (String) -> Unit, // Tambahkan ini
    onPortionChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // PERUBAHAN: Ganti Text dengan OutlinedTextField
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
    }
}