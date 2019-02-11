package com.cookpad.saliencytest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.cookpad.saliencytest.Utils.generateEmptyTensor
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

object Utils {
    fun loadModelFile(activity: Context, MODEL_FILE: String): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun crop(original: Bitmap, x: Float, y: Float, targetWith: Int, targetHeight: Int): Bitmap {
        val resizedBitmap = when {
            targetWith > targetHeight -> { //target landscape
                val newWidth = (targetWith * when {
                    original.width > original.height -> 2f //source landscape
                    original.width == original.height -> 2f //source square
                    else -> 1.25f //source portrait
                }).toInt()

                val ratio = original.width.toFloat() / newWidth.toFloat()
                Bitmap.createScaledBitmap(original, newWidth, (original.height / ratio).toInt(), true)
            }
            targetWith == targetHeight -> { //target square
                val newWidth = (targetWith * when {
                    original.width > original.height -> 3f //source landscape
                    original.width == original.height -> 3f //source square
                    else -> 1.5f //source portrait
                }).toInt()

                val ratio = original.width.toFloat() / newWidth.toFloat()
                Bitmap.createScaledBitmap(original, newWidth, (original.height / ratio).toInt(), true)
            }
            else -> { // target portrait
                val newHeight = (targetHeight * when {
                    original.width > original.height -> 1f //source landscape
                    original.width == original.height -> 1.5f //source square
                    else -> 2f //source portrait
                }).toInt()

                val ratio = original.height.toFloat() / newHeight.toFloat()
                Bitmap.createScaledBitmap(original, (original.width / ratio).toInt(), newHeight, true)
            }
        }

        val xWith = max((x * resizedBitmap.width - targetWith * .5).toInt(), 0)
        val croppedWith = targetWith + min(xWith, 0)
        val xAdjusted = max(xWith, 0) - max(xWith + croppedWith - resizedBitmap.width, 0)

        val yHeight = max((y * resizedBitmap.height - targetHeight * .5).toInt(), 0)
        val croppedHeight = targetHeight + min(yHeight, 0)
        val yAdjusted = max(yHeight, 0) - max(yHeight + croppedHeight - resizedBitmap.height, 0)

        return Bitmap.createBitmap(
            resizedBitmap,
            xAdjusted,
            yAdjusted,
            croppedWith,
            croppedHeight
        )
    }

    fun crop(original: Bitmap, x: Float, y: Float, windowPct: Float = .1f) = original.run {
        val croppedWith = (width * windowPct).toInt()
        val croppedHeight = (height * windowPct).toInt()

        val xWith = (x * width - croppedWith * .5).toInt()
        val yHeight = (y * height - croppedHeight * .5).toInt()

        val xAdjusted = if (xWith + croppedWith > width) {
            width - croppedWith
        } else {
            maxOf(xWith, 0)
        }

        val yAdjusted = if (yHeight + croppedHeight > height) {
            height - croppedHeight
        } else {
            maxOf(yHeight, 0)
        }

        Bitmap.createBitmap(
            this,
            xAdjusted,
            yAdjusted,
            croppedWith,
            croppedHeight
        )
    }

    fun generateEmptyTensor(batches: Int, channels: Int, rows: Int, cols: Int, defaultValue: Float = 0f) =
        Array(batches) {
            Array(channels) {
                Array(rows) {
                    FloatArray(cols) {
                        defaultValue
                    }
                }
            }
        }

    fun populateTensorFromPixels(tensor: Array<Array<Array<FloatArray>>>, pixels: IntArray) {
        if (pixels.size != tensor[0][0].size * tensor[0][0][0].size) {
            throw ArrayIndexOutOfBoundsException("The tensor and the pixels array have incompatible shapes")
        }

        for (i in 0 until tensor[0][0].size) {
            for (j in 0 until tensor[0][0][0].size) {
                val pixel = pixels[j + i * tensor[0][0][0].size]

                tensor[0][0][i][j] = Color.red(pixel) / 255f
                tensor[0][1][i][j] = Color.green(pixel) / 255f
                tensor[0][2][i][j] = Color.blue(pixel) / 255f
            }
        }
    }

    fun getAveragedCenter(heatMap: Array<FloatArray>): Pair<Float, Float> {
        var x = 0f
        var y = 0f

        heatMap.forEachIndexed { positionY, row ->
            row.forEachIndexed { positionX, value ->
                x += value * positionX
                y += value * positionY
            }
        }

        return Pair(x / heatMap[0].size, y / heatMap.size)
    }

