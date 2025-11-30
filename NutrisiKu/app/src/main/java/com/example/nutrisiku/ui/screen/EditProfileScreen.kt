package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.screen.components.ActivityLevelDropdown
import com.example.nutrisiku.ui.screen.components.GenderDropdown
import com.example.nutrisiku.ui.screen.components.NutrisiKuBottomNavBar
import com.example.nutrisiku.ui.viewmodel.ProfileViewModel

/**
 * Layar untuk mengedit data profil pengguna yang sudah ada.
 *
 * @param viewModel ViewModel yang mengelola state dan logika untuk profil.
 * @param onBackClick Aksi untuk kembali ke layar sebelumnya.
 * @param onSaveClick Aksi yang dipanggil saat tombol simpan diklik.
 * @param navigateToHome Aksi navigasi ke layar Home.
 * @param navigateToDetection Aksi navigasi ke layar Deteksi.
 * @param navigateToHistory Aksi navigasi ke layar Riwayat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToDetection: () -> Unit,
    navigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Memuat data awal saat layar pertama kali ditampilkan
    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.edit_profile_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        },
        bottomBar = {
            NutrisiKuBottomNavBar(
                currentRoute = Screen.Profile.route, // Tetap menyorot ikon profil
                onHomeClick = navigateToHome,
                onDetectionClick = navigateToDetection,
                onHistoryClick = navigateToHistory
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_input_required_fields_note),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.label_name)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.age,
                onValueChange = viewModel::onAgeChange,
                label = { Text(stringResource(R.string.label_age_years)) },
                leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.weight,
                onValueChange = viewModel::onWeightChange,
                label = { Text(stringResource(R.string.label_weight)) },
                leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.height,
                onValueChange = viewModel::onHeightChange,
                label = { Text(stringResource(R.string.label_height)) },
                leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            GenderDropdown(
                selectedGender = uiState.gender,
                onGenderSelected = viewModel::onGenderChange
            )
            ActivityLevelDropdown(
                selectedActivity = uiState.activityLevel,
                onActivitySelected = viewModel::onActivityLevelChange
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSaveClick,
                enabled = uiState.isConfirmButtonEnabled,
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
