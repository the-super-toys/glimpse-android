package glimpse.sample

import android.app.Application
import glimpse.core.Glimpse

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Glimpse.init(this)
    }
}