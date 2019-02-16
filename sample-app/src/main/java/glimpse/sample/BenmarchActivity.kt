package glimpse.sample

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.TimingLogger
import androidx.appcompat.app.AppCompatActivity
import glimpse.core.crop
import glimpse.core.debugHeatMap
import glimpse.core.findCenter
import kotlinx.android.synthetic.main.activity_benmarch.*

class BenmarchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_benmarch)


        val timings = TimingLogger("PIPELINE", "imageCenterCropping")

        val bitmap = (original.drawable as BitmapDrawable).bitmap
        val (center, surface) = bitmap.findCenter()

        timings.addSplit("Get center from bitmap")

        landscape.post {
            val landscapeBitmap =
                bitmap.crop(center, landscape.width, landscape.height, true, surface)
            landscape.setImageBitmap(landscapeBitmap)
        }

        timings.addSplit("Crop landscape")

        square.post {
            val squareBitmap =
                bitmap.crop(center, square.width, square.height, true, surface)
            square.setImageBitmap(squareBitmap)
        }

        timings.addSplit("Crop square")

        portrait.post {
            val portraitBitmap =
                bitmap.crop(center, portrait.width, portrait.height, true, surface)
            portrait.setImageBitmap(portraitBitmap)
        }

        timings.addSplit("Crop portrait")

        // Multi size to test zoom
        square_s.post {
            val squareBitmap =
                bitmap.crop(center, square_s.width, square_s.height, true, surface)
            square_s.setImageBitmap(squareBitmap)
        }

        square_xs.post {
            val squareBitmap =
                bitmap.crop(center, square_xs.width, square_xs.height, true, surface)
            square_xs.setImageBitmap(squareBitmap)
        }

        square_xxs.post {
            val squareBitmap =
                bitmap.crop(center, square_xxs.width, square_xxs.height, true, surface)
            square_xxs.setImageBitmap(squareBitmap)
        }

        val heatMapBitmap = bitmap.debugHeatMap()
        heatmap.setImageBitmap(heatMapBitmap)

        print("CENTER: $center")
        timings.dumpToLog()
    }
}
