package com.example.nutrisiku.ui.screen.components

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ButtonDefaults
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Divider
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.OutlinedButton
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.nutrisiku.R
import com.example.nutrisiku.data.DetectionResult
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.viewmodel.DetectionViewModel
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

@Composable
fun NutrisiKuBottomNavBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onDetectionClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.shadow(elevation = 8.dp)
    ) {
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = onHomeClick,
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = onDetectionClick,
            icon = {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = "Deteksi",
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(8.dp),
                    tint = Color.White
                )
            },
            label = { Text("Deteksi") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = currentRoute == Screen.History.route,
            onClick = onHistoryClick,
            icon = { Icon(Icons.Filled.History, contentDescription = "Riwayat") },
            label = { Text("Riwayat") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun DateHeader(date: String, totalCalorie: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background) // Latar belakang agar tidak transparan
            .padding(vertical = 8.dp)
    ) {
        Text(text = date, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = totalCalorie, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryEntryCard(
    imagePath: String,
    session: String,
    totalCalorie: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = session,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo_nutrisiku)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = session, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Total: $totalCalorie KKAL", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}

// PERBAIKAN: GenderDropdown dipindahkan ke sini dan diberi parameter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderDropdown(
    selectedGender: String,
    onGenderSelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Pria", "Wanita")

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            value = selectedGender,
            onValueChange = {},
            readOnly = true,
            label = { androidx.compose.material3.Text("Jenis Kelamin") },
            leadingIcon = {
                androidx.compose.material3.Icon(
                    Icons.Default.Wc,
                    contentDescription = null
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            genderOptions.forEach { gender ->
                DropdownMenuItem(
                    text = { androidx.compose.material3.Text(gender) },
                    onClick = {
                        onGenderSelected(gender)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLevelDropdown(
    selectedActivity: String,
    onActivitySelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val activityOptions = listOf("Jarang Olahraga", "Aktivitas Ringan", "Aktivitas Sedang", "Sangat Aktif", "Ekstra Aktif")

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            value = selectedActivity,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tingkat Aktivitas Harian") },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            activityOptions.forEach { activity ->
                DropdownMenuItem(
                    text = { Text(activity) },
                    onClick = {
                        onActivitySelected(activity)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

// --- FUNGSI BARU DITAMBAHKAN DI SINI ---
// Komponen ini bertanggung jawab untuk menampilkan gambar
// dan menggambar bounding box di atasnya dengan penskalaan yang benar.
@Composable
fun ImageWithBoundingBoxes(
    bitmap: Bitmap,
    detectionResults: List<DetectionResult>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Hasil Deteksi",
            // PERBAIKAN KUNCI: Samakan ContentScale dengan pratinjau kamera
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val originalWidth = bitmap.width.toFloat()
            val originalHeight = bitmap.height.toFloat()

            // Logika penskalaan yang sama dengan OverlayCanvas, tetapi untuk ContentScale.Crop
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Hitung skala agar gambar memenuhi canvas (Crop)
            val scale = max(canvasWidth / originalWidth, canvasHeight / originalHeight)

            val offsetX = (canvasWidth - originalWidth * scale) / 2
            val offsetY = (canvasHeight - originalHeight * scale) / 2

            detectionResults.forEach { result ->
                val rect = result.boundingBox

                // Model memberikan koordinat relatif terhadap 320x320
                val modelInputWidth = 320f
                val modelInputHeight = 320f

                // Ubah koordinat model ke koordinat gambar asli
                val displayLeft = rect.left / modelInputWidth * originalWidth
                val displayTop = rect.top / modelInputHeight * originalHeight
                val displayRight = rect.right / modelInputWidth * originalWidth
                val displayBottom = rect.bottom / modelInputHeight * originalHeight

                // Terapkan skala dan offset canvas
                val scaledLeft = displayLeft * scale + offsetX
                val scaledTop = displayTop * scale + offsetY
                val scaledRight = displayRight * scale + offsetX
                val scaledBottom = displayBottom * scale + offsetY

                drawRect(
                    color = Color.Red,
                    topLeft = Offset(scaledLeft, scaledTop),
                    size = Size(scaledRight - scaledLeft, scaledBottom - scaledTop),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun CalorieCard(
    total: Int,
    consumed: Int,
    modifier: Modifier = Modifier
) {
    val remaining = total - consumed
    val progress = if (total > 0) consumed.toFloat() / total.toFloat() else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Kebutuhan Kalori Harian:",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = total.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " KKAL",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                )
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = "Kalori",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = Color.White
            )
            Text(
                text = "$remaining KKAL TERSISA",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            )
        }
    }
}
@Composable
fun RealtimeCameraView(
    modifier: Modifier = Modifier,
    viewModel: DetectionViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val detectionResults by viewModel.realtimeResults.collectAsState()
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
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        viewModel.analyzeFrame(imageProxy)
                    }
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
            } catch (_: Exception) {
                // Handle error
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = modifier) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
        OverlayCanvas(
            results = detectionResults,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun OverlayCanvas(
    results: List<DetectionResult>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val modelInputWidth = 320f
        val modelInputHeight = 320f
        val canvasWidth = size.width
        val canvasHeight = size.height
        val scaleW = canvasWidth / modelInputWidth
        val scaleH = canvasHeight / modelInputHeight
        val scale = min(scaleW, scaleH)
        val offsetX = (canvasWidth - modelInputWidth * scale) / 2
        val offsetY = (canvasHeight - modelInputHeight * scale) / 2

        results.forEach { result ->
            val rect = result.boundingBox
            val scaledLeft = rect.left * scale + offsetX
            val scaledTop = rect.top * scale + offsetY
            val scaledRight = rect.right * scale + offsetX
            val scaledBottom = rect.bottom * scale + offsetY

            drawRect(
                color = Color.Red,
                topLeft = Offset(scaledLeft, scaledTop),
                size = Size(scaledRight - scaledLeft, scaledBottom - scaledTop),
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

@Composable
fun DetectionResultCard(
    modifier: Modifier = Modifier,
    viewModel: DetectionViewModel,
    onManualClick: () -> Unit,
    onGalleryClick: () -> Unit // Parameter baru
) {
    val uiState by viewModel.realtimeUiState.collectAsState()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Hasil Deteksi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            if (uiState.detectedItems.isEmpty()) {
                Text(
                    "Arahkan kamera ke makanan...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                uiState.detectedItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.name} (${item.standardPortion}g)")
                        Text("${item.calories} KKAL")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Estimasi Kalori", fontWeight = FontWeight.Bold)
                Text("${uiState.totalCalories} KKAL", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // PERBAIKAN: Tambahkan Row untuk dua tombol
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onGalleryClick, // Hubungkan aksi klik
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Galeri", modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Galeri")
                }
                OutlinedButton(
                    onClick = onManualClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Input Manual", modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Manual")
                }
            }
        }
    }
}

@Composable
fun PermissionDeniedView(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Izin kamera diperlukan untuk fitur ini.")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRequestPermission) {
            Text("Berikan Izin")
        }
    }
}

