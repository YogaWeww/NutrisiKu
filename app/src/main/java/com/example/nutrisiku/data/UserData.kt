package com.example.nutrisiku.data

/**
 * Data class yang merepresentasikan dan menampung semua informasi
 * terkait profil pengguna.
 *
 * @property name Nama lengkap pengguna.
 * @property age Usia pengguna dalam tahun.
 * @property weight Berat badan pengguna dalam kilogram.
 * @property height Tinggi badan pengguna dalam sentimeter.
 * @property gender Jenis kelamin pengguna ("Pria" atau "Wanita").
 * @property activityLevel Tingkat aktivitas harian pengguna.
 * @property imagePath Path lokal ke gambar profil pengguna.
 * @property tdee Total Daily Energy Expenditure (TDEE) atau total kebutuhan kalori harian
 * yang dihitung berdasarkan data pengguna.
 */
data class UserData(
    val name: String,
    val age: Int,
    val weight: Double,
    val height: Double,
    val gender: String,
    val activityLevel: String,
    val imagePath: String,
    val tdee: Float = 0f
)
