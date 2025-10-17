package com.example.nutrisiku.ui.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nutrisiku.R
import com.example.nutrisiku.data.HistoryEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Bagian header di HomeScreen, menampilkan sapaan dan gambar profil.
 *
 * @param name Nama pengguna yang akan disapa.
 * @param imagePath Path lokal ke gambar profil pengguna.
 * @param onProfileClick Aksi yang dipanggil saat gambar profil diklik.
 */
@Composable
fun HeaderSection(
    name: String,
    imagePath: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.home_greeting, name.ifEmpty { "Pengguna" }),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        AsyncImage(
            model = if (imagePath.isNotEmpty()) File(imagePath) else R.drawable.default_profile,
            contentDescription = stringResource(R.string.content_desc_profile_picture),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onProfileClick),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Kartu kecil yang menampilkan tanggal hari ini.
 */
@Composable
fun DateCard() {
    val currentDate = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = null, // Deskripsi diberikan oleh teks di sebelahnya
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Tombol utama di HomeScreen untuk memulai alur deteksi makanan.
 *
 * @param onClick Aksi yang dipanggil saat tombol diklik.
 */
@Composable
fun DetectNowButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = null, // Deskripsi diberikan oleh teks di sebelahnya
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.detect_now_button),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * Bagian yang menampilkan entri riwayat terbaru dan tombol untuk melihat semua riwayat.
 *
 * @param latestHistory Entri riwayat terbaru untuk ditampilkan, bisa null jika tidak ada.
 * @param onSeeAllClick Aksi yang dipanggil saat tombol "Lihat Semua" diklik.
 * @param onLatestHistoryClick Aksi yang dipanggil saat kartu riwayat terbaru diklik.
 */
@Composable
fun HistorySection(
    latestHistory: HistoryEntity?,
    onSeeAllClick: () -> Unit,
    onLatestHistoryClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_recent_history),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (latestHistory != null) {
            HistoryEntryCard(
                imagePath = latestHistory.imagePath,
                session = latestHistory.sessionLabel,
                totalCalorie = latestHistory.totalCalories,
                onClick = onLatestHistoryClick
            )
        } else {
            Text(stringResource(R.string.home_no_history))
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onSeeAllClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.home_view_all),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
