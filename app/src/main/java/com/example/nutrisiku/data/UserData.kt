package com.example.nutrisiku.data

// Data class untuk merepresentasikan data profil pengguna
data class UserData(
    val name: String = "",
    val age: Int = 0,
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val gender: String = "Pria",
    val activityLevel: String = "Aktivitas Ringan"
)