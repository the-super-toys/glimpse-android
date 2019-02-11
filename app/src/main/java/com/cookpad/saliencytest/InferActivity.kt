package com.cookpad.saliencytest

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TimingLogger
import com.cookpad.saliencytest.Utils.crop
import com.cookpad.saliencytest.Utils.generateEmptyTensor
import com.cookpad.saliencytest.Utils.getAveragedCenter
import com.cookpad.saliencytest.Utils.getTopZoneCenter
import com.cookpad.saliencytest.Utils.populateTensorFromPixels
import com.cookpad.saliencytest.Utils.softmax
import kotlinx.android.synthetic.main.activity_infer.*
import org.tensorflow.lite.Interpreter


class InferActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infer)

        val interpreter = Interpreter(Utils.loadModelFile(this, "saliency.tflite"), Interpreter.Options().apply {
            this.setNumThreads(4)
        })

        val timings = TimingLogger("PIPELINE", "imageCenterCropping")

        val bitmap = (original.drawable as BitmapDrawable).bitmap
        val newWidth = (240f / bitmap.height) * bitmap.width
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth.toInt(), 240, false)
        val pixels = IntArray(scaledBitmap.width * scaledBitmap.height)
        scaledBitmap.getPixels(pixels, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

        timings.addSplit("Obtain image pixels")

        // generate input tensor from pixeles
        val input = generateEmptyTensor(1, 3, scaledBitmap.height, scaledBitmap.width)
        populateTensorFromPixels(input, pixels)

        val output = generateEmptyTensor(1, 1, scaledBitmap.height / 8, scaledBitmap.width / 8)

        timings.addSplit("Setup tensors")

        interpreter.run(input, output)

        timings.addSplit("Generate heatmap from pixeles")

        val temperature = 0.25f

        // calculate tempered softmax
        val flattened = output[0][0].flattened()
        val softmaxed = softmax(flattened).temper(temperature)
        val reshaped = softmaxed.reshape(output[0][0].size, output[0][0][0].size)

        timings.addSplit("Apply softmax & temperature")

        // get averaged center
        val averageCenter = getAveragedCenter(reshaped[0][0])
        timings.addSplit("Calculate averaged center")
        val topCenter = getTopZoneCenter(reshaped[0][0], lowerBound = 0.25f)
        timings.addSplit("Calculate top center")

        landscape.post {
            val landscapeBitmap = crop(bitmap, topCenter.first, topCenter.second, landscape.width, landscape.height)
            landscape.setImageBitmap(landscapeBitmap)
        }

        timings.addSplit("Crop landscape")

        square.post {
            val squareBitmap = crop(bitmap, topCenter.first, topCenter.second, square.width, square.height)
            square.setImageBitmap(squareBitmap)
        }

        timings.addSplit("Crop square")

        portrait.post {
            val portraitBitmap = crop(bitmap, topCenter.first, topCenter.second, portrait.width, portrait.height)
            portrait.setImageBitmap(portraitBitmap)
        }

        timings.addSplit("Crop portrait")

        // From this point, we will probably not use anything for the final product so I will not refactor it for now
        val binary = false
        softmaxed
            .let { a ->
                a.map {
                    if (binary) {
                        if (it >= (a.max() ?: 0f) * 0.25f) 255f
                        else 0f
                    } else {
                        255 * it / (a.max() ?: 1f)
                    }
                }
            }
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
                    val (pos_x, pos_y) = index % output[0][0][0].size to index / output[0][0][0].size
                    newBitmap.setPixel(pos_x, pos_y, color)
                }

                heatmap.setImageBitmap(newBitmap)
            }


        print("CENTER:$averageCenter $topCenter")
        timings.dumpToLog()
    }
}
