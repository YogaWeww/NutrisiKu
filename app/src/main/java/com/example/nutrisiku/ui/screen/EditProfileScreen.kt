package com.example.nutrisiku.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.components.ActivityLevelDropdown
import com.example.nutrisiku.ui.components.GenderDropdown
import com.example.nutrisiku.ui.viewmodel.ProfileViewModel

/**
 * Layar yang berfungsi sebagai formulir untuk mengedit data profil pengguna.
 * Menangani konfirmasi sebelum keluar jika ada perubahan yang belum disimpan.
 *
 * @param viewModel ViewModel yang mengelola state dan logika untuk profil.
 * @param onBackClick Aksi untuk kembali ke layar sebelumnya (setelah konfirmasi jika perlu).
 * @param onSaveClick Aksi yang dipanggil saat tombol simpan ditekan (diteruskan dari NavHost).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit, // Dipanggil setelah konfirmasi 'discard' atau jika tidak ada perubahan
    onSaveClick: () -> Unit  // Dipanggil saat tombol Simpan ditekan
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDiscardConfirmationDialog by remember { mutableStateOf(false) }

    // Tangani penekanan tombol kembali sistem
    BackHandler(enabled = uiState.hasUnsavedChanges) {
        showDiscardConfirmationDialog = true
    }

    // Dialog konfirmasi jika ada perubahan belum disimpan
    if (showDiscardConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmationDialog = false },
            title = { Text(stringResource(R.string.discard_changes_dialog_title)) },
            text = { Text(stringResource(R.string.discard_changes_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        // PERBAIKAN: Panggil fungsi discardChanges yang baru
                        viewModel.discardChanges()
                        showDiscardConfirmationDialog = false
                        onBackClick() // Lanjutkan navigasi kembali
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.button_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirmationDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.edit_profile_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        // Cek perubahan sebelum navigasi kembali via ikon panah
                        if (uiState.hasUnsavedChanges) {
                            showDiscardConfirmationDialog = true
                        } else {
                            onBackClick() // Langsung kembali jika tidak ada perubahan
                        }
                    }) {
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.profile_edit_required_fields_note),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.label_name)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.age,
                onValueChange = viewModel::onAgeChange,
                label = { Text(stringResource(R.string.label_age_years)) },
                leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.weight,
                onValueChange = viewModel::onWeightChange,
                label = { Text(stringResource(R.string.label_weight)) },
                leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.height,
                onValueChange = viewModel::onHeightChange,
                label = { Text(stringResource(R.string.label_height)) },
                leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            GenderDropdown(
                selectedGender = uiState.gender,
                onGenderSelected = viewModel::onGenderChange
            )
            Spacer(modifier = Modifier.height(8.dp))

            ActivityLevelDropdown(
                selectedActivity = uiState.activityLevel,
                onActivitySelected = viewModel::onActivityLevelChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Panggil fungsi saveUserData di ViewModel SEBELUM memanggil onSaveClick (navigasi)
                    viewModel.saveUserData() // Ini akan menyimpan & mereset flag hasUnsavedChanges
                    onSaveClick()          // Panggil aksi navigasi yang diteruskan
                },
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

