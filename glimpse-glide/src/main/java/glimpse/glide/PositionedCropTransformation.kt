package glimpse.glide

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.PAINT_FLAGS
import glimpse.core.findCenter
import java.nio.charset.Charset
import java.security.MessageDigest


class PositionedCropTransformation : BitmapTransformation() {
    companion object {
        private val id = "glimpse.glide.transformation"
        private val idBytes = id.toByteArray(Charset.forName("UTF-8"))
    }

    override fun transform(pool: BitmapPool, toCrop: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        if (toCrop.width == outWidth && toCrop.height == outHeight) {
            return toCrop
        }

        val (center, _) = toCrop.findCenter()
        val (xPercentage, yPercentage) = center

        val recycled = pool.get(
            outWidth, outHeight,
            if (toCrop.config != null) toCrop.config
            else Bitmap.Config.ARGB_8888
        )

        val scale: Float
        var dx = 0f
        var dy = 0f

        if (toCrop.width * outHeight > outWidth * toCrop.height) {
            scale = outHeight.toFloat() / toCrop.height.toFloat()
            dx = outWidth - toCrop.width * scale
            dx *= xPercentage
        } else {
            scale = outWidth.toFloat() / toCrop.width.toFloat()
            dy = outHeight - toCrop.height * scale
            dy *= yPercentage
        }

        val matrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(dx + 0.5f, dy + 0.5f)
        }

        Canvas(recycled)
            .drawBitmap(toCrop, matrix, Paint(PAINT_FLAGS))

        return recycled
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(idBytes)
    }
}
