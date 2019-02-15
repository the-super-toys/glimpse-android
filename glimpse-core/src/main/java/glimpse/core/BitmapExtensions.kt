package glimpse.core

import android.graphics.Bitmap
import glimpse.core.ArrayUtils.generateEmptyTensor
import org.tensorflow.lite.Interpreter
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

fun Bitmap.crop(
    center: Pair<Float, Float>,
    targetWith: Int,
    targetHeight: Int,
    optimizeZoom: Boolean = true,
    focusSurface: Pair<Float, Float>? = null
): Bitmap {
    val ratioTarget = targetWith / targetHeight.toFloat()
    val ratioSource = width / height.toFloat()

    val (newWidth, newHeight) = if (ratioTarget < ratioSource) {
        Pair(height * ratioTarget, height.toFloat())
    } else {
        Pair(width.toFloat(), width / ratioTarget)
    }.let { dims ->
        // apply zoom
        val zoom = if (optimizeZoom) {
            // Max zoom until we start getting a blurry image
            val maxResolutionZoom = max(1.5f * min(dims.x / targetWith, dims.y / targetHeight), 1f)

            // Max zoom so the final image fits the focus area
            val maxFocusZoom = if (focusSurface != null && focusSurface.x > 0f && focusSurface.y > 0f) {
                // The padding amount is value above 1, so 0.2
                // If you put a number < 1 it will have negative padding, maybe this can be set by the user?
                val padding = 1.2f
                val focusWidth = focusSurface.x * width * padding
                val focusHeight = focusSurface.y * height * padding

                max(min(dims.x / focusWidth, dims.y / focusHeight), 1f)
            } else Float.POSITIVE_INFINITY

            min(maxResolutionZoom, maxFocusZoom)
        } else 1f

        Pair(floor(dims.x / zoom).toInt(), floor(dims.y / zoom).toInt())
    }

    val centerX = min(max(center.x * width, newWidth / 2f), width - newWidth / 2f)
    val centerY = min(max(center.y * height, newHeight / 2f), height - newHeight / 2f)

    val croppedBitmap = Bitmap.createBitmap(
        this,
        (centerX - newWidth / 2).toInt(),
        (centerY - newHeight / 2).toInt(),
        newWidth,
        newHeight
    )

    return Bitmap.createScaledBitmap(croppedBitmap, targetWith, targetHeight, true)
}

fun Bitmap.findCenter(
    temperature: Float = 0.35f,
    lowerBound: Float = 0.25f,
    useLightModel: Boolean = true
): MathUtils.FocusArea {
    // resize bitmap to make process faster and better
    val scaledBitmap = Bitmap.createScaledBitmap(this, 320, 240, false)
    val pixels = IntArray(scaledBitmap.width * scaledBitmap.height)
    scaledBitmap.getPixels(pixels, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

    // setup tensors
    val input = generateEmptyTensor(1, 3, scaledBitmap.height, scaledBitmap.width)
    MathUtils.populateTensorFromPixels(input, pixels)

    val output = generateEmptyTensor(1, 1, scaledBitmap.height / 8, scaledBitmap.width / 8)

    val intpr = Interpreter(rawModel, Interpreter.Options().apply {
        setNumThreads(4)
    })

    if (useLightModel) {
        intpr.run(input, output)
    } else {
        intpr.run(input, output)
    }

    intpr.close()

    // calculate tempered softmax
    val flattened = output[0][0].flattened()
    val softmaxed = MathUtils.softMax(flattened).temper(temperature)
    val reshaped = softmaxed.reshape(output[0][0].size, output[0][0][0].size)

    // get averaged center
    return MathUtils.getLargestFocusArea(reshaped[0][0], lowerBound = lowerBound)
}