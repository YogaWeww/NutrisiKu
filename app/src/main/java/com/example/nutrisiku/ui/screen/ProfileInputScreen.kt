package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.ui.screen.components.ActivityLevelDropdown
import com.example.nutrisiku.ui.screen.components.GenderDropdown
import com.example.nutrisiku.ui.theme.NutrisiKuTheme
import com.example.nutrisiku.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileInputScreen(
    viewModel: ProfileViewModel, // Terima ViewModel sebagai parameter
    onConfirmClick: () -> Unit
) {
    // Ambil UI state dari ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Selamat Datang",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Silahkan Masukkan Data Diri Anda",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Input Fields
            OutlinedTextField(
                value = uiState.name, // Baca data dari state
                onValueChange = { viewModel.onNameChange(it) }, // Kirim perubahan ke ViewModel
                label = { Text("Nama / Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.age,
                onValueChange = { viewModel.onAgeChange(it) },
                label = { Text("Umur (Tahun)") },
                leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.weight, // Baca data dari state
                onValueChange = { viewModel.onWeightChange(it) }, // Kirim perubahan ke ViewModel
                label = { Text("Berat Badan (kg)") },
                leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.height, // Baca data dari state
                onValueChange = { viewModel.onHeightChange(it) }, // Kirim perubahan ke ViewModel
                label = { Text("Tinggi Badan (cm)") },
                leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            GenderDropdown()
            Spacer(modifier = Modifier.height(8.dp))

            ActivityLevelDropdown(
                selectedActivity = uiState.activityLevel,
                onActivitySelected = viewModel::onActivityLevelChange
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Anda Dapat Mengubah Data Diri Kapan Saja",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onConfirmClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Konfirmasi", fontWeight = FontWeight.Bold)
            }
        }
    }
}


//@Preview(showBackground = true, device = "id:pixel_5")
//@Composable
//fun ProfileInputScreenPreview() {
//    NutrisiKuTheme {
//        ProfileInputScreen(onConfirmClick = {})
//    }
//}