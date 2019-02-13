package glimpse.picasso

import android.graphics.Bitmap
import android.widget.ImageView
import com.squareup.picasso.Transformation
import glimpse.core.crop
import glimpse.core.findCenter

class GlimpseTransformation(target: ImageView, private val optimizeZoom: Boolean) : Transformation {
    companion object {
        private val id = "glimpse.picasso.transformation"
    }

    private val targetWidth by lazy { target.layoutParams.width }
    private val targetHeight by lazy { target.layoutParams.height }

    override fun transform(source: Bitmap): Bitmap {
        if (source.width == targetWidth || source.height == targetHeight
            || targetWidth == 0 || targetWidth == 1
            || targetHeight == 0 || targetHeight == 1
        ) {
            return source
        }

        val (x, y) = source.findCenter()
        return source.crop(x, y, targetWidth, targetHeight, optimizeZoom = optimizeZoom).also {
            source.recycle()
        }
    }

    override fun key(): String {
        return "$id$targetWidth,$targetHeight,$optimizeZoom"
    }
}