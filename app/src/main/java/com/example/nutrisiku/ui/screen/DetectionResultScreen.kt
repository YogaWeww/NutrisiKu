package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.ui.screen.components.ImageResult
import com.example.nutrisiku.ui.viewmodel.DetectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionResultScreen(
    viewModel: DetectionViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hasil Deteksi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
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
            // PERBAIKAN: Gunakan komponen ImageWithBoundingBoxes yang baru
            uiState.selectedBitmap?.let { bitmap ->
                ImageResult(
                    bitmap = bitmap,
                    detectionResults = uiState.detectedItems.map { it.originalResult },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) // Menjaga rasio aspek 1:1
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daftar Makanan
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(uiState.detectedItems) { index, item ->
                    DetectedItemRow(
                        name = item.name,
                        portion = item.standardPortion.toString(),
                        calories = item.calories,
                        onPortionChange = { newPortion ->
                            viewModel.updatePortion(index, newPortion.toIntOrNull() ?: 0)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Kalori
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Kalori:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${uiState.totalCalories} KKAL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Simpan
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Simpan")
            }
        }
    }
}

@Composable
fun DetectedItemRow(
    name: String,
    portion: String,
    calories: Int,
    onPortionChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge)
            Text("$calories KKAL", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }

        OutlinedTextField(
            value = portion,
            onValueChange = onPortionChange,
            modifier = Modifier.width(120.dp),
            label = { Text("gram") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Tombol titik tiga telah dihapus dari sini
    }
}
