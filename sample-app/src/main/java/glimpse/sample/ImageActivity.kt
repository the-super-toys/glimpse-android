package glimpse.sample

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.load.engine.DiskCacheStrategy
import glimpse.glide.GlimpseTransformation
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity() {
    companion object {
        val imageUrlKey = "imageUrlKey"
    }

    private val url by lazy { intent.getStringExtra(imageUrlKey) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        setSupportActionBar(toolbar)

        GlideApp.with(this)
            .load(url)
            .into(original)

        updateImages(optimizeZoom = true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_image, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        updateImages(optimizeZoom = R.id.glimpse_zoom_optimized == item.itemId)
        return super.onOptionsItemSelected(item)
    }

    private fun updateImages(optimizeZoom: Boolean = true) {
        glide(landscape, optimizeZoom)
        glide(square, optimizeZoom)
        glide(portrait, optimizeZoom)
        glide(square_s, optimizeZoom)
        glide(square_xs, optimizeZoom)
        glide(square_xxs, optimizeZoom)
    }


    private fun glide(imageView: ImageView, optimizeZoom: Boolean = true) {
        GlideApp.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .transform(GlimpseTransformation(optimizeZoom))
            .into(imageView)
    }
}