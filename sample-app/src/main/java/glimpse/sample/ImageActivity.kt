package glimpse.sample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.vansuita.pickimage.bean.PickResult
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import com.vansuita.pickimage.listeners.IPickResult
import glimpse.core.debugHeatMap
import glimpse.glide.GlimpseTransformation
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity(), IPickResult {
    private var config: Config = Config.Glimpse
    private lateinit var currentBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        setSupportActionBar(toolbar)

        val uri = intent.getParcelableExtra(uriKey) as? Uri

        if (uri != null) {
            GlideApp.with(this)
                .asBitmap()
                .dontTransform()
                .load(uri)
        } else {
            GlideApp.with(this)
                .asBitmap()
                .dontTransform()
                .load(intent.getStringExtra(urlKey))
        }.into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                currentBitmap = resource
                setupImages()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_image, menu)
        menu.colorizeItems(this, R.color.colorWhite)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.pick_image) {
            PickImageDialog.build(
                PickSetup()
                    .setCameraIcon(R.mipmap.camera_colored)
                    .setGalleryIcon(R.mipmap.gallery_colored)
            ).show(this)
            return super.onOptionsItemSelected(item)
        }

        config = when (item.itemId) {
            R.id.glimpse -> Config.Glimpse
            else -> Config.CenterCrop
        }

        supportActionBar?.title = when (item.itemId) {
            R.id.glimpse -> Config.Glimpse.toString()
            else -> Config.CenterCrop.toString()
        }

        setupImages()

        return super.onOptionsItemSelected(item)
    }

    private fun setupImages() {
        original.setImageBitmap(currentBitmap)

        heatmap.setImageBitmap(
            Bitmap.createScaledBitmap(
                currentBitmap.debugHeatMap(),
                currentBitmap.width,
                currentBitmap.height,
                false
            )
        )

        fun glide(imageView: ImageView) {
            if (config == Config.CenterCrop) {
                GlideApp.with(this)
                    .load(currentBitmap)
                    .centerCrop()
                    .into(imageView)
            } else {
                GlideApp.with(this)
                    .load(currentBitmap)
                    .transform(GlimpseTransformation())
                    .into(imageView)
            }
        }

        glide(landscape)
        glide(square)
        glide(portrait)
        glide(square_s)
        glide(square_xs)
        glide(square_xxs)
    }

    override fun onPickResult(result: PickResult) {
        if (result.error != null) {
            Toast.makeText(this, result.error.message, Toast.LENGTH_LONG).show();
            return
        }

        currentBitmap = result.bitmap
        setupImages()
    }

    companion object {
        val urlKey = "urlKey"
        val uriKey = "uriKey"

        fun launch(activity: Activity, url: String) {
            val intent = Intent(activity, ImageActivity::class.java)
            intent.putExtra(urlKey, url)
            activity.startActivity(intent)
        }

        fun launch(activity: Activity, uri: Uri) {
            val intent = Intent(activity, ImageActivity::class.java)
            intent.putExtra(uriKey, uri)
            activity.startActivity(intent)
        }
    }
}