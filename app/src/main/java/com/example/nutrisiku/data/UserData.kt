package com.example.nutrisiku.data

// Data class untuk merepresentasikan data profil pengguna
data class UserData(
    val name: String,
    val age: Int,
    val weight: Double,
    val height: Double,
    val gender: String,
    val activityLevel: String,
    val imagePath: String,
    val tdee: Float = 0f // PERUBAHAN: Tambahkan properti TDEE
)
