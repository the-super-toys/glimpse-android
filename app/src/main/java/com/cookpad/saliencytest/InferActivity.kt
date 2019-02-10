package com.cookpad.saliencytest

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.cookpad.saliencytest.Utils.generateEmptyTensor
import com.cookpad.saliencytest.Utils.populateTensorFromPixels
import kotlinx.android.synthetic.main.activity_infer.*
import org.tensorflow.lite.Interpreter
import kotlin.math.exp
import kotlin.math.ln


class InferActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infer)

        val bitmap = (original.drawable as BitmapDrawable).bitmap
        val newWidth = (240f / bitmap.height) * bitmap.width
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth.toInt(), 240, false)
        val pixels = IntArray(scaledBitmap.width * scaledBitmap.height)
        scaledBitmap.getPixels(pixels, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

        // generate input tensor from pixeles
        val input = generateEmptyTensor(1, 3, scaledBitmap.height, scaledBitmap.width)
        populateTensorFromPixels(input, pixels)

        val output = generateEmptyTensor(1, 1, scaledBitmap.height / 8, scaledBitmap.width / 8)

        Interpreter(Utils.loadModelFile(this, "saliency.tflite"), Interpreter.Options())
            .run(input, output)

        // From this point, we will probably not use anything for the final product so I will not refactor it for now
        val flattenOutput = output.flatten().flatMap { it.flatMap { it.map { it } } }

        val temperature = 0.25f

        flattenOutput
            .map { ln(it.toDouble()).toFloat() / temperature }.toFloatArray()
            .map { exp(it.toDouble()).toFloat() }.toFloatArray()
            .let { a -> a.map { it / a.sum() } }
            .let { it.toFloatArray() }
            .let { a -> a.map { 255 * it / (a.max() ?: 1f) } }
            .let { it.toFloatArray() }
            .let { a ->
                val newBitmap = Bitmap
                    .createBitmap(scaledBitmap.width / 8, scaledBitmap.height / 8, Bitmap.Config.ARGB_8888)

                a.forEachIndexed { index, value ->
                    val color = if (value > 0) {
                        Color.rgb(value.toInt(), value.toInt(), value.toInt())
                    } else {
                        Color.rgb(0, 0, 0)
                    }
                    val (pos_x, pos_y) = index % 40 to index / 40
                    newBitmap.setPixel(pos_x, pos_y, color)
                }

                heatmap.setImageBitmap(newBitmap)
            }

        print(output)
    }
}
