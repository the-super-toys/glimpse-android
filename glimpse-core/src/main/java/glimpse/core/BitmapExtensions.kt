package glimpse.core

import android.graphics.Bitmap
import glimpse.core.ArrayUtils.generateEmptyTensor
import kotlin.math.max
import kotlin.math.min

fun Bitmap.crop(x: Float, y: Float, targetWith: Int, targetHeight: Int, optimizeZoom: Boolean = false): Bitmap {
    val resizedBitmap = if (optimizeZoom) fitOptimizingZoom(targetWith, targetHeight) else fit(targetWith, targetHeight)

    val xWith = max((x * resizedBitmap.width - targetWith * .5).toInt(), 0)
    val croppedWith = targetWith + min(xWith, 0)
    val xAdjusted = max(xWith, 0) - max(xWith + croppedWith - resizedBitmap.width, 0)

    val yHeight = max((y * resizedBitmap.height - targetHeight * .5).toInt(), 0)
    val croppedHeight = min(targetHeight + min(yHeight, 0), resizedBitmap.height)
    val yAdjusted = max(max(yHeight, 0) - max(yHeight + croppedHeight - resizedBitmap.height, 0), 0)

    return Bitmap.createBitmap(
        resizedBitmap,
        xAdjusted,
        yAdjusted,
        croppedWith,
        croppedHeight
    )
}

private fun Bitmap.fit(targetWith: Int, targetHeight: Int) =
    when {
        targetWith > targetHeight -> { //target landscape
            val ratio = width.toFloat() / targetWith.toFloat()
            Bitmap.createScaledBitmap(this, targetWith, (height / ratio).toInt(), true)
        }
        targetWith == targetHeight -> { //target  square
            if (width >= height) { // source landscape or square
                val ratio = height.toFloat() / targetHeight.toFloat()
                Bitmap.createScaledBitmap(this, (width / ratio).toInt(), targetHeight, true)
            } else { //source portrait
                val ratio = width.toFloat() / targetWith.toFloat()
                Bitmap.createScaledBitmap(this, targetWith, (height / ratio).toInt(), true)
            }
        }
        else -> { // target portrait
            val ratio = height.toFloat() / targetHeight.toFloat()
            Bitmap.createScaledBitmap(this, (width / ratio).toInt(), targetHeight, true)
        }
    }

fun Bitmap.fitOptimizingZoom(targetWith: Int, targetHeight: Int) = when {
    targetWith > targetHeight -> { //target landscape
        val newWidth = (targetWith * when {
            width > height -> 2f //source landscape
            width == height -> 2f //source square
            else -> 1.25f //source portrait
        }).toInt()

        val ratio = width.toFloat() / newWidth.toFloat()
        Bitmap.createScaledBitmap(this, newWidth, (height / ratio).toInt(), true)
    }
    targetWith == targetHeight -> { //target square
        val newWidth = (targetWith * when {
            width > height -> 3f //source landscape
            width == height -> 3f //source square
            else -> 1.5f //source portrait
        }).toInt()

        val ratio = width.toFloat() / newWidth.toFloat()
        Bitmap.createScaledBitmap(this, newWidth, (height / ratio).toInt(), true)
    }
    else -> { // target portrait
        val newHeight = (targetHeight * when {
            width > height -> 1f //source landscape
            width == height -> 1.5f //source square
            else -> 2f //source portrait
        }).toInt()

        val ratio = height.toFloat() / newHeight.toFloat()
        Bitmap.createScaledBitmap(this, (width / ratio).toInt(), newHeight, true)
    }
}

fun Bitmap.crop(x: Float, y: Float, windowPct: Float = .1f): Bitmap {
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

    return Bitmap.createBitmap(
        this,
        xAdjusted,
        yAdjusted,
        croppedWith,
        croppedHeight
    )
}

fun Bitmap.findCenter(
    centerMode: CenterMode = CenterMode.LARGEST,
    temperature: Float = 0.25f,
    lowerBound: Float = 0.25f,
    useLightModel: Boolean = true
): Pair<Float, Float> {
    // resize bitmap to make process faster and better
    val scaledBitmap = Bitmap.createScaledBitmap(this, 320, 240, false)
    val pixels = IntArray(scaledBitmap.width * scaledBitmap.height)
    scaledBitmap.getPixels(pixels, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

    // setup tensors
    val input = generateEmptyTensor(1, 3, scaledBitmap.height, scaledBitmap.width)
    MathUtils.populateTensorFromPixels(input, pixels)

    val output = generateEmptyTensor(1, 1, scaledBitmap.height / 8, scaledBitmap.width / 8)

    if (useLightModel) {
        interpreterLite.run(input, output)
    } else {
        interpreter.run(input, output)
    }

    // calculate tempered softmax
    val flattened = output[0][0].flattened()
    val softmaxed = MathUtils.softMax(flattened).temper(temperature)
    val reshaped = softmaxed.reshape(output[0][0].size, output[0][0][0].size)

    // get averaged center
    return if (centerMode == CenterMode.AVERAGE) {
        MathUtils.getAveragedCenter(reshaped[0][0])
    } else {
        MathUtils.getTopZoneCenter(reshaped[0][0], lowerBound = lowerBound)
    }
}