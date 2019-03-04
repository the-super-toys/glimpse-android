package glimpse.core

import android.app.Application

object Glimpse {
    internal lateinit var client: Application

    @JvmStatic
    fun init(app: Application) {
        client = app
    }
}