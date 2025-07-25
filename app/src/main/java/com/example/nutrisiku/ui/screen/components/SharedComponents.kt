package com.example.nutrisiku.ui.screen.components

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.R // PERBAIKAN: Tambahkan import ini
import java.io.File

@Composable
fun DetectedItem(name: String, calorie: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(name, style = MaterialTheme.typography.bodyLarge)
            Text(calorie, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        IconButton(onClick = { /* TODO: Aksi ubah porsi/hapus */ }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Opsi")
        }
    }
}

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
            // PERUBAHAN: Gunakan AsyncImage untuk memuat gambar dari file
            AsyncImage(
                model = File(imagePath), // Muat gambar dari file
                contentDescription = session,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo_nutrisiku) // Tampilkan logo saat memuat
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
fun GenderDropdown() {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedGender by remember { mutableStateOf("Pria") }
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
                        selectedGender = gender
                        isExpanded = false
                    }
                )
            }
        }
    }
}

// PERBAIKAN: Menambahkan parameter ke ActivityLevelDropdown
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
            leadingIcon = { Icon(Icons.Default.DirectionsRun, contentDescription = null) },
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
                        onActivitySelected(activity) // Panggil fungsi dari ViewModel
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ImageWithBoundingBoxes(
    bitmap: Bitmap,
    detectionResults: List<Screen.DetectionResult>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Hasil Deteksi",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Canvas untuk menggambar bounding box di atas gambar
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / bitmap.width
            val scaleY = size.height / bitmap.height

            detectionResults.forEach { result ->
                val rect = result.boundingBox
                drawRect(
                    color = Color.Red, // Anda bisa menggunakan warna dari tema
                    topLeft = androidx.compose.ui.geometry.Offset(rect.left * scaleX, rect.top * scaleY),
                    size = androidx.compose.ui.geometry.Size((rect.right - rect.left) * scaleX, (rect.bottom - rect.top) * scaleY),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
