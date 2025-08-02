package com.example.nutrisiku.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- PERUBAHAN: Definisikan skema warna terang kustom Anda ---
private val LightColorScheme = lightColorScheme(
    primary = HijauSage,           // Warna utama untuk tombol, FAB, dll.
    secondary = PeachHangat,         // Warna aksen untuk progress bar, switch, dll.
    background = Putih,              // Warna latar belakang utama layar.
    surface = KremPucat,             // Warna untuk permukaan komponen seperti Card.
    onPrimary = Putih,               // Warna teks di atas komponen 'primary'.
    onSecondary = Putih,             // Warna teks di atas komponen 'secondary'.
    onBackground = Arang,            // Warna teks di atas latar belakang utama.
    onSurface = Arang                // Warna teks di atas permukaan seperti Card.
)

// Kita tidak akan mendefinisikan dark theme untuk saat ini,
// tapi ini adalah contoh jika diperlukan di masa depan.
private val DarkColorScheme = darkColorScheme(
    primary = HijauSage,
    secondary = PeachHangat,
    // ... definisikan warna lain untuk mode gelap
)

@Composable
fun NutrisiKuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set ke false untuk selalu menggunakan tema kustom kita
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //     val context = LocalContext.current
        //     if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        // }
        // darkTheme -> DarkColorScheme // Nonaktifkan dark theme untuk saat ini
        else -> LightColorScheme
    }

    // Mengatur warna status bar agar sesuai dengan tema
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}