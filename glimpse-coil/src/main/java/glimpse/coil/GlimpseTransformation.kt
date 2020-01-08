package glimpse.coil

import android.graphics.Bitmap
import coil.bitmappool.BitmapPool
import coil.size.PixelSize
import coil.size.Size
import coil.transform.Transformation
import glimpse.core.crop
import glimpse.core.findCenter

class GlimpseTransformation : Transformation {
    override fun key(): String = GlimpseTransformation::class.java.name

    override suspend fun transform(pool: BitmapPool, input: Bitmap, size: Size): Bitmap {
        val pixelSize = size as PixelSize
        if (input.width == pixelSize.width && input.height == pixelSize.height) {
            pool.put(input)
            return input
        }

        val (xPercentage, yPercentage) = input.findCenter()

        val config = if (input.config != null) input.config else Bitmap.Config.ARGB_8888
        val recycled = pool.get(pixelSize.width, pixelSize.height, config)

        val outBitmap =
            input.crop(xPercentage, yPercentage, pixelSize.width, pixelSize.height, recycled)

        pool.put(input)

        return outBitmap
    }


}

