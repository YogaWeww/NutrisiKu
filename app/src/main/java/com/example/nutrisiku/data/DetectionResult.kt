package com.example.nutrisiku.data

import android.graphics.RectF

// Data class untuk menampung hasil deteksi tunggal
data class DetectionResult(
    val boundingBox: RectF,
    val label: String,
    val confidence: Float
)