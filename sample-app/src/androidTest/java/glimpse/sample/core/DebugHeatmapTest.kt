package glimpse.sample.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory.decodeResource
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import glimpse.core.debugHeatMap
import org.junit.Test

class DebugHeatmapTest {
    private val context by lazy { InstrumentationRegistry.getInstrumentation().targetContext }
    private val bitmap by lazy {
        decodeResource(context.resources, glimpse.sample.R.drawable.original).let {
            Bitmap.createScaledBitmap(it, 30, 20, false)
        }
    }

    @Test
    fun testFocalPoints() {
        val heatMap = bitmap.debugHeatMap()
        assertThat(heatMap.width).isEqualTo(320)
        assertThat(heatMap.height).isEqualTo(240)
    }
}