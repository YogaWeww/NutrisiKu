package com.example.nutrisiku.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class FoodDetector(
    private val context: Context,
    private val modelPath: String = "best_model.tflite", // Pastikan nama ini sesuai
    private val scoreThreshold: Float = 0.5f,
    private val maxResults: Int = 5,
    private val numThreads: Int = 4
) {
    private var objectDetector: ObjectDetector? = null

    init {
        setupObjectDetector()
    }

    private fun setupObjectDetector() {
        try {
            val optionsBuilder =
                ObjectDetector.ObjectDetectorOptions.builder()
                    .setScoreThreshold(scoreThreshold)
                    .setMaxResults(maxResults)

            val baseOptionsBuilder =
                BaseOptions.builder().setNumThreads(numThreads)
            optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

            Log.d("DEBUG_MODEL", "FoodDetector: Mencoba memuat model dari path: $modelPath")
            objectDetector = ObjectDetector.createFromFileAndOptions(context, modelPath, optionsBuilder.build())
            Log.d("DEBUG_MODEL", "FoodDetector: Model berhasil dimuat.")

        } catch (e: IllegalStateException) {
            Log.e("DEBUG_MODEL", "FoodDetector: Gagal memuat model! Error: ${e.message}")
        }
    }

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        if (objectDetector == null) {
            Log.e("DEBUG_MODEL", "FoodDetector: ObjectDetector null, proses deteksi dibatalkan.")
            return emptyList()
        }

        val imageProcessor = ImageProcessor.Builder().build()
        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

        Log.d("DEBUG_MODEL", "FoodDetector: Memulai proses deteksi...")
        val results = objectDetector?.detect(tensorImage)
        Log.d("DEBUG_MODEL", "FoodDetector: Proses deteksi selesai. Ditemukan ${results?.size ?: 0} objek.")

        return results?.mapNotNull { detection ->
            // Ambil kategori dengan skor tertinggi
            detection.categories.maxByOrNull { it.score }?.let { category ->
                Log.d("DEBUG_MODEL", "  -> Ditemukan: ${category.label} (Confidence: ${category.score})")
                DetectionResult(
                    boundingBox = detection.boundingBox,
                    label = category.label,
                    confidence = category.score
                )
            }
        } ?: emptyList()
    }
}