package com.example.nutrisiku.data

import android.graphics.RectF

/**
 * Data class untuk merepresentasikan hasil deteksi objek tunggal dari model TFLite.
 *
 * Kelas ini berfungsi sebagai model data untuk setiap makanan yang terdeteksi,
 * menyimpan informasi lokasi, label, dan tingkat kepercayaan.
 *
 * @property boundingBox Kotak pembatas (bounding box) dari objek yang terdeteksi dalam bentuk RectF.
 * @property label Nama atau kelas dari objek yang terdeteksi (contoh: "Nasi Goreng").
 * @property confidence Tingkat kepercayaan (confidence score) dari deteksi, dengan rentang nilai dari 0.0 hingga 1.0.
 */
data class DetectionResult(
    val boundingBox: RectF,
    val label: String,
    val confidence: Float
) {
    // Properti tambahan (helper properties) untuk kemudahan akses ke koordinat dan dimensi.
    // Nilai-nilai ini dihitung dari `boundingBox` dan tidak disimpan secara terpisah.
    val x: Float get() = boundingBox.left
    val y: Float get() = boundingBox.top
    val width: Float get() = boundingBox.width()
    val height: Float get() = boundingBox.height()
}
