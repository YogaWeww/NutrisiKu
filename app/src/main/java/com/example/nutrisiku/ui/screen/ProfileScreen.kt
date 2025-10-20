package com.example.nutrisiku.ui.screen

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.navigation.Screen
import com.example.nutrisiku.ui.components.CalorieCard
import com.example.nutrisiku.ui.components.EditProfileButton
import com.example.nutrisiku.ui.components.NutrisiKuBottomNavBar
import com.example.nutrisiku.ui.viewmodel.HistoryViewModel
import com.example.nutrisiku.ui.viewmodel.ProfileViewModel
import java.io.File

/**
 * Layar Profil Pengguna.
 * Menampilkan ringkasan data pengguna, status kalori, dan menyediakan navigasi
 * untuk mengedit profil dan mengelola foto profil.
 *
 * @param profileViewModel ViewModel untuk data profil.
 * @param historyViewModel ViewModel untuk data riwayat (diperlukan untuk kalori harian).
 * @param onEditProfileClick Aksi navigasi ke layar Edit Profil.
 * @param onBackClick Aksi untuk kembali ke layar sebelumnya.
 * @param navigateToHome Aksi navigasi ke layar Home.
 * @param navigateToDetection Aksi navigasi ke layar Deteksi.
 * @param navigateToHistory Aksi navigasi ke layar Riwayat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    historyViewModel: HistoryViewModel,
    onEditProfileClick: () -> Unit,
    onBackClick: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToDetection: () -> Unit,
    navigateToHistory: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val todaysCalories by historyViewModel.todaysCalories.collectAsState()
    val context = LocalContext.current

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
                profileViewModel.onProfileImageChanged(bitmap.copy(Bitmap.Config.ARGB_8888, true))
            }
        }
    )

    // Bottom sheet untuk opsi ganti/hapus foto profil
    if (uiState.showProfileImageOptions) {
        ModalBottomSheet(onDismissRequest = profileViewModel::onDismissProfileImageOptions) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                // Opsi "Ubah foto"
                ListItem(
                    headlineContent = { Text(stringResource(R.string.change_profile_photo_option)) },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
                    modifier = Modifier.clickable {
                        imagePickerLauncher.launch("image/*")
                        profileViewModel.onDismissProfileImageOptions()
                    }
                )
                // Opsi "Hapus foto" (hanya jika ada foto)
                if (uiState.imagePath.isNotEmpty()) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.delete_profile_photo_option), color = MaterialTheme.colorScheme.error) },
                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable {
                            profileViewModel.onDeleteProfileImage()
                        }
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.profile_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back))
                    }
                }
            )
        },
        bottomBar = {
            NutrisiKuBottomNavBar(
                currentRoute = Screen.Profile.route,
                onHomeClick = navigateToHome,
                onDetectionClick = navigateToDetection,
                onHistoryClick = navigateToHistory
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Gambar profil yang bisa diklik
            AsyncImage(
                model = if (uiState.imagePath.isNotEmpty()) File(uiState.imagePath) else R.drawable.default_profile,
                contentDescription = stringResource(R.string.content_desc_profile_picture),
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { profileViewModel.onProfileImageClicked() },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = uiState.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            CalorieCard(
                total = uiState.tdee,
                consumed = todaysCalories
            )

            Spacer(modifier = Modifier.height(16.dp))

            EditProfileButton(onClick = onEditProfileClick)
        }
    }
}
