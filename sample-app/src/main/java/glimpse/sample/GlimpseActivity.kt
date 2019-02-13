package glimpse.sample

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TimingLogger
import glimpse.core.cropAlt
import glimpse.core.findCenter
import kotlinx.android.synthetic.main.activity_infer.*

class GlimpseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infer)


        val timings = TimingLogger("PIPELINE", "imageCenterCropping")

        val bitmap = (original.drawable as BitmapDrawable).bitmap
        val focusArea = mutableListOf<Float>()
        val center = bitmap.findCenter(focusArea = focusArea)

        timings.addSplit("Get center from bitmap")

        landscape.post {
            val landscapeBitmap =
                bitmap.cropAlt(center.first, center.second, landscape.width, landscape.height, true, focusArea)
            landscape.setImageBitmap(landscapeBitmap)
        }

        timings.addSplit("Crop landscape")

        square.post {
            val squareBitmap =
                bitmap.cropAlt(center.first, center.second, square.width, square.height, true, focusArea)
            square.setImageBitmap(squareBitmap)
        }

        timings.addSplit("Crop square")

        portrait.post {
            val portraitBitmap =
                bitmap.cropAlt(center.first, center.second, portrait.width, portrait.height, true, focusArea)
            portrait.setImageBitmap(portraitBitmap)
        }

        timings.addSplit("Crop portrait")

        // Multi size to test zoom
        square_s.post {
            val squareBitmap =
                bitmap.cropAlt(center.first, center.second, square_s.width, square_s.height, true, focusArea)
            square_s.setImageBitmap(squareBitmap)
        }

        square_xs.post {
            val squareBitmap =
                bitmap.cropAlt(center.first, center.second, square_xs.width, square_xs.height, true, focusArea)
            square_xs.setImageBitmap(squareBitmap)
        }

        square_xxs.post {
            val squareBitmap =
                bitmap.cropAlt(center.first, center.second, square_xxs.width, square_xxs.height, true, focusArea)
            square_xxs.setImageBitmap(squareBitmap)
        }

        // From this point, we will probably not use anything for the final product so I will not refactor it for now
        /*
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
        */

        print("CENTER: $center")
        timings.dumpToLog()
    }
}
