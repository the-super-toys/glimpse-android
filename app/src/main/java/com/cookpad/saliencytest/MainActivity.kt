package com.cookpad.saliencytest

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter
import kotlin.math.exp
import kotlin.math.ln


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bitmap = (original.drawable as BitmapDrawable).bitmap
        val newWidth = (240f / bitmap.height) * bitmap.width
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth.toInt(), 240, false)
        val pixels = IntArray(scaledBitmap.width * scaledBitmap.height)
        scaledBitmap.getPixels(pixels, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

        val red = mutableListOf<FloatArray>()
        val green = mutableListOf<FloatArray>()
        val blue = mutableListOf<FloatArray>()

        for (i in 0 until scaledBitmap.height) {
            val row_red = FloatArray(scaledBitmap.width)
            val row_green = FloatArray(scaledBitmap.width)
            val row_blue = FloatArray(scaledBitmap.width)

            for (j in 0 until scaledBitmap.width) {
                val pixel = pixels[j + i * scaledBitmap.width]
                row_red[j] = Color.red(pixel) / 255f
                row_green[j] = Color.green(pixel) / 255f
            }

            red.add(row_red)
            green.add(row_green)
            blue.add(row_blue)
        }

        val input = arrayOf(arrayOf(red.toTypedArray(), green.toTypedArray(), blue.toTypedArray()))
        val output = arrayOf(arrayOf(Array(scaledBitmap.height / 8) { FloatArray(scaledBitmap.width / 8) }))

        Interpreter(loadModelFile(this, "saliency.tflite"), Interpreter.Options())
            .run(input, output)

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
