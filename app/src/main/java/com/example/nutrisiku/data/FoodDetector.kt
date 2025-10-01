package com.example.nutrisiku.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

class FoodDetector(
    private val context: Context,
    private val modelPath: String = "best_model.tflite",
    private val scoreThreshold: Float = 0.5f,
    private val maxResults: Int = 5
) {
    private var interpreter: Interpreter? = null
    private var modelInputWidth: Int = 0
    private var modelInputHeight: Int = 0

    init {
        setupInterpreter()
    }

    private fun setupInterpreter() {
        try {
            val options = Interpreter.Options().apply {
                numThreads = 4
            }
            val modelBuffer = FileUtil.loadMappedFile(context, modelPath)
            interpreter = Interpreter(modelBuffer, options)
            val inputTensor = interpreter?.getInputTensor(0)
            val inputShape = inputTensor?.shape()
            modelInputHeight = inputShape?.get(1) ?: 0
            modelInputWidth = inputShape?.get(2) ?: 0
            Log.d("DEBUG_MODEL", "Interpreter berhasil dibuat. Input size: $modelInputWidth x $modelInputHeight")
        } catch (e: IOException) {
            Log.e("DEBUG_MODEL", "Gagal memuat model TFLite.", e)
        }
    }

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        if (interpreter == null) {
            Log.e("DEBUG_MODEL", "Interpreter belum diinisialisasi.")
            return emptyList()
        }

        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(modelInputHeight, modelInputWidth))
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        val processedImage = imageProcessor.process(tensorImage)

        val outputTensor = interpreter?.getOutputTensor(0)
        val outputShape = outputTensor?.shape() ?: return emptyList()
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32)

        interpreter?.run(processedImage.buffer, outputBuffer.buffer.rewind())

        return postProcess(outputBuffer, originalWidth, originalHeight)
    }

    private fun postProcess(outputBuffer: TensorBuffer, originalWidth: Int, originalHeight: Int): List<DetectionResult> {
        val outputArray = outputBuffer.floatArray
        val shape = outputBuffer.shape
        val numDetections = shape[2]
        val elementsPerDetection = shape[1]

        val transposedOutput = Array(numDetections) { FloatArray(elementsPerDetection) }
        for (i in 0 until elementsPerDetection) {
            for (j in 0 until numDetections) {
                transposedOutput[j][i] = outputArray[i * numDetections + j]
            }
        }

        val detections = mutableListOf<DetectionResult>()
        val numClasses = 1

        for (i in 0 until numDetections) {
            val detection = transposedOutput[i]
            val x = detection[0] * modelInputWidth
            val y = detection[1] * modelInputHeight
            val w = detection[2] * modelInputWidth
            val h = detection[3] * modelInputHeight

            var maxScore = 0f
            var bestClassIndex = -1
            for (j in 0 until numClasses) {
                val score = detection[4 + j]
                if (score > maxScore) {
                    maxScore = score
                    bestClassIndex = j
                }
            }

            if (maxScore > scoreThreshold && bestClassIndex != -1) {
                val label = labels[bestClassIndex]

                // --- PERBAIKAN KUNCI DI SINI ---
                // Hapus semua logika penskalaan yang rumit.
                // Kembalikan koordinat relatif terhadap input model (320x320).
                // OverlayCanvas akan menangani sisanya.
                val left = x - w / 2
                val top = y - h / 2
                val right = x + w / 2
                val bottom = y + h / 2

                val boundingBox = RectF(left, top, right, bottom)
                detections.add(DetectionResult(boundingBox, label, maxScore))
            }
        }
        return nms(detections)
    }

    private fun nms(detections: List<DetectionResult>): List<DetectionResult> {
        val sortedDetections = detections.sortedByDescending { it.confidence }
        val result = mutableListOf<DetectionResult>()
        for (detection in sortedDetections) {
            var shouldAdd = true
            for (existing in result) {
                if (iou(detection.boundingBox, existing.boundingBox) > 0.5f) {
                    shouldAdd = false
                    break
                }
            }
            if (shouldAdd) {
                result.add(detection)
            }
            if (result.size >= maxResults) {
                break
            }
        }
        return result
    }

    private fun iou(box1: RectF, box2: RectF): Float {
        val xA = max(box1.left, box2.left)
        val yA = max(box1.top, box2.top)
        val xB = min(box1.right, box2.right)
        val yB = min(box1.bottom, box2.bottom)
        val intersectionArea = max(0f, xB - xA) * max(0f, yB - yA)
        val box1Area = (box1.right - box1.left) * (box1.bottom - box1.top)
        val box2Area = (box2.right - box2.left) * (box2.bottom - box2.top)
        val unionArea = box1Area + box2Area - intersectionArea
        return if (unionArea > 0) intersectionArea / unionArea else 0f
    }

    private val labels = listOf(
        "Nasi Putih"
    )
}