package glimpse.core

import android.graphics.Bitmap
import android.graphics.Color
import android.util.TimingLogger
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

fun Bitmap.debugHeatMap(
    temperature: Float = 0.15f,
    lowerBound: Float = 0.25f
): Bitmap {
    val scaledBitmap = Bitmap.createScaledBitmap(this, 320, 240, false)
    val pixels = IntArray(scaledBitmap.width * scaledBitmap.height)
    scaledBitmap.getPixels(pixels, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

    // setup tensors
    val input = generateEmptyTensor(1, 3, scaledBitmap.height, scaledBitmap.width)
    MathUtils.populateTensorFromPixels(input, pixels)

    val output = generateEmptyTensor(1, 1, scaledBitmap.height / 8, scaledBitmap.width / 8)

    val intpr = Interpreter(rawModel, Interpreter.Options().apply {
        setNumThreads(1)
    })

    intpr.run(input, output)

    intpr.close()

    // calculate tempered softmax
    val flattened = output[0][0].flattened()
    val softmaxed = MathUtils.softMax(flattened).temper(temperature)
    val reshaped = softmaxed.reshape(output[0][0].size, output[0][0][0].size)

    // get averaged center
    val focusArea = MathUtils.getLargestFocusArea(reshaped[0][0], lowerBound = lowerBound)

    return softmaxed
        .let { a ->
            a.map {
                val intensity = 255 * it / (a.max() ?: 1f)
                if (it >= (a.max() ?: 0f) * lowerBound) Pair(intensity, 1)
                else Pair(intensity, 0)
            }
        }
        .let { a ->
            val newBitmap = Bitmap
                .createBitmap(scaledBitmap.width / 8, scaledBitmap.height / 8, Bitmap.Config.ARGB_8888)

            a.forEachIndexed { index, (value, focused) ->
                val (pos_x, pos_y) = index % output[0][0][0].size to index / output[0][0][0].size
                val (focus_x, focus_y) = focusArea.center.x * output[0][0][0].size to focusArea.center.y * output[0][0].size
                val color = if (focus_x.toInt() == pos_x && focus_y.toInt() == pos_y) {
                    Color.rgb(value.toInt(), value.toInt() / 2, value.toInt() / 2)
                } else if (focused == 1) {
                    Color.rgb(value.toInt() / 2, value.toInt(), value.toInt() / 2)
                } else {
                    Color.rgb(value.toInt(), value.toInt(), value.toInt())
                }

                newBitmap.setPixel(pos_x, pos_y, color)
            }

            newBitmap
        }.let {
            Bitmap.createScaledBitmap(it, 320, 240, false)
        }
}

fun Bitmap.findCenter(
    temperature: Float = 0.15f,
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

    val timings = TimingLogger("TimingLogger", "TF Pipeline")

    val intpr = Interpreter(rawModel, Interpreter.Options().apply {
        setNumThreads(1)
    })

    timings.addSplit("Setup Interpreter")

    if (useLightModel) {
        intpr.run(input, output)
    } else {
        intpr.run(input, output)
    }

    timings.addSplit("Inference")

    intpr.close()

    timings.addSplit("Close Interpreter")
    timings.dumpToLog()

    // calculate tempered softmax
    val flattened = output[0][0].flattened()
    val softmaxed = MathUtils.softMax(flattened).temper(temperature)
    val reshaped = softmaxed.reshape(output[0][0].size, output[0][0][0].size)

    // get averaged center
    return MathUtils.getLargestFocusArea(reshaped[0][0], lowerBound = lowerBound)
}