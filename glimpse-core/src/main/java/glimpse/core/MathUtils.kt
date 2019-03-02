package glimpse.core

import android.graphics.Color
import java.lang.Math.pow
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

internal object MathUtils {
    fun populateTensorFromPixels(tensor: Array<Array<Array<FloatArray>>>, pixels: IntArray) {
        if (pixels.size != tensor[0].size * tensor[0][0].size) {
            throw ArrayIndexOutOfBoundsException("The tensor and the pixels array have incompatible shapes")
        }

        for (i in 0 until tensor[0].size) {
            for (j in 0 until tensor[0][0].size) {
                val pixel = pixels[j + i * tensor[0][0].size]

                tensor[0][i][j][0] = Color.red(pixel) / 255f
                tensor[0][i][j][1] = Color.green(pixel) / 255f
                tensor[0][i][j][2] = Color.blue(pixel) / 255f
            }
        }
    }

    fun softMax(logits: FloatArray, temperature: Float = 1f): FloatArray {
        val exp = logits.map { exp(it / temperature) }
        val sum = exp.sum()
        return exp.map { it / sum }.toFloatArray()
    }

    private fun distance(pointA: Pair<Float, Float>, pointB: Pair<Float, Float>): Float {
        return sqrt((pointA.x - pointB.x).pow(2) + (pointA.y - pointB.y).pow(2))
    }

    fun getLargestFocusArea(
        heatMap: Array<FloatArray>,
        lowerBound: Float
    ): Pair<Float, Float> {
        val minValue = lowerBound * (heatMap.map { it.max() ?: 0f }.max() ?: 0f)
        val cheatSheet = Array(heatMap.size) { IntArray(heatMap[0].size) }
        val blobs = mutableListOf<BinaryBlob>()

        heatMap.forEachIndexed { i, row ->
            row.forEachIndexed { j, value ->
                if (value >= minValue && cheatSheet[i][j] == 0) {
                    val blob = BinaryBlob()
                    blob.explore(j, i, heatMap, cheatSheet, minValue)
                    blobs.add(blob)
                }
            }
        }

        val largestBlob = blobs.maxBy { it.getRelevance() } ?: return Pair(heatMap[0].size / 2f, heatMap.size / 2f)

        // Group blobs if they are close enough
        if (blobs.size in 2..3) {
            for (i in 0..blobs.lastIndex) {
                val targetBlob = blobs[i]
                if (targetBlob != largestBlob) {
                    val distance = distance(
                        largestBlob.getCenter(), targetBlob.getCenter()
                    )
                    if (distance <= 3f && targetBlob.getRelevance() > largestBlob.getRelevance() * 0.75f) {
                        largestBlob.merge(targetBlob)
                    }
                }
            }
        }

        return largestBlob.let {
            val centerPosition = it.getCenter()
            Pair(centerPosition.x / heatMap[0].size, centerPosition.y / heatMap.size)
        }
    }

    private class BinaryBlob {
        private var pixelCount = 0
        private var centerX: Float = 0f
        private var centerY: Float = 0f
        private var weightSum = 0f

        fun addPixel(x: Int, y: Int, weight: Float) {
            pixelCount++
            centerX += x * weight
            centerY += y * weight
            weightSum += weight
        }

        fun explore(j: Int, i: Int, heatMap: Array<FloatArray>, cheatSheet: Array<IntArray>, lowerBound: Float) {
            if (heatMap.getOrNull(i)?.getOrNull(j) ?: 0f > lowerBound && cheatSheet[i][j] == 0) {
                addPixel(j, i, heatMap[i][j])
                cheatSheet[i][j] = 1

                explore(j + 1, i, heatMap, cheatSheet, lowerBound)
                explore(j - 1, i, heatMap, cheatSheet, lowerBound)
                explore(j, i + 1, heatMap, cheatSheet, lowerBound)
                explore(j, i - 1, heatMap, cheatSheet, lowerBound)
            }
        }

        fun merge(targetBlob: BinaryBlob) {
            centerX += targetBlob.centerX
            centerY += targetBlob.centerY
            weightSum += targetBlob.weightSum
            pixelCount += targetBlob.pixelCount
        }

        fun getCenter() = Pair(centerX / weightSum, centerY / weightSum)
        fun getRelevance() = pow(weightSum.toDouble(), 2.0).toFloat() / pixelCount
    }
}