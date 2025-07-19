package com.example.nutrisiku.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nutrisiku.R
import com.example.nutrisiku.ui.theme.NutrisiKuTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    // Efek ini akan berjalan sekali saat Composable pertama kali ditampilkan
    LaunchedEffect(Unit) {
        delay(2000L) // Tampilkan splash screen selama 2 detik
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface), // Krem Pucat
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_nutrisiku), // Pastikan nama drawable benar
            contentDescription = "Logo Aplikasi",
            modifier = Modifier.size(150.dp)
        )
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    NutrisiKuTheme {
        SplashScreen(onTimeout = {})
    }
}
