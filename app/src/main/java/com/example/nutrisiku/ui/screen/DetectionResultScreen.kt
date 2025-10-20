package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.components.FoodItemResultCard
import com.example.nutrisiku.ui.components.ImageResult
import com.example.nutrisiku.ui.components.PortionEditDialog
import com.example.nutrisiku.ui.components.SessionDropdown
import com.example.nutrisiku.ui.viewmodel.DetectedFoodItem
import com.example.nutrisiku.ui.viewmodel.DetectionViewModel

/**
 * Layar untuk menampilkan hasil deteksi makanan dari sebuah gambar.
 * Pengguna dapat meninjau, mengedit kuantitas/porsi, dan menyimpan hasilnya.
 *
 * @param viewModel ViewModel yang menyediakan state dan logika untuk layar ini.
 * @param onBackClick Aksi yang dipanggil saat tombol kembali ditekan.
 * @param onSaveClick Aksi yang dipanggil saat tombol simpan ditekan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionResultScreen(
    viewModel: DetectionViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // State untuk mengelola dialog edit porsi
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
                title = { Text(stringResource(R.string.detection_result_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
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
            // Menampilkan gambar hasil deteksi
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
                stringResource(R.string.food_details_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Daftar item makanan yang terdeteksi
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(uiState.detectedItems) { index, item ->
                    FoodItemResultCard(
                        item = item,
                        onQuantityChange = { newQuantity ->
                            viewModel.updateItemQuantity(item, newQuantity)
                        },
                        onPortionChange = {
                            editingPortionItem = IndexedValue(index, item)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown untuk memilih sesi makan
            SessionDropdown(
                selectedSession = uiState.sessionLabel,
                onSessionSelected = viewModel::onSessionLabelChange
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Tampilan total kalori
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.total_calories_label), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.kcal_value, uiState.totalCalories),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol simpan
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.button_save), fontWeight = FontWeight.Bold)
            }
        }
    }
}

