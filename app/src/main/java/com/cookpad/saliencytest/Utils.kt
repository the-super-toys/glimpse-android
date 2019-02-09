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
        val windowPct = .2f

        val cropped = original.run {
            val croppedWith = (width * windowPct).toInt()
            val croppedHeight = (height * windowPct).toInt()

            val xWith = (x * width - croppedWith * .5).toInt()
            val yHeight = (y * height - croppedHeight * .5).toInt()

            val xAdjusted = if (xWith + croppedWith > width) {
                width - croppedWith
            } else {
                maxOf(xWith, 0)
            }

            val yAdjusted = if (yHeight + croppedHeight > height) {
                height - croppedHeight
            } else {
                maxOf(yHeight, 0)
            }

            Bitmap.createBitmap(
                this,
                xAdjusted,
                yAdjusted,
                croppedWith,
                croppedHeight
            )
        }


        return cropped
    }
}

