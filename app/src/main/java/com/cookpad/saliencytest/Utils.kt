package com.cookpad.saliencytest

import android.app.Activity
import android.graphics.Bitmap
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel



object Utils {
    fun loadModelFile(activity: Activity, MODEL_FILE: String): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun crop(original: Bitmap, x: Float, y: Float): Bitmap {
        val windowPct = .5f

        val cropped = original.run {
            val xWith = (x * width).toInt()
            val yHeight = (y * height).toInt()

            val croppedWith = (width * windowPct / 2).toInt()
            val croppedHeight = (height * windowPct / 2).toInt()

            val xAdjusted = if (xWith > width * windowPct) {
                minOf(xWith - croppedWith, (width * windowPct).toInt())
            } else {
                maxOf(xWith - croppedWith, 0)
            }

            val yAdjusted = if (yHeight > height * windowPct) {
                minOf(yHeight - croppedHeight, (height * windowPct).toInt())
            } else {
                maxOf(yHeight - croppedHeight, 0)
            }

/*            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true*/

            Bitmap.createBitmap(
                this,
                xAdjusted,
                yAdjusted,
                (width * windowPct).toInt(),
                (height * windowPct).toInt()
            )
        }

        return Bitmap.createScaledBitmap(
            cropped, original.width, original.height,
            true
        )
    }
}

