@file:Suppress("DEPRECATION")

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
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.ui.screen.components.DetectionResultCard
import com.example.nutrisiku.ui.screen.components.PermissionDeniedView
import com.example.nutrisiku.ui.screen.components.RealtimeCameraView
import com.example.nutrisiku.ui.viewmodel.DetectionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

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
    // PERUBAHAN: Ambil state dari realtimeUiState
    val realtimeUiState by viewModel.realtimeUiState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                viewModel.onImageSelected(bitmap.copy(Bitmap.Config.ARGB_8888, true))
                navigateToResult()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Deteksi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.confirmRealtimeDetection()
                            navigateToResult()
                        },
                        // PERUBAHAN: Tombol aktif berdasarkan state dari ViewModel
                        enabled = realtimeUiState.isConfirmEnabled
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Konfirmasi Deteksi")
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
            if (cameraPermissionState.status.isGranted) {
                RealtimeCameraView(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            } else {
                PermissionDeniedView(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                )
            }

            DetectionResultCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                viewModel = viewModel,
                onManualClick = onManualClick,
                onGalleryClick = { imagePickerLauncher.launch("image/*") }
            )
        }
    }
}