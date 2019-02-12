package glimpse.picasso

import android.graphics.Bitmap
import android.widget.ImageView
import com.squareup.picasso.Transformation
import glimpse.core.crop

class GlimpseTransformation(target: ImageView, private val zoom: Float = 1f) : Transformation {
    companion object {
        private val id = "glimpse.picasso.transformation"
    }

    private val targetWidth by lazy { target.layoutParams.width }
    private val targetHeight by lazy { target.layoutParams.height }

    override fun transform(source: Bitmap): Bitmap {
        return source.crop(0.1f, 0.1f, targetWidth, targetHeight).also {
            source.recycle()
        }
    }

    override fun key(): String {
        return "$id$targetWidth,$targetHeight,$zoom"
    }
}