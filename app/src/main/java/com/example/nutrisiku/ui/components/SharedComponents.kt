package com.example.nutrisiku.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nutrisiku.R
import com.example.nutrisiku.data.DetectionResult
import com.example.nutrisiku.ui.navigation.Screen
import java.io.File

/**
 * Bottom navigation bar kustom untuk aplikasi NutrisiKu.
 * Menampilkan tiga tujuan utama: Home, Deteksi (tengah, menonjol), dan Riwayat.
 *
 * @param currentRoute Rute saat ini, digunakan untuk menyorot item navigasi yang aktif.
 * @param onHomeClick Lambda yang akan dieksekusi saat item Home diklik.
 * @param onDetectionClick Lambda yang akan dieksekusi saat item Deteksi diklik.
 * @param onHistoryClick Lambda yang akan dieksekusi saat item Riwayat diklik.
 */
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
            icon = { Icon(Icons.Filled.Home, contentDescription = stringResource(R.string.home_screen_title)) },
            label = { Text(stringResource(R.string.home_screen_title)) },
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
                    contentDescription = stringResource(R.string.detection_screen_title),
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(8.dp),
                    tint = Color.White
                )
            },
            label = { Text(stringResource(R.string.detection_screen_title)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = currentRoute == Screen.History.route,
            onClick = onHistoryClick,
            icon = { Icon(Icons.Filled.History, contentDescription = stringResource(R.string.history_screen_title)) },
            label = { Text(stringResource(R.string.history_screen_title)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        )
    }
}

/**
 * Dropdown untuk memilih jenis kelamin.
 *
 * @param selectedGender Jenis kelamin yang saat ini dipilih.
 * @param onGenderSelected Lambda yang dipanggil dengan jenis kelamin baru saat dipilih.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderDropdown(
    selectedGender: String,
    onGenderSelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf(stringResource(R.string.gender_male), stringResource(R.string.gender_female))

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            value = selectedGender,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_gender)) },
            leadingIcon = { Icon(Icons.Default.Wc, contentDescription = null) },
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
                    }
                )
            }
        }
    }
}

/**
 * Dropdown untuk memilih tingkat aktivitas harian.
 *
 * @param selectedActivity Tingkat aktivitas yang saat ini dipilih.
 * @param onActivitySelected Lambda yang dipanggil dengan tingkat aktivitas baru saat dipilih.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLevelDropdown(
    selectedActivity: String,
    onActivitySelected: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val activityOptions = listOf(
        stringResource(R.string.activity_level_1),
        stringResource(R.string.activity_level_2),
        stringResource(R.string.activity_level_3),
        stringResource(R.string.activity_level_4),
        stringResource(R.string.activity_level_5)
    )

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it }
    ) {
        OutlinedTextField(
            value = selectedActivity,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_activity_level)) },
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
                    }
                )
            }
        }
    }
}


/**
 * Menampilkan gambar hasil deteksi beserta bounding box di atasnya.
 *
 * @param bitmap Bitmap yang akan ditampilkan.
 * @param detectionResults Daftar hasil deteksi untuk digambar.
 * @param modifier Modifier untuk komponen ini.
 */
@Composable
fun ImageResult(
    bitmap: Bitmap,
    detectionResults: List<DetectionResult>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.detection_result_image_desc),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}


/**
 * Kartu yang menampilkan kebutuhan kalori harian, yang sudah dikonsumsi, dan sisanya.
 *
 * @param total Total kebutuhan kalori harian.
 * @param consumed Jumlah kalori yang sudah dikonsumsi.
 * @param modifier Modifier untuk komponen ini.
 */
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
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.daily_calorie_needs_label),
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
                    text = stringResource(R.string.kcal_unit),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                )
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = stringResource(R.string.content_desc_calories),
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
                stringResource(R.string.calorie_needs_met)
            } else {
                stringResource(R.string.calories_remaining_label, remaining)
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


/**
 * Komponen untuk menambah dan mengurangi jumlah (kuantitas).
 *
 * @param quantity Jumlah saat ini.
 * @param onDecrement Lambda yang dieksekusi saat tombol kurang diklik.
 * @param onIncrement Lambda yang dieksekusi saat tombol tambah diklik.
 * @param modifier Modifier untuk komponen ini.
 */
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
            Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.content_desc_decrement))
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
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.content_desc_increment))
        }
    }
}

/**
 * Tampilan yang akan muncul jika izin kamera ditolak.
 *
 * @param onRequestPermission Lambda untuk meminta kembali izin kamera.
 */
@Composable
fun PermissionDeniedView(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.camera_permission_denied_message))
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRequestPermission) {
            Text(stringResource(R.string.grant_permission_button))
        }
    }
}

/**
 * Dialog untuk mengedit porsi makanan dalam gram.
 *
 * @param currentPortion Nilai porsi saat ini.
 * @param onDismiss Lambda yang dieksekusi saat dialog ditutup.
 * @param onConfirm Lambda yang dieksekusi saat tombol simpan diklik, membawa nilai porsi baru.
 */
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
        title = { Text(stringResource(R.string.edit_portion_dialog_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.portion_in_grams_label)) },
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
                Text(stringResource(R.string.button_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * Dropdown untuk memilih sesi makan.
 *
 * @param selectedSession Sesi yang saat ini dipilih.
 * @param onSessionSelected Lambda yang dipanggil saat sesi baru dipilih.
 * @param modifier Modifier untuk komponen ini.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDropdown(
    selectedSession: String,
    onSessionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val sessionOptions = listOf(
        stringResource(R.string.session_breakfast),
        stringResource(R.string.session_lunch),
        stringResource(R.string.session_dinner),
        stringResource(R.string.session_snack)
    )

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedSession,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.save_as_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            sessionOptions.forEach { session ->
                DropdownMenuItem(
                    text = { Text(session) },
                    onClick = {
                        onSessionSelected(session)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

