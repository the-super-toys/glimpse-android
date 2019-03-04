package glimpse.sample.core

import android.graphics.BitmapFactory.decodeResource
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import glimpse.core.findCenter
import org.junit.Test

class BitmapFindCenterTest {
    private val context by lazy { InstrumentationRegistry.getInstrumentation().targetContext }
    private val bitmap by lazy { decodeResource(context.resources, glimpse.sample.R.drawable.original) }

    @Test
    fun testFocalPoints() {
        val (x, y) = bitmap.findCenter()
        assertThat(x).isEqualTo(0.5081514f)
        assertThat(y).isEqualTo(0.34436738f)
    }
}