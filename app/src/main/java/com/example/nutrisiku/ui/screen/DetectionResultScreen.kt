package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.ui.screen.components.ImageResult
import com.example.nutrisiku.ui.screen.components.PortionEditDialog
import com.example.nutrisiku.ui.screen.components.QuantityEditor
import com.example.nutrisiku.ui.screen.components.SessionDropdown
import com.example.nutrisiku.ui.viewmodel.DetectedFoodItem
import com.example.nutrisiku.ui.viewmodel.DetectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionResultScreen(
    viewModel: DetectionViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var editingPortionItem by remember { mutableStateOf<IndexedValue<DetectedFoodItem>?>(null) }

    editingPortionItem?.let { (index, item) ->
        PortionEditDialog(
            currentPortion = item.standardPortion,
            onDismiss = { editingPortionItem = null },
            onConfirm = { newPortion ->
                viewModel.updatePortion(index, newPortion)
                editingPortionItem = null
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hasil Deteksi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            uiState.selectedBitmap?.let { bitmap ->
                ImageResult(
                    bitmap = bitmap,
                    detectionResults = uiState.detectedItems.map { it.originalResult },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Rincian Makanan:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(uiState.detectedItems) { index, item ->
                    FoodItemResultCard(
                        item = item,
                        onQuantityChange = { newQuantity ->
                            viewModel.updateItemQuantity(item, newQuantity, isFromResultScreen = true)
                        },
                        onPortionChange = {
                            editingPortionItem = IndexedValue(index, item)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SessionDropdown(
                selectedSession = uiState.sessionLabel,
                onSessionSelected = viewModel::onSessionLabelChange
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Kalori:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${uiState.totalCalories} KKAL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FoodItemResultCard(
    item: DetectedFoodItem,
    onQuantityChange: (Int) -> Unit,
    onPortionChange: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Porsi: ${item.standardPortion}g",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    IconButton(onClick = onPortionChange, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ubah Porsi",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            QuantityEditor(
                quantity = item.quantity,
                onDecrement = { onQuantityChange(item.quantity - 1) },
                onIncrement = { onQuantityChange(item.quantity + 1) }
            )

            Text(
                text = "${item.caloriesPerPortion * item.quantity} Kkal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

// --- FUNGSI SessionDropdown DIHAPUS DARI SINI KARENA PINDAH KE SHAREDCOMPONENTS ---

