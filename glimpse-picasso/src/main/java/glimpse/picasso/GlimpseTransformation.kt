package glimpse.picasso

import android.graphics.Bitmap
import android.widget.ImageView
import com.squareup.picasso.Transformation
import glimpse.core.crop
import glimpse.core.findCenter

class GlimpseTransformation(private val target: ImageView, private val optimizeZoom: Boolean) : Transformation {
    companion object {
        private val id = "glimpse.picasso.transformation"
    }

    private val targetWidth by lazy { maxOf(target.layoutParams.width, target.width) }
    private val targetHeight by lazy { maxOf(target.layoutParams.height, target.height) }

    override fun transform(source: Bitmap): Bitmap {
        if (source.width == targetWidth || source.height == targetHeight
            || targetWidth == 0 || targetWidth == 1
            || targetHeight == 0 || targetHeight == 1
        ) {
            return source
        }

        val (center, surface) = source.findCenter()
        return source.crop(center, targetWidth, targetHeight, optimizeZoom = optimizeZoom, focusSurface = surface)
            .also {
                source.recycle()
            }
    }

    override fun key(): String {
        return "$id$targetWidth,$targetHeight,$optimizeZoom"
    }
}