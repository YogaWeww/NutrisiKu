// --- File: ui/screen/ProfileScreen.kt ---
// Anda bisa membuat file baru ini di dalam package ui.screen
package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.ui.screen.components.NutrisiKuBottomNavBar
import com.example.nutrisiku.ui.screen.components.ActivityLevelDropdown
import com.example.nutrisiku.ui.screen.components.GenderDropdown
import com.example.nutrisiku.ui.theme.NutrisiKuTheme

// Halaman utama untuk menampilkan profil
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onEditProfileClick: () -> Unit,
    onBackClick: () -> Unit, // Mungkin tidak diperlukan jika navigasi utama via BottomBar
    navigateToHome: () -> Unit, // Tambahkan ini
    navigateToDetection: () -> Unit, // Tambahkan ini
    navugateToHistory: () -> Unit // Tambahkan ini
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = { NutrisiKuBottomNavBar(
            onHomeClick = navigateToHome,
            onDetectionClick = navigateToDetection,
            onHistoryClick = navugateToHistory
        ) }
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

            // Placeholder Foto Profil
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Yoga", // Data dummy
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Kartu Kebutuhan Kalori
            CalorieProfileCard(total = 1500)

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Edit Profil
            EditProfileButton(onClick = onEditProfileClick)
        }
    }
}

@Composable
fun CalorieProfileCard(total: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                progress = 1.0f, // Di halaman profil, progress bar bisa dibuat penuh
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = Color.White
            )
            Text(
                text = "750 KKAL TERSISA", // Data dummy
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun EditProfileButton(onClick: () -> Unit) {
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
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Profil",
            tint = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Edit Profil",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

// Halaman untuk mengedit profil
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToDetection: () -> Unit,
    navugateToHistory: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Profil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = { NutrisiKuBottomNavBar(
            onHomeClick = navigateToHome,
            onDetectionClick = navigateToDetection,
            onHistoryClick = navugateToHistory
        ) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // PERUBAHAN: Menambahkan semua input field sesuai mockup
            OutlinedTextField(
                value = "Yoga", // Data dummy
                onValueChange = {},
                label = { Text("Nama / Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = "22", // Data dummy
                onValueChange = {},
                label = { Text("Umur (Tahun)") },
                leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = "65", // Data dummy
                onValueChange = {},
                label = { Text("Berat Badan (kg)") },
                leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = "175", // Data dummy
                onValueChange = {},
                label = { Text("Tinggi Badan (cm)") },
                leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Implementasi Dropdown untuk Jenis Kelamin
            GenderDropdown()
            Spacer(modifier = Modifier.height(8.dp))

            // Implementasi Dropdown untuk Tingkat Aktivitas
            ActivityLevelDropdown()

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Konfirmasi", fontWeight = FontWeight.Bold)
            }
        }
    }
}


//@Preview(showBackground = true, device = "id:pixel_5")
//@Composable
//fun ProfileScreenPreview() {
//    NutrisiKuTheme {
//        ProfileScreen(onEditProfileClick = {}, onBackClick = {})
//    }
//}
//
//@Preview(showBackground = true, device = "id:pixel_5")
//@Composable
//fun EditProfileScreenPreview() {
//    NutrisiKuTheme {
//        EditProfileScreen(onBackClick = {}, onSaveClick = {})
//    }
//}