    fun getTopZoneCenter(heatMap: Array<FloatArray>, lowerBound: Float): Pair<Float, Float> {
        val minValue = lowerBound * (heatMap.map { it.max() ?: 0f }.max() ?: 0f)
        val cheatSheet = Array(heatMap.size) { IntArray(heatMap[0].size) }
        val blobs = mutableListOf<BinaryBlob>()

        heatMap.forEachIndexed { i, row ->
            row.forEachIndexed { j, value ->
                if (value >= minValue && cheatSheet[i][j] == 0) {
                    val blob = BinaryBlob(j, i)
                    blob.explore(j, i, heatMap, cheatSheet, minValue)
                    blobs.add(blob)
                }
            }
        }

        return blobs.maxBy { it.size }?.let {
            val centerPosition = it.getCenter()
            Pair(centerPosition.first / heatMap[0].size, centerPosition.second / heatMap.size)
        } ?: Pair(0.5f, 0.5f)
    }

    fun softmax(logits: FloatArray): FloatArray {
        val exp = logits.map { exp(it) }
        val sum = exp.sum()
        return exp.map { it / sum }.toFloatArray()
    }
}

object SmartCrop {

    enum class CenterMode {
        AVERAGE,
        LARGEST
    }

    var interpreter: Interpreter? = null

    fun init(context: Context) {
        interpreter = Interpreter(Utils.loadModelFile(context, "saliency.tflite"), Interpreter.Options().apply {
            this.setNumThreads(4)
        })
    }

    fun findBitmapCenter(
        bitmap: Bitmap,
        centerMode: CenterMode = CenterMode.LARGEST,
        temperature: Float = 0.25f,
        lowerBound: Float = 0.25f
    ): Pair<Float, Float> {
        // resize bitmap to make process faster and better
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 320, 240, false)
        val pixels = IntArray(scaledBitmap.width * scaledBitmap.height)
        scaledBitmap.getPixels(pixels, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

        // setup tensors
        val input = generateEmptyTensor(1, 3, scaledBitmap.height, scaledBitmap.width)
        Utils.populateTensorFromPixels(input, pixels)

        val output = generateEmptyTensor(1, 1, scaledBitmap.height / 8, scaledBitmap.width / 8)

        interpreter?.run(input, output)

        // calculate tempered softmax
        val flattened = output[0][0].flattened()
        val softmaxed = Utils.softmax(flattened).temper(temperature)
        val reshaped = softmaxed.reshape(output[0][0].size, output[0][0][0].size)

        // get averaged center
        return if (centerMode == CenterMode.AVERAGE) {
            Utils.getAveragedCenter(reshaped[0][0])
        } else {
            Utils.getTopZoneCenter(reshaped[0][0], lowerBound = lowerBound)
        }
    }
}

class BinaryBlob(startingX: Int, startingY: Int) {
    var size = 0
    var hBounds = Pair(startingX, startingX)
    var vBounds = Pair(startingY, startingY)

    fun addPixel(x: Int, y: Int) {
        size++
        hBounds = Pair(min(hBounds.first, x), max(hBounds.second, x))
        vBounds = Pair(min(vBounds.first, y), max(vBounds.second, y))
    }

    fun explore(j: Int, i: Int, heatMap: Array<FloatArray>, cheatSheet: Array<IntArray>, lowerBound: Float) {
        if (heatMap.getOrNull(i)?.getOrNull(j) ?: 0f > lowerBound && cheatSheet[i][j] == 0) {
            addPixel(j, i)
            cheatSheet[i][j] = 1

            explore(j + 1, i, heatMap, cheatSheet, lowerBound)
            explore(j - 1, i, heatMap, cheatSheet, lowerBound)
            explore(j, i + 1, heatMap, cheatSheet, lowerBound)
            explore(j, i - 1, heatMap, cheatSheet, lowerBound)
        }
    }

    fun getCenter() = Pair((hBounds.second + hBounds.first) / 2f, (vBounds.second + vBounds.first) / 2f)
}

fun Array<FloatArray>.flattened(): FloatArray {
    var flattened = floatArrayOf()
    this.forEach { row ->
        flattened += row
    }
    return flattened
}

fun FloatArray.reshape(rows: Int, cols: Int): Array<Array<Array<FloatArray>>> {
    val newShaped = generateEmptyTensor(1, 1, rows, cols)
    for (i in 0 until rows) {
        for (j in 0 until cols) {
            val value = this[j + i * cols]

            newShaped[0][0][i][j] = value
        }
    }
    return newShaped
}

fun FloatArray.temper(temperature: Float) = this.map {
    ln(it) / temperature
}.map {
    exp(it)
}.let { a ->
    val sum = a.sum()
    a.map { it / sum }
}.toFloatArray()
