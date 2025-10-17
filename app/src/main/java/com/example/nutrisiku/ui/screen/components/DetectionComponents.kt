package com.example.nutrisiku.ui.screen.components

import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.nutrisiku.R
import com.example.nutrisiku.data.DetectionResult
import com.example.nutrisiku.ui.viewmodel.DetectedFoodItem
import com.example.nutrisiku.ui.viewmodel.RealtimeUiState
import java.util.concurrent.Executors
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider


/**
 * Menampilkan pratinjau kamera secara real-time dan overlay untuk bounding box.
 *
 * @param realtimeUiState State UI real-time yang berisi hasil deteksi dan dimensi gambar.
 * @param onFrameAnalyzed Lambda yang akan dipanggil untuk setiap frame dari kamera.
 * @param modifier Modifier untuk komponen ini.
 */
@Composable
fun RealtimeCameraView(
    realtimeUiState: RealtimeUiState,
    onFrameAnalyzed: (ImageProxy) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    // Versi baru yang lebih aman
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        onFrameAnalyzed(imageProxy)
                    }
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                // Tangani error, misal dengan logging
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = modifier) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        OverlayCanvas(
            results = realtimeUiState.rawDetections,
            sourceBitmapWidth = realtimeUiState.sourceBitmapWidth,
            sourceBitmapHeight = realtimeUiState.sourceBitmapHeight,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Canvas transparan untuk menggambar bounding box di atas pratinjau kamera.
 *
 * @param results Daftar hasil deteksi untuk digambar.
 * @param sourceBitmapWidth Lebar bitmap asli yang dianalisis.
 * @param sourceBitmapHeight Tinggi bitmap asli yang dianalisis.
 * @param modifier Modifier untuk komponen ini.
 */
@Composable
fun OverlayCanvas(
    results: List<DetectionResult>,
    sourceBitmapWidth: Int,
    sourceBitmapHeight: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (results.isEmpty() || sourceBitmapWidth <= 1 || sourceBitmapHeight <= 1) {
            return@Canvas
        }

        val canvasWidth = size.width
        val canvasHeight = size.height
        val bitmapAspectRatio = sourceBitmapWidth.toFloat() / sourceBitmapHeight.toFloat()
        val canvasAspectRatio = canvasWidth / canvasHeight

        val scale: Float
        val offsetX: Float
        val offsetY: Float

        if (bitmapAspectRatio > canvasAspectRatio) {
            scale = canvasHeight / sourceBitmapHeight.toFloat()
            offsetX = (canvasWidth - sourceBitmapWidth * scale) / 2f
            offsetY = 0f
        } else {
            scale = canvasWidth / sourceBitmapWidth.toFloat()
            offsetY = (canvasHeight - sourceBitmapHeight * scale) / 2f
            offsetX = 0f
        }

        results.forEach { result ->
            val rect = result.boundingBox
            val scaledLeft = rect.left * scale + offsetX
            val scaledTop = rect.top * scale + offsetY
            val scaledWidth = rect.width() * scale
            val scaledHeight = rect.height() * scale

            drawRect(
                color = Color.Red,
                topLeft = Offset(scaledLeft, scaledTop),
                size = Size(scaledWidth, scaledHeight),
                style = Stroke(width = 2.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.apply {
                val text = "${result.label} (${"%.2f".format(result.confidence)})"
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 16.sp.toPx()
                    textAlign = android.graphics.Paint.Align.LEFT
                }
                val bgPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.RED
                    style = android.graphics.Paint.Style.FILL
                }
                val textBounds = android.graphics.Rect()
                paint.getTextBounds(text, 0, text.length, textBounds)
                val textBgLeft = scaledLeft
                val textBgTop = scaledTop - textBounds.height() - 8.dp.toPx()
                val textBgRight = scaledLeft + textBounds.width() + 8.dp.toPx()
                val textBgBottom = scaledTop
                drawRect(textBgLeft, textBgTop, textBgRight, textBgBottom, bgPaint)
                drawText(text, textBgLeft + 4.dp.toPx(), textBgBottom - 4.dp.toPx(), paint)
            }
        }
    }
}

/**
 * Kartu di bagian bawah layar deteksi yang menampilkan hasil real-time dan tombol aksi.
 *
 * @param realtimeUiState State UI real-time dari ViewModel.
 * @param onLockToggle Lambda yang dipanggil saat tombol kunci/buka kunci diklik.
 * @param onManualClick Lambda yang dipanggil saat tombol input manual diklik.
 * @param onGalleryClick Lambda yang dipanggil saat tombol galeri diklik.
 * @param modifier Modifier untuk komponen ini.
 */
@Composable
fun DetectionResultCard(
    realtimeUiState: RealtimeUiState,
    onLockToggle: (DetectedFoodItem) -> Unit,
    onManualClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.detection_result_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (realtimeUiState.groupedItems.isNotEmpty()) {
                    realtimeUiState.groupedItems.forEach { item ->
                        RealtimeDetectionResultItem(
                            item = item,
                            onLockToggle = { onLockToggle(item) }
                        )
                    }
                } else {
                    Text(stringResource(R.string.point_camera_prompt), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.total_locked_calories_label), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.kcal_value, realtimeUiState.totalLockedCalories),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onGalleryClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.Image, contentDescription = stringResource(R.string.button_open_gallery))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.button_open_gallery))
                }
                Button(onClick = onManualClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.button_manual_input))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.button_manual_input))
                }
            }
        }
    }
}

/**
 * Baris untuk menampilkan satu item makanan yang terdeteksi secara real-time.
 *
 * @param item Data item makanan yang akan ditampilkan.
 * @param onLockToggle Lambda yang dipanggil saat tombol kunci/buka kunci diklik.
 */
@Composable
fun RealtimeDetectionResultItem(
    item: DetectedFoodItem,
    onLockToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val displayText = if (item.quantity > 1) "${item.name} x${item.quantity}" else item.name
            Text(text = displayText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = stringResource(R.string.portion_value_g, item.standardPortion),
                style = MaterialTheme.typography.bodySmall, color = Color.Gray
            )
        }

        Text(
            text = stringResource(R.string.kcal_value, item.caloriesPerPortion * item.quantity),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onLockToggle) {
            Icon(
                imageVector = if (item.isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                contentDescription = if (item.isLocked) stringResource(R.string.content_desc_unlock) else stringResource(R.string.content_desc_lock),
                tint = if (item.isLocked) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}

