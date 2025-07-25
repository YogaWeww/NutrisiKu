package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.ui.screen.components.ImageWithBoundingBoxes
import com.example.nutrisiku.ui.viewmodel.DetectedFoodItem
import com.example.nutrisiku.ui.viewmodel.DetectionViewModel
// --- 2. Halaman Hasil Deteksi ---
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
            // Box untuk menampilkan gambar dan loading indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                uiState.selectedBitmap?.let {
                    ImageWithBoundingBoxes(
                        bitmap = it,
                        detectionResults = uiState.detectedItems.map { item -> item.originalResult },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daftar item yang bisa diedit
            if (!uiState.isLoading) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(uiState.detectedItems) { index, item ->
                        EditableDetectedItem(
                            item = item,
                            onPortionChange = { newPortionString ->
                                val newPortion = newPortionString.toIntOrNull() ?: 0
                                viewModel.updatePortion(index, newPortion)
                            }
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            } else {
                // Beri ruang kosong saat loading agar layout tidak meloncat
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Kalori
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Kalori:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${uiState.totalCalories} KKAL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Simpan
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

// Composable untuk item yang bisa diedit porsinya
@Composable
fun EditableDetectedItem(
    item: DetectedFoodItem,
    onPortionChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = item.standardPortion.toString(),
            onValueChange = onPortionChange,
            label = { Text("gram") },
            modifier = Modifier.width(100.dp)
        )
    }
}
