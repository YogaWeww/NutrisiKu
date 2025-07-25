package com.example.nutrisiku.data

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class FoodDetector(
    private val context: Context,
    private val modelPath: String = "best_model.tflite", // Sesuaikan dengan nama file model Anda
    private val scoreThreshold: Float = 0.5f // Ambang batas kepercayaan
) {
    private var objectDetector: ObjectDetector? = null

    // Fungsi untuk menginisialisasi ObjectDetector
    private fun setupObjectDetector() {
        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5) // Maksimal 5 objek terdeteksi dalam satu gambar
            .setScoreThreshold(scoreThreshold)
            .build()
        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(context, modelPath, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fungsi untuk menjalankan deteksi
    fun detect(bitmap: Bitmap): List<DetectionResult> {
        if (objectDetector == null) {
            setupObjectDetector()
        }

        val image = TensorImage.fromBitmap(bitmap)
        val results = objectDetector?.detect(image)

        // Ubah hasil dari TensorFlow ke dalam data class kita
        return results?.map {
            val category = it.categories.first()
            DetectionResult(
                boundingBox = it.boundingBox,
                label = category.label,
                confidence = category.score
            )
        } ?: emptyList()
    }
}
