package com.example.nutrisiku.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Palet warna kustom untuk tema terang (light theme)
// Dibuat menggunakan variabel yang telah didefinisikan di Color.kt
private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface
)

/**
 * Komponen tema utama untuk aplikasi NutrisiKu.
 * Menerapkan skema warna, tipografi, dan bentuk kustom.
 * Tema ini secara permanen diatur ke mode terang (light mode).
 *
 * @param content Konten Composable yang akan menerapkan tema ini.
 */
@Composable
fun NutrisiKuTheme(
    content: @Composable () -> Unit
) {
    // PERBAIKAN: Selalu gunakan LightColorScheme untuk menonaktifkan mode gelap.
    val colorScheme = LightColorScheme

    // Mengatur warna dan ikon di status bar sistem agar sesuai dengan tema terang.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            // 'true' berarti ikon status bar akan menjadi gelap, cocok untuk latar belakang terang.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

