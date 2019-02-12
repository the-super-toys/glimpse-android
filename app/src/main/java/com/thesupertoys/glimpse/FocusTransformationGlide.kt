package com.thesupertoys.glimpse

import android.graphics.Bitmap
import android.os.Build.ID
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.util.Util
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.random.Random


class FocusTransformationGlide(private val zoom: Float = 1f) : BitmapTransformation() {

    companion object {
        private val id = "com.thesupertoys.transformations.FocusTransformation"
        private val idBytes = id.toByteArray(Charset.forName("UTF-8"))
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val x = Random.nextDouble(0.0, 1.0).toFloat()
        val y = Random.nextDouble(0.0, 1.0).toFloat()
        return Utils.crop(toTransform, x, y, outWidth, outHeight)
    }

    override fun equals(other: Any?): Boolean = if (other is FocusTransformationGlide) {
        zoom == other.zoom
    } else {
        false
    }

    override fun hashCode(): Int = Util.hashCode(ID.hashCode(), Util.hashCode((zoom * 100).toInt()))

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(idBytes)

        messageDigest.update(ByteBuffer.allocate(4).putFloat(zoom).array())
    }
}