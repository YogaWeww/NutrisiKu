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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.nutrisiku.R
import com.example.nutrisiku.data.DetectionResult
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.viewmodel.DetectedFoodItem
import com.example.nutrisiku.ui.viewmodel.DetectionViewModel
import java.io.File
import java.util.concurrent.Executors
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
                model = if (imagePath.isNotEmpty()) File(imagePath) else R.drawable.logo_nutrisiku,
                contentDescription = session,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo_nutrisiku),
                error = painterResource(id = R.drawable.logo_nutrisiku)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = session, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Total: $totalCalorie KKAL", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}

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
            label = { Text("Jenis Kelamin") },
            leadingIcon = {
                Icon(
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
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            genderOptions.forEach { gender ->
                DropdownMenuItem(
                    text = { Text(gender) },
                    onClick = {
                        onGenderSelected(gender)
                        isExpanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
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
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            activityOptions.forEach { activity ->
                DropdownMenuItem(
                    text = { Text(activity) },
                    onClick = {
                        onActivitySelected(activity)
                        isExpanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
fun ImageResult(
    bitmap: Bitmap,
    detectionResults: List<DetectionResult>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Hasil Deteksi",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CalorieCard(
    total: Int,
    consumed: Int,
    modifier: Modifier = Modifier
) {
    val remaining = total - consumed
    val progress = if (total > 0) (consumed.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f

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

            val remainingText = if (remaining <= 0) {
                "Kebutuhan kalori harian terpenuhi"
            } else {
                "$remaining KKAL TERSISA"
            }

            Text(
                text = remainingText,
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
    val realtimeUiState by viewModel.realtimeUiState.collectAsState()
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
            results = realtimeUiState.rawDetections,
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
    onGalleryClick: () -> Unit
) {
    val realtimeUiState by viewModel.realtimeUiState.collectAsState()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Hasil Deteksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                            onLockToggle = { viewModel.toggleLockState(item) }
                        )
                    }
                } else {
                    Text("Arahkan kamera ke makanan Anda...", style = MaterialTheme.typography.bodyMedium)
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
                Text("Total Kalori Terkunci:", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("${realtimeUiState.totalLockedCalories} Kkal", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onGalleryClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.Image, contentDescription = "Galeri")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galeri")
                }
                Button(onClick = onManualClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.Edit, contentDescription = "Input Manual")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manual")
                }
            }
        }
    }
}

@Composable
fun RealtimeDetectionResultItem(
    item: DetectedFoodItem,
    onLockToggle: () -> Unit,
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
            Text(text = "${item.standardPortion}g", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        Text(text = "${item.caloriesPerPortion * item.quantity} Kkal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onLockToggle) {
            Icon(
                imageVector = if (item.isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                contentDescription = if (item.isLocked) "Buka Kunci" else "Kunci",
                tint = if (item.isLocked) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}

@Composable
fun QuantityEditor(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(28.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Kurangi Jumlah")
        }

        Text(
            text = quantity.toString(),
            modifier = Modifier.padding(horizontal = 12.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(28.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Jumlah")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortionEditDialog(
    currentPortion: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(currentPortion.toString()) }
    val isError = text.toIntOrNull() == null || text.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Porsi (gram)") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Porsi dalam gram") },
                singleLine = true,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val newPortion = text.toIntOrNull()
                    if (newPortion != null) {
                        onConfirm(newPortion)
                    }
                },
                enabled = !isError
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

