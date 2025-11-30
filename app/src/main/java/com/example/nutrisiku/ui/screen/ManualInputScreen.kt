package com.example.nutrisiku.ui.screen

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.components.ImagePicker
import com.example.nutrisiku.ui.components.ManualFoodItemCard
import com.example.nutrisiku.ui.components.SessionDropdown
import com.example.nutrisiku.ui.viewmodel.ManualInputViewModel

/**
 * Layar untuk memasukkan data makanan secara manual.
 * Memungkinkan pengguna menambahkan beberapa item makanan, porsi, kalori, dan gambar opsional.
 * Menangani konfirmasi sebelum keluar jika ada perubahan yang belum disimpan.
 *
 * @param viewModel ViewModel yang mengelola state dan logika input manual.
 * @param onBackClick Aksi untuk kembali ke layar sebelumnya.
 * @param onSaveSuccess Aksi yang dipanggil setelah penyimpanan berhasil (untuk navigasi).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(
    viewModel: ManualInputViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDiscardConfirmationDialog by remember { mutableStateOf(false) }

    // State untuk mengelola dialog konfirmasi hapus item
    var itemIndexToDelete by remember { mutableStateOf<Int?>(null) }

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
                        viewModel.discardChanges() // Panggil discard di ViewModel
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


    // Launcher untuk memilih gambar dari galeri
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                viewModel.onImageSelected(bitmap.copy(Bitmap.Config.ARGB_8888, true))
            }
        }
    )

    // Dialog konfirmasi hapus item makanan
    if (itemIndexToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemIndexToDelete = null },
            title = { Text(stringResource(R.string.delete_food_item_dialog_title)) },
            text = { Text(stringResource(R.string.delete_food_item_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemIndexToDelete?.let { viewModel.removeFoodItem(it) }
                        itemIndexToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.button_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemIndexToDelete = null }) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }


    // Tampilkan Snackbar jika ada pesan error
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.errorMessageShown()
        }
    }

    // Panggil onSaveSuccess jika penyimpanan berhasil
    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onSaveSuccess()
            // Reset flag isSaveSuccess di ViewModel (opsional, tergantung alur navigasi)
            // viewModel.resetSaveSuccessFlag() // Jika diperlukan
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.manual_input_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        // Cek perubahan sebelum navigasi kembali via ikon
                        if (uiState.hasUnsavedChanges) {
                            showDiscardConfirmationDialog = true
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        },
        bottomBar = {
            // Tombol "Tambah Makanan" dan "Simpan"
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedButton( // Ubah menjadi OutlinedButton agar beda dari Simpan
                    onClick = { viewModel.addFoodItem() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp) // Samakan bentuk
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_other_food_button))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_other_food_button))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.saveManualEntry() }, // Panggil fungsi save di ViewModel
                    enabled = uiState.isSaveButtonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.button_save), fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp), // Beri padding bawah
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pemilih gambar
            item {
                ImagePicker(
                    bitmap = uiState.selectedBitmap,
                    onImageClick = { imagePickerLauncher.launch("image/*") }
                )
            }

            // Daftar kartu item makanan
            itemsIndexed(uiState.foodItems, key = { _, item -> item.id }) { index, item ->
                ManualFoodItemCard(
                    item = item,
                    onItemChange = { updatedItem -> viewModel.onFoodItemChange(index, updatedItem) },
                    onRemoveClick = { itemIndexToDelete = index },
                    isLastItem = uiState.foodItems.size <= 1
                )
            }

            // Dropdown sesi makan
            item {
                SessionDropdown(
                    selectedSession = uiState.sessionLabel,
                    onSessionSelected = viewModel::onSessionLabelChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Spacer di akhir untuk memastikan tidak tertutup bottom bar
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

