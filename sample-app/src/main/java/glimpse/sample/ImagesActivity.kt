package glimpse.sample

import android.app.Activity
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import glimpse.glide.GlimpseTransformation
import kotlinx.android.synthetic.main.activity_images.*
import kotlinx.android.synthetic.main.item_image_landscape.view.*

class ImagesActivity : AppCompatActivity() {
    private var layoutRes = R.layout.item_image_landscape
    private var config: Config = Config.GlimpseZoom
    private var spanCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_images)

        setSupportActionBar(toolbar)

        setupAdapter()

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_landscape -> {
                    spanCount = 1
                    layoutRes = R.layout.item_image_landscape
                    setupAdapter()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_portrait -> {
                    spanCount = 2
                    layoutRes = R.layout.item_image_portrait
                    setupAdapter()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_square -> {
                    spanCount = 3
                    layoutRes = R.layout.item_image_square
                    setupAdapter()
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_images, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        config = when (item.itemId) {
            R.id.glimpse_zoom_optimized -> Config.GlimpseZoom
            R.id.glimpse -> Config.Glimpse
            else -> Config.CenterCrop
        }

        setupAdapter()

        return super.onOptionsItemSelected(item)
    }

    private fun setupAdapter() {
        val position = (rvImages.layoutManager as? GridLayoutManager)?.findFirstVisibleItemPosition() ?: 0
        rvImages.layoutManager = GridLayoutManager(this@ImagesActivity, spanCount)
        rvImages.adapter = ImagesAdapter(config, layoutRes, this@ImagesActivity)
        if (position!= 0) rvImages.scrollToPosition(position)
    }
}

private class ImagesAdapter(private val config: Config, private val layoutRes: Int, context: Activity) :
    RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImagesAdapter.ImageViewHolder {
        val root = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ImageViewHolder(root)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        if (config == Config.CenterCrop) {
            GlideApp.with(holder.itemView.ivImage.context)
                .load(urlsSample[position])
                .centerCrop()
                .into(holder.itemView.ivImage)
/*            Picasso.get()
                .load(urlsImages[position])
                .fit()
                //.resize(holder.itemView.ivImage.layoutParams.width, holder.itemView.ivImage.layoutParams.height)
                .centerCrop()
                .into(holder.itemView.ivImage)*/

        } else {
            GlideApp.with(holder.itemView.ivImage.context)
                .load(urlsSample[position])
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .transform(GlimpseTransformation(optimizeZoom = config.zoom))
                .into(holder.itemView.ivImage)
/*            Picasso.get()
                .load(urlsImages[position])
                .transform(GlimpseTransformation(holder.itemView.ivImage, optimizeZoom = config.zoom))
                .fit()
                .into(holder.itemView.ivImage)*/
        }
    }

    override fun getItemCount() = urlsSample.size
}

private enum class Config(val zoom: Boolean) {
    GlimpseZoom(true), Glimpse(false), CenterCrop(false)
}