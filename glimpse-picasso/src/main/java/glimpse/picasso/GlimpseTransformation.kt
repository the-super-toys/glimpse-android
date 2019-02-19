package glimpse.picasso

import android.graphics.Bitmap
import android.widget.ImageView
import com.squareup.picasso.Transformation
import glimpse.core.crop
import glimpse.core.findCenter

class GlimpseTransformation(target: ImageView) : Transformation {
    companion object {
        private val id = "glimpse.picasso.transformation"
    }

    private val targetWidth by lazy { maxOf(target.layoutParams.width, target.width) }
    private val targetHeight by lazy { maxOf(target.layoutParams.height, target.height) }

    override fun transform(source: Bitmap): Bitmap {
        val (centerX, centerY) = source.findCenter()
        return source.crop(centerX, centerY, targetWidth, targetHeight)
            .also { source.recycle() }
    }

    override fun key(): String {
        return id
    }
}