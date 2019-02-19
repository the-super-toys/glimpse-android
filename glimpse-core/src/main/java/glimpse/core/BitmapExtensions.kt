package glimpse.core

import android.graphics.*
import glimpse.core.ArrayUtils.generateEmptyTensor
import org.tensorflow.lite.Interpreter

fun Bitmap.crop(
    xPercentage: Float,
    yPercentage: Float,
    outWidth: Int,
    outHeight: Int,
    recycled: Bitmap? = null
): Bitmap {
    val scale: Float
    var dx = 0f
    var dy = 0f

    if (width * outHeight > outWidth * height) {
        scale = outHeight.toFloat() / height.toFloat()
        dx = outWidth - width * scale
        dx *= xPercentage
    } else {
        scale = outWidth.toFloat() / width.toFloat()
        dy = outHeight - height * scale
        dy *= yPercentage
    }

    val matrix = Matrix().apply {
        setScale(scale, scale)
        postTranslate(dx + 0.5f, dy + 0.5f)
    }

    val target = recycled ?: Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)

    Canvas(target).drawBitmap(this, matrix, Paint(Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG))

    return target
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
                val (focus_x, focus_y) = focusArea.x * output[0][0][0].size to focusArea.y * output[0][0].size
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
    lowerBound: Float = 0.25f
): Pair<Float, Float> {
    // resize bitmap to make process faster and better
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
    return MathUtils.getLargestFocusArea(reshaped[0][0], lowerBound = lowerBound)
}