package glimpse.core

import android.graphics.Color
import kotlin.math.*

object MathUtils {
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

    fun softMax(logits: FloatArray): FloatArray {
        val exp = logits.map { exp(it) }
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
                    val blob = BinaryBlob(j, i)
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
                        Pair(largestBlob.centerX, largestBlob.centerY), Pair(targetBlob.centerX, targetBlob.centerY)
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

    private class BinaryBlob(startingX: Int, startingY: Int) {
        var pixelCount = 0
        var hBounds = Pair(startingX, startingX)
        var vBounds = Pair(startingY, startingY)
        var centerX: Float = 0f
        var centerY: Float = 0f
        var weightSum = 0f

        fun addPixel(x: Int, y: Int, weight: Float) {
            pixelCount++
            hBounds = Pair(min(hBounds.first, x), max(hBounds.second, x))
            vBounds = Pair(min(vBounds.first, y), max(vBounds.second, y))
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
            centerX = (centerX + targetBlob.centerX) / 2f
            centerY = (centerY + targetBlob.centerY) / 2f
            weightSum += targetBlob.weightSum
            pixelCount += targetBlob.pixelCount

            hBounds = Pair(min(hBounds.first, targetBlob.hBounds.first), max(hBounds.second, targetBlob.hBounds.second))
            vBounds = Pair(min(vBounds.first, targetBlob.vBounds.first), max(vBounds.second, targetBlob.vBounds.second))
        }

        fun getCenter() = Pair(centerX / weightSum, centerY / weightSum)
        fun getBoxDims() = Pair(1f * hBounds.second - hBounds.first, 1f * vBounds.second - vBounds.first)
        fun getRelevance() = weightSum / pixelCount
    }

    data class FocusArea(val center: Pair<Float, Float>, val surface: Pair<Float, Float>)
}