package glimpse.sample

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.DisplayMetrics
import android.view.*
import com.squareup.picasso.Picasso
import glimpse.picasso.GlimpseTransformation
import kotlinx.android.synthetic.main.activity_images.*
import kotlinx.android.synthetic.main.item_image.view.*

class ImagesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_images)

        setSupportActionBar(toolbar)

        rvImages.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
                gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            }
            adapter = ImagesAdapter(Config.CenterCrop, this@ImagesActivity)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_images, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        (rvImages?.adapter as? ImagesAdapter)?.apply {
            config = when (item.itemId) {
                R.id.glimpse_zoom_optimized_high_quality -> Config.GlimpseZoomHighQualityImage
                R.id.glimpse_zoom_optimized -> Config.GlimpseZoom
                R.id.glimpse -> Config.Glimpse
                else -> Config.CenterCrop
            }

            notifyDataSetChanged()
        }

        return super.onOptionsItemSelected(item)
    }
}

private class ImagesAdapter(var config: Config, context: Activity) :
    RecyclerView.Adapter<ImagesAdapter.ImageViewHolder>() {

    private val withScreen by lazy {
        DisplayMetrics()
            .also { context.windowManager.defaultDisplay.getMetrics(it) }
            .run { widthPixels }
    }

    private val urlsImages
        get() = if (config == Config.GlimpseZoomHighQualityImage) urlsHighQuality else urlsMediumQuality

    private val heightRatios by lazy { urlsImages.map { listOf(1f, 0.5f, 0.25f).random() } }

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImagesAdapter.ImageViewHolder {
        val root = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(root)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.itemView.ivImage.layoutParams.apply {
            width = (withScreen * 0.5).toInt()
            height = (width * heightRatios[position]).toInt()
        }

        if (config == Config.CenterCrop) {
/*            GlideApp.with(holder.itemView.ivImage.context)
                .load(urlsImages[position])
                .fitCenter()
                .into(holder.itemView.ivImage)*/
            Picasso.get()
                .load(urlsImages[position])
                .resize(holder.itemView.ivImage.layoutParams.width, holder.itemView.ivImage.layoutParams.height)
                .centerCrop()
                .into(holder.itemView.ivImage)

        } else {
/*            GlideApp.with(holder.itemView.ivImage.context)
                .load(urlsImages[position])
                .transform(GlimpseTransformation(optimizeZoom = config.zoom))
                .into(holder.itemView.ivImage)*/
            Picasso.get()
                .load(urlsImages[position])
                .transform(GlimpseTransformation(holder.itemView.ivImage, optimizeZoom = config.zoom))
                .into(holder.itemView.ivImage)
        }
    }

    override fun getItemCount() = urlsImages.size
}

private enum class Config(val zoom: Boolean) {
    GlimpseZoomHighQualityImage(true), GlimpseZoom(false), Glimpse(false), CenterCrop(false)
}