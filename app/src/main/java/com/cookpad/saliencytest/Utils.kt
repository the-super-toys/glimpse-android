package com.cookpad.saliencytest

import android.app.Activity
import android.graphics.Bitmap
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min

object Utils {
    fun loadModelFile(activity: Activity, MODEL_FILE: String): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun crop(original: Bitmap, x: Float, y: Float, targetWith: Int, targetHeight: Int): Bitmap {
        val resizedBitmap = when {
            targetWith > targetHeight -> { //target landscape
                val newWidth = (targetWith * when {
                    original.width > original.height -> 2f //source landscape
                    original.width == original.height -> 2f //source square
                    else -> 1.25f //source portrait
                }).toInt()

                val ratio = original.width.toFloat() / newWidth.toFloat()
                Bitmap.createScaledBitmap(original, newWidth, (original.height / ratio).toInt(), true)
            }
            targetWith == targetHeight -> { //target square
                val newWidth = (targetWith * when {
                    original.width > original.height -> 3f //source landscape
                    original.width == original.height -> 3f //source square
                    else -> 1.5f //source portrait
                }).toInt()

                val ratio = original.width.toFloat() / newWidth.toFloat()
                Bitmap.createScaledBitmap(original, newWidth, (original.height / ratio).toInt(), true)
            }
            else -> { // target portrait
                val newHeight = (targetHeight * when {
                    original.width > original.height -> 1f //source landscape
                    original.width == original.height -> 1.5f //source square
                    else -> 2f //source portrait
                }).toInt()

                val ratio = original.height.toFloat() / newHeight.toFloat()
                Bitmap.createScaledBitmap(original, (original.width / ratio).toInt(), newHeight, true)
            }
        }

        val xWith = max((x * resizedBitmap.width - targetWith * .5).toInt(), 0)
        val croppedWith = targetWith + min(xWith, 0)
        val xAdjusted = max(xWith, 0) - max(xWith + croppedWith - resizedBitmap.width, 0)

        val yHeight = max((y * resizedBitmap.height - targetHeight * .5).toInt(), 0)
        val croppedHeight = targetHeight + min(yHeight, 0)
        val yAdjusted = max(yHeight, 0) - max(yHeight + croppedHeight - resizedBitmap.height, 0)

        return Bitmap.createBitmap(
            resizedBitmap,
            xAdjusted,
            yAdjusted,
            croppedWith,
            croppedHeight
        )
    }

    fun crop(original: Bitmap, x: Float, y: Float, windowPct: Float = .1f) = original.run {
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
}

