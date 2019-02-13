package glimpse.core

import android.graphics.Color
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

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

    fun getAveragedFocusArea(heatMap: Array<FloatArray>): FocusArea {
        var x = 0f
        var y = 0f

        heatMap.forEachIndexed { positionY, row ->
            row.forEachIndexed { positionX, value ->
                x += value * positionX
                y += value * positionY
            }
        }

        return FocusArea(Pair(x / heatMap[0].size, y / heatMap.size), Pair(0f, 0f))
    }

    fun getLargestFocusArea(
        heatMap: Array<FloatArray>,
        lowerBound: Float
    ): FocusArea {
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

        val largestBlob = blobs.maxBy { it.size }

        val surface = largestBlob?.let {
            val focusWidth = (it.hBounds.second - it.hBounds.first) / heatMap[0].size.toFloat()
            val focusHeight = (it.vBounds.second - it.vBounds.first) / heatMap.size.toFloat()
            Pair(focusWidth, focusHeight)
        } ?: Pair(0f, 0f)

        val center = largestBlob?.let {
            val centerPosition = it.getCenter()
            Pair(centerPosition.first / heatMap[0].size, centerPosition.second / heatMap.size)
        } ?: Pair(0.5f, 0.5f)

        return FocusArea(center, surface)
    }

    private class BinaryBlob(startingX: Int, startingY: Int) {
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

    data class FocusArea(val center: Pair<Float, Float>, val surface: Pair<Float, Float>)
}