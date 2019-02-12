package glimpse.sample

import android.graphics.Bitmap
import android.widget.ImageView
import com.squareup.picasso.Transformation
import glimpse.core.crop

class FocusTransformationPicasso(target: ImageView, private val zoom: Float = 1f) : Transformation {
    companion object {
        private val id = "com.thesupertoys.transformations.FocusTransformation"
    }

    private val targetWidth by lazy { target.layoutParams.width }
    private val targetHeight by lazy { target.layoutParams.height }

    override fun transform(source: Bitmap): Bitmap {
        return source.crop(0.1f, 0.1f, targetWidth, targetHeight).apply {
            source.recycle()
        }
    }

    override fun key(): String {
        return "$id$targetWidth,$targetHeight,$zoom"
    }
}