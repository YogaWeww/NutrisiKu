package com.example.nutrisiku.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R

/**
 * Model data yang merepresentasikan konten untuk satu halaman onboarding.
 *
 * @param imageRes Resource ID untuk gambar ilustrasi.
 * @param titleRes Resource ID untuk teks judul.
 * @param descriptionRes Resource ID untuk teks deskripsi.
 */
data class OnboardingPage(
    val imageRes: Int,
    val titleRes: Int,
    val descriptionRes: Int
)

/**
 * Daftar halaman yang akan ditampilkan dalam proses onboarding.
 */
val onboardingPages = listOf(
    OnboardingPage(
        imageRes = R.drawable.onboarding_1,
        titleRes = R.string.onboarding_title_1,
        descriptionRes = R.string.onboarding_desc_1
    ),
    OnboardingPage(
        imageRes = R.drawable.onboarding_2,
        titleRes = R.string.onboarding_title_2,
        descriptionRes = R.string.onboarding_desc_2
    ),
    OnboardingPage(
        imageRes = R.drawable.onboarding_3,
        titleRes = R.string.onboarding_title_3,
        descriptionRes = R.string.onboarding_desc_3
    )
)

/**
 * Indikator halaman (dots) untuk pager.
 *
 * @param numberOfPages Jumlah total halaman.
 * @param selectedPage Indeks halaman yang saat ini dipilih.
 */
@Composable
fun PageIndicator(numberOfPages: Int, selectedPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(numberOfPages) { i ->
            val isSelected = i == selectedPage
            Box(
                modifier = Modifier
                    .size(if (isSelected) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
            )
        }
    }
}

/**
 * Konten untuk satu halaman onboarding.
 * Terdiri dari gambar latar belakang, overlay gelap, dan teks.
 *
 * @param page Objek OnboardingPage yang berisi data untuk ditampilkan.
 */
@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = stringResource(R.string.content_desc_onboarding_image),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = stringResource(page.titleRes),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(page.descriptionRes),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
