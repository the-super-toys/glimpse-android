package glimpse.sample

import android.content.Context
import android.graphics.PorterDuff
import android.view.Menu
import androidx.core.content.ContextCompat


fun Menu.colorizeItems(context: Context, colorRes: Int) {
    val color = ContextCompat.getColor(context, colorRes)
    for (i in 0 until size()) {
        getItem(i).icon?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
}