package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.data.HistoryFoodItem
import com.example.nutrisiku.ui.screen.components.QuantityEditor
import com.example.nutrisiku.ui.screen.components.SessionDropdown
import com.example.nutrisiku.ui.viewmodel.HistoryDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHistoryScreen(
    viewModel: HistoryDetailViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val historyDetail by viewModel.historyDetail.collectAsState()
    var itemIndexToDelete by remember { mutableStateOf<Int?>(null) }

    if (itemIndexToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemIndexToDelete = null },
            title = { Text("Hapus Makanan") },
            text = { Text("Apakah Anda yakin ingin menghapus item ini dari riwayat?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemIndexToDelete?.let { viewModel.onDeleteItem(it) }
                        itemIndexToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemIndexToDelete = null }) {
                    Text("Batal")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Check, contentDescription = "Simpan")
            }
        }
    ) { innerPadding ->
        val detail = historyDetail
        if (detail == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    SessionDropdown(
                        selectedSession = detail.sessionLabel,
                        onSessionSelected = { newLabel -> viewModel.onSessionLabelChange(newLabel) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                itemsIndexed(detail.foodItems) { index, foodItem ->
                    EditableHistoryItem(
                        item = foodItem,
                        onNameChange = { newName -> viewModel.onNameChange(index, newName) },
                        onPortionChange = { newPortion -> viewModel.onPortionChange(index, newPortion) },
                        // --- PERUBAHAN: Tambahkan callback untuk kalori ---
                        onCaloriesChange = { newCalories -> viewModel.onCaloriesChange(index, newCalories) },
                        onQuantityChange = { newQuantity -> viewModel.onQuantityChange(index, newQuantity) },
                        onDeleteItem = { itemIndexToDelete = index }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
    onCaloriesChange: (String) -> Unit, // --- PERUBAHAN: Parameter baru ---
    onQuantityChange: (Int) -> Unit,
    onDeleteItem: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = item.name,
                onValueChange = onNameChange,
                label = { Text("Nama Makanan") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDeleteItem) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus Item", tint = MaterialTheme.colorScheme.secondary)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = if (item.portion > 0) item.portion.toString() else "",
                onValueChange = onPortionChange,
                label = { Text("Porsi (g)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.width(16.dp))
            QuantityEditor(
                quantity = item.quantity,
                onDecrement = { onQuantityChange(item.quantity - 1) },
                onIncrement = { onQuantityChange(item.quantity + 1) }
            )
        }
        // --- PERUBAHAN: Tambahkan field untuk mengedit kalori ---
        OutlinedTextField(
            value = if (item.calories > 0) item.calories.toString() else "",
            onValueChange = onCaloriesChange,
            label = { Text("Kalori per Porsi") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

