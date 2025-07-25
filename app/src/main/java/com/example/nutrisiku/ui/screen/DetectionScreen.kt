package com.example.nutrisiku.ui.screen

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nutrisiku.R // Pastikan Anda memiliki gambar placeholder
import com.example.nutrisiku.ui.screen.components.DetectedItem
import com.example.nutrisiku.ui.theme.NutrisiKuTheme
import com.example.nutrisiku.ui.viewmodel.DetectionViewModel

// --- 1. Halaman Awal Deteksi (Pilihan Input) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel,
    onBackClick: () -> Unit,
    onCameraClick: () -> Unit,
    onManualClick: () -> Unit,
    navigateToResult: () -> Unit
) {
    val context = LocalContext.current

    // Launcher untuk memilih gambar dari galeri
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                // Konversi URI menjadi Bitmap
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                // Kirim bitmap ke ViewModel dan navigasi ke halaman hasil
                viewModel.onImageSelected(bitmap.copy(Bitmap.Config.ARGB_8888, true))
                navigateToResult()
            }
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Deteksi", fontWeight = FontWeight.Bold) },
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
            // Placeholder untuk pratinjau gambar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Placeholder Gambar",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Pilihan Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OptionButton(
                    text = "Kamera",
                    icon = Icons.Default.CameraAlt,
                    onClick = onCameraClick, // Terhubung ke navigasi kamera
                    modifier = Modifier.weight(1f)
                )
                OptionButton(
                    text = "Galeri",
                    icon = Icons.Default.PhotoLibrary,
                    onClick = { imagePickerLauncher.launch("image/*") }, // Memicu launcher galeri
                    modifier = Modifier.weight(1f)
                )
                OptionButton(
                    text = "Manual",
                    icon = Icons.Default.Edit,
                    onClick = onManualClick, // Terhubung ke navigasi input manual
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun OptionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = text)
            Text(text)
        }
    }
}

//// --- Previews ---
//@Preview(showBackground = true, device = "id:pixel_5")
//@Composable
//fun DetectionScreenPreview() {
//    NutrisiKuTheme {
//        DetectionScreen({}, {}, {}, {})
//    }
//}
//
//@Preview(showBackground = true, device = "id:pixel_5")
//@Composable
//fun DetectionResultScreenPreview() {
//    NutrisiKuTheme {
//        DetectionResultScreen({}, {})
//    }
//}
//
//@Preview(showBackground = true, device = "id:pixel_5")
//@Composable
//fun ManualInputScreenPreview() {
//    NutrisiKuTheme {
//        ManualInputScreen({}, {})
//    }
//}