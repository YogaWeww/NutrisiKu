package com.example.nutrisiku.ui.screen

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.components.DetectionResultCard
import com.example.nutrisiku.ui.components.PermissionDeniedView
import com.example.nutrisiku.ui.components.RealtimeCameraView
import com.example.nutrisiku.ui.viewmodel.DetectionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Layar utama untuk deteksi makanan.
 * Menampilkan pratinjau kamera real-time, menangani izin kamera,
 * dan menyediakan opsi untuk beralih ke galeri atau input manual.
 *
 * @param viewModel ViewModel yang mengelola state dan logika deteksi.
 * @param onBackClick Aksi untuk kembali ke layar sebelumnya.
 * @param onManualClick Aksi untuk navigasi ke layar input manual.
 * @param navigateToResult Aksi untuk navigasi ke layar hasil deteksi.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel,
    onBackClick: () -> Unit,
    onManualClick: () -> Unit,
    navigateToResult: () -> Unit
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val realtimeUiState by viewModel.realtimeUiState.collectAsState()

    // Launcher untuk memilih gambar dari galeri.
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                // Konversi URI ke Bitmap. Metode berbeda untuk API level yang berbeda.
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                // Kirim bitmap ke ViewModel dan navigasi ke layar hasil.
                viewModel.onImageSelected(bitmap.copy(Bitmap.Config.ARGB_8888, true))
                navigateToResult()
            }
        }
    )

    // Meminta izin kamera saat layar pertama kali ditampilkan.
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.detection_screen_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                },
                actions = {
                    // Tombol konfirmasi hanya aktif jika ada item yang dikunci.
                    IconButton(
                        onClick = {
                            viewModel.confirmRealtimeDetection()
                            navigateToResult()
                        },
                        enabled = realtimeUiState.isConfirmEnabled
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.button_confirm))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tampilkan pratinjau kamera atau pesan penolakan izin.
            if (cameraPermissionState.status.isGranted) {
                // PERBAIKAN: Panggil RealtimeCameraView dengan parameter yang benar
                RealtimeCameraView(
                    realtimeUiState = realtimeUiState,
                    onFrameAnalyzed = viewModel::analyzeFrame,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PermissionDeniedView(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                )
            }

            DetectionResultCard(
                realtimeUiState = realtimeUiState,
                onLockToggle = viewModel::toggleLockState,
                onManualClick = onManualClick,
                onGalleryClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

