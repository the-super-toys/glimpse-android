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

    private val tflite by lazy { Interpreter(loadModelFile(this, "saliency_v2.tflite"), Interpreter.Options()) }

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
            val row = FloatArray(scaledBitmap.width)
            for (j in 0 until scaledBitmap.width) {
                val pixel = pixels[j + i * scaledBitmap.width]
                row[i] = Color.red(pixel) / 255f
            }
            red.add(row)
        }
        for (i in 0 until scaledBitmap.height) {
            val row = FloatArray(scaledBitmap.width)
            for (j in 0 until scaledBitmap.width) {
                val pixel = pixels[j + i * scaledBitmap.width]
                row.set(
                    i,
                    Color.green(pixel) / 255f
                )
            }

            green.add(row)
        }
        for (i in 0 until scaledBitmap.height) {
            val row = FloatArray(scaledBitmap.width)
            for (j in 0 until scaledBitmap.width) {
                val pixel = pixels[j + i * scaledBitmap.width]
                row.set(
                    i,
                    Color.blue(pixel) / 255f
                )
            }
            blue.add(row)
        }


        val input_array = arrayOf(
            arrayOf(red.toTypedArray(), green.toTypedArray(), blue.toTypedArray())
        )
        //pixels.forEach { input.add(Color.red(it) / 255f) }
        //pixels.forEach { input.add(Color.green(it) / 255f) }
        //pixels.forEach { input.add(Color.blue(it) / 255f) }

        var output =
            arrayOf(arrayOf(Array(scaledBitmap.height / 8) { FloatArray(scaledBitmap.width / 8) })) //FloatArray(1, scaledBitmap.height / 8, scaledBitmap.width / 8)


        tflite.run(input_array, output)


        /*inferenceInterface.feed(
            "0", input.toFloatArray(), 1, 3,
            scaledBitmap.height.toLong(), scaledBitmap.width.toLong()
        )

        inferenceInterface.run(arrayOf("Sigmoid"))
        inferenceInterface.fetch("Sigmoid", output)*/

        val flattenOutput = mutableListOf<Float>()
        output.flatten().forEachIndexed { i, arrayOfArrays ->
            arrayOfArrays.forEachIndexed { j, floats ->
                floats.forEachIndexed { k, value ->
                    flattenOutput.add(value)
                }
            }

        }

        val temperature = 0.25f


        var a = flattenOutput.map { ln(it.toDouble()).toFloat() / temperature }.toFloatArray()
        a = a.map { exp(it.toDouble()).toFloat() }.toFloatArray()
        val sum = a.sum()
        a = a.map { it / sum }.toFloatArray()

        val max = a.max() ?: 1f
        a = a.map { 255 * it / max }.toFloatArray()

        val newBitmap = Bitmap.createBitmap(
            scaledBitmap.width / 8, scaledBitmap.height / 8, Bitmap.Config.ARGB_8888
        )

        a.forEachIndexed { index, value ->
            val color = if (value > 0) {
                Color.rgb(value.toInt(), value.toInt(), value.toInt())
            } else Color.rgb(0, 0, 0)
            val pos_x = index % 40
            val pos_y = index / 40
            newBitmap.setPixel(pos_x, pos_y, color)
        }

        heatmap.setImageBitmap(newBitmap)

        print(output)
    }
}
