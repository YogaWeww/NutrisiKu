package com.example.nutrisiku.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * File ini mendefinisikan palet warna dasar yang akan digunakan
 * untuk membangun skema warna terang (light) dan gelap (dark) untuk aplikasi.
 */

// Warna dasar dari palet kustom Anda
val sageGreen = Color(0xFFA3B899)      // Primer
val warmPeach = Color(0xFFE29578)     // Sekunder / Aksen
val charcoal = Color(0xFF3D405B)          // Teks Utama, Latar Belakang Gelap
val paleCream = Color(0xFFF4F3EE)      // Permukaan Terang
val offWhite = Color(0xFFFFFFFF)          // Latar Belakang Terang
val errorRed = Color(0xFFB00020)       // Warna untuk status error

// Skema Warna untuk Tema Terang (Light Theme)
val md_theme_light_primary = sageGreen
val md_theme_light_onPrimary = offWhite
val md_theme_light_secondary = warmPeach
val md_theme_light_onSecondary = offWhite
val md_theme_light_error = errorRed
val md_theme_light_onError = offWhite
val md_theme_light_background = offWhite
val md_theme_light_onBackground = charcoal
val md_theme_light_surface = paleCream
val md_theme_light_onSurface = charcoal
