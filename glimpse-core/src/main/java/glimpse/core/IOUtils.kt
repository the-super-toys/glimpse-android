package glimpse.core

import android.content.Context
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object IOUtils {

    internal fun loadModel(activity: Context, MODEL_FILE: String): MappedByteBuffer =
        activity.assets.openFd(MODEL_FILE).run {
            FileInputStream(fileDescriptor).channel
                .map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
}