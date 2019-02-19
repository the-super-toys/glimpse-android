package glimpse.glide

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import glimpse.core.crop
import glimpse.core.findCenter
import java.nio.charset.Charset
import java.security.MessageDigest


class GlimpseTransformation : BitmapTransformation() {
    companion object {
        private val id = "glimpse.glide.transformation"
        private val idBytes = id.toByteArray(Charset.forName("UTF-8"))
    }

    override fun transform(pool: BitmapPool, toCrop: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        if (toCrop.width == outWidth && toCrop.height == outHeight) {
            return toCrop
        }

        val (xPercentage, yPercentage) = toCrop.findCenter()

        val config = if (toCrop.config != null) toCrop.config else Bitmap.Config.ARGB_8888
        val recycled = pool.get(outWidth, outHeight, config)

        return toCrop.crop(xPercentage, yPercentage, outWidth, outHeight, recycled)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(idBytes)
    }
}
