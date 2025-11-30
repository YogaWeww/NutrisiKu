package com.example.nutrisiku.ui.screen

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import com.example.nutrisiku.ui.screen.components.ImagePicker
import com.example.nutrisiku.ui.screen.components.ManualFoodItemCard
import com.example.nutrisiku.ui.screen.components.SessionDropdown
import com.example.nutrisiku.ui.viewmodel.ManualInputViewModel

/**
 * Layar untuk menginput data makanan secara manual.
 * Pengguna dapat menambahkan beberapa item makanan, mengedit detailnya, dan menyimpannya.
 *
 * @param viewModel ViewModel yang mengelola state dan logika untuk input manual.
 * @param onBackClick Aksi yang dipanggil saat tombol kembali ditekan.
 * @param onSaveSuccess Aksi yang dipanggil setelah data berhasil disimpan.
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
    var itemIndexToDelete by remember { mutableStateOf<Int?>(null) }

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

    // Dialog konfirmasi hapus item
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

    // Efek untuk menampilkan Snackbar saat ada pesan error
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.errorMessageShown()
        }
    }

    // Efek untuk memicu navigasi saat penyimpanan berhasil
    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onSaveSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.manual_input_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        },
        bottomBar = {
            // Tombol-tombol aksi di bagian bawah layar
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedButton(
                    onClick = { viewModel.addFoodItem() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_other_food_button))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_other_food_button))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.saveManualEntry() },
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
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ImagePicker(
                    bitmap = uiState.selectedBitmap,
                    onImageClick = { imagePickerLauncher.launch("image/*") }
                )
            }

            itemsIndexed(uiState.foodItems) { index, item ->
                ManualFoodItemCard(
                    item = item,
                    onItemChange = { updatedItem -> viewModel.onFoodItemChange(index, updatedItem) },
                    onRemoveClick = { itemIndexToDelete = index },
                    isLastItem = uiState.foodItems.size <= 1
                )
            }

            item {
                SessionDropdown(
                    selectedSession = uiState.sessionLabel,
                    onSessionSelected = viewModel::onSessionLabelChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
