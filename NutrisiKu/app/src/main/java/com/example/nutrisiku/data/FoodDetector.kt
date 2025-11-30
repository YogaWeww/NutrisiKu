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
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.math.max
import kotlin.math.min

/**
 * Kelas untuk mendeteksi objek makanan dalam gambar menggunakan model TensorFlow Lite.
 *
 * @param context Konteks aplikasi.
 * @param modelPath Path ke file model .tflite di folder assets.
 * @param labelPath Path ke file label .txt di folder assets.
 * @param scoreThreshold Ambang batas kepercayaan minimum untuk menampilkan hasil deteksi.
 * @param maxResults Jumlah maksimum hasil yang akan ditampilkan.
 */
class FoodDetector(
    private val context: Context,
    private val modelPath: String = "best_model.tflite",
    private val labelPath: String = "labels.txt",
    private val scoreThreshold: Float = 0.5f,
    private val maxResults: Int = 5
) {
    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private var modelInputWidth: Int = 0
    private var modelInputHeight: Int = 0

    init {
        setupInterpreter()
        loadLabels()
    }

    private fun setupInterpreter() {
        try {
            val options = Interpreter.Options().apply {
                // PERBAIKAN FINAL: Secara eksplisit mengatur jumlah thread CPU.
                // Ini memberikan stabilitas maksimum pada emulator dan berbagai perangkat.
                setNumThreads(4)
                // Ini memaksa TFLite untuk menggunakan CPU dan menghindari bug NNAPI di emulator x86.
                setUseNNAPI(false)
            }
            val modelBuffer = FileUtil.loadMappedFile(context, modelPath)
            interpreter = Interpreter(modelBuffer, options)

            val inputTensor = interpreter?.getInputTensor(0)
            val inputShape = inputTensor?.shape()
            modelInputHeight = inputShape?.get(1) ?: 0
            modelInputWidth = inputShape?.get(2) ?: 0
        } catch (e: IOException) {
            Log.e(TAG, "Error initializing TensorFlow Lite interpreter.", e)
        }
    }

    // ... Sisa file tetap sama persis seperti versi terakhir yang berfungsi ...

    private fun loadLabels() {
        try {
            context.assets.open(labelPath).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    labels = reader.readLines()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error loading labels from assets.", e)
        }
    }

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        if (interpreter == null || labels.isEmpty()) {
            return emptyList()
        }

        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(modelInputHeight, modelInputWidth, ResizeOp.ResizeMethod.BILINEAR))
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
        val numDitations = shape[2]
        val elementsPerDetection = shape[1]

        val transposedOutput = Array(numDitations) { FloatArray(elementsPerDetection) }
        for (i in 0 until elementsPerDetection) {
            for (j in 0 until numDitations) {
                transposedOutput[j][i] = outputArray[i * numDitations + j]
            }
        }

        val detections = mutableListOf<DetectionResult>()
        val numClasses = labels.size

        for (i in 0 until numDitations) {
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

                val scaleX = originalWidth.toFloat() / modelInputWidth
                val scaleY = originalHeight.toFloat() / modelInputHeight

                val left = (x - w / 2) * scaleX
                val top = (y - h / 2) * scaleY
                val right = (x + w / 2) * scaleX
                val bottom = (y + h / 2) * scaleY

                val boundingBox = RectF(
                    left.coerceIn(0f, originalWidth.toFloat()),
                    top.coerceIn(0f, originalHeight.toFloat()),
                    right.coerceIn(0f, originalWidth.toFloat()),
                    bottom.coerceIn(0f, originalHeight.toFloat())
                )
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
                if (iou(detection.boundingBox, existing.boundingBox) > IOU_THRESHOLD) {
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

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    companion object {
        private const val TAG = "FoodDetector"
        private const val IOU_THRESHOLD = 0.5f
    }
}

