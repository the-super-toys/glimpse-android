package com.cookpad.saliencytest

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import kotlin.math.exp
import kotlin.math.ln


class MainActivity : AppCompatActivity() {

    private val inferenceInterface by lazy { TensorFlowInferenceInterface(assets, "file:///android_asset/saliency.pb") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val input = mutableListOf<Float>()
        for (i in 0 until 230400) {
            input.add(0f)
        }*/

        val bitmap = (original.drawable as BitmapDrawable).bitmap
        val newWidth = (240f / bitmap.height) * bitmap.width
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth.toInt(), 240, false)
        val pixels = IntArray(scaledBitmap.width * scaledBitmap.height)
        scaledBitmap.getPixels(pixels, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

        val input = mutableListOf<Float>()
        pixels.forEach { input.add(Color.red(it) / 255f) }
        pixels.forEach { input.add(Color.green(it) / 255f) }
        pixels.forEach { input.add(Color.blue(it) / 255f) }

        inferenceInterface.feed(
            "0", input.toFloatArray(), 1, 3,
            scaledBitmap.height.toLong(), scaledBitmap.width.toLong()
        )

        var output = FloatArray(scaledBitmap.height * scaledBitmap.width / 64)
        inferenceInterface.run(arrayOf("Sigmoid"))
        inferenceInterface.fetch("Sigmoid", output)

        val temperature = 0.25f
        output = output.map { ln(it.toDouble()).toFloat() / temperature }.toFloatArray()
        output = output.map { exp(it.toDouble()).toFloat() }.toFloatArray()
        val sum = output.sum()
        output = output.map { it / sum }.toFloatArray()

        val max = output.max() ?: 1f
        output = output.map { 255 * it / max }.toFloatArray()

        val newBitmap = Bitmap.createBitmap(
            scaledBitmap.width / 8, scaledBitmap.height / 8, Bitmap.Config.ARGB_8888
        )

        output.forEachIndexed { index, value ->
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
