package com.example.nutrisiku.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.nutrisiku.R

/**
 * Mendefinisikan keluarga font Poppins yang akan digunakan di seluruh aplikasi.
 * Font ini harus ditempatkan di direktori `res/font`.
 */
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

