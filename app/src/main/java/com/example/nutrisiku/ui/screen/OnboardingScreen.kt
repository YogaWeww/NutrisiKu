@file:Suppress("DEPRECATION")

package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R // Pastikan Anda memiliki gambar placeholder di res/drawable
import com.example.nutrisiku.ui.theme.NutrisiKuTheme
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

// Data class untuk setiap halaman onboarding
data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)

// Daftar halaman onboarding
val onboardingPages = listOf(
    OnboardingPage(
        imageRes = R.drawable.onboarding_1, // Ganti dengan nama file ilustrasi Anda
        title = "Selamat Datang",
        description = "Pantau gizi dan jaga kesehatan Anda. Aplikasi ini membantu Anda melacak kalori dari makanan khas Indonesia dengan mudah."
    ),
    OnboardingPage(
        imageRes = R.drawable.onboarding_2, // Ganti dengan nama file ilustrasi Anda
        title = "Deteksi Kalori dari Foto",
        description = "Tak perlu lagi bingung. Cukup ambil foto makanan Anda, dan biarkan teknologi AI kami yang menganalisis dan menghitung estimasi kalorinya secara instan."
    ),
    OnboardingPage(
        imageRes = R.drawable.onboarding_3, // Ganti dengan nama file ilustrasi Anda
        title = "Lacak Riwayat & Capai Target",
        description = "Simpan riwayat makanan Anda, pantau total asupan kalori harian, dan sesuaikan dengan target yang dipersonalisasi untuk mencapai gaya hidup sehat Anda."
    )
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onFinishClick: () -> Unit
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    // PERUBAHAN: Menggunakan Box agar Pager bisa menjadi latar belakang
    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            count = onboardingPages.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            OnboardingPageContent(page = onboardingPages[pageIndex])
        }

        // Kontrol (indikator dan tombol) diletakkan di atas Pager
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                activeColor = Color.White,
                inactiveColor = Color.White.copy(alpha = 0.5f)
            )

            Button(
                onClick = {
                    if (pagerState.currentPage < onboardingPages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinishClick()
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (pagerState.currentPage < onboardingPages.size - 1) "Next" else "Mulai",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    // PERUBAHAN: Menggunakan Box untuk menumpuk gambar, overlay, dan teks
    Box(modifier = Modifier.fillMaxSize()) {
        // Lapisan 1: Gambar Latar Belakang
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null, // Gambar hanya dekoratif
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Memastikan gambar mengisi seluruh layar
        )

        // Lapisan 2: Overlay Gelap
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)) // Sesuaikan alpha untuk tingkat kegelapan
        )

        // Lapisan 3: Konten Teks
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Posisikan teks lebih ke bawah agar tidak tertutup ilustrasi utama
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            // Memberi ruang agar tidak terlalu mepet dengan tombol navigasi
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

// Catatan: Untuk menggunakan Pager, Anda perlu menambahkan dependency Accompanist
// di build.gradle.kts (Module :app)
// implementation("com.google.accompanist:accompanist-pager:0.32.0")
// implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")
// Versi mungkin perlu disesuaikan.

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun OnboardingScreenPreview() {
    NutrisiKuTheme {
        OnboardingScreen(onFinishClick = {})
    }
}