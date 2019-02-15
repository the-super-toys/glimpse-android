package glimpse.sample

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import glimpse.glide.GlimpseTransformation
import glimpse.sample.ImagesActivity.Companion.configKey
import glimpse.sample.ImagesActivity.Companion.resLayoutKey
import glimpse.sample.ImagesActivity.Companion.spanCountKey
import kotlinx.android.synthetic.main.activity_images.*
import kotlinx.android.synthetic.main.fragment_images.*
import kotlinx.android.synthetic.main.item_image_landscape.view.*

class ImagesActivity : AppCompatActivity() {
    companion object {
        val spanCountKey = "spanCountKey"
        val configKey = "configKey"
        val resLayoutKey = "resLayoutKey"
    }

    private val viewPagerDataSource by lazy {
        val initialConfig: Config = Config.GlimpseZoom

        listOf(ImagesFragment().apply {
            arguments = Bundle().apply {
                putSerializable(configKey, initialConfig)
                putInt(spanCountKey, 1)
                putInt(resLayoutKey, R.layout.item_image_landscape)
            }
        }, ImagesFragment().apply {
            arguments = Bundle().apply {
                putSerializable(configKey, initialConfig)
                putInt(spanCountKey, 2)
                putInt(resLayoutKey, R.layout.item_image_portrait)
            }
        }, ImagesFragment().apply {
            arguments = Bundle().apply {
                putSerializable(configKey, initialConfig)
                putInt(spanCountKey, 3)
                putInt(resLayoutKey, R.layout.item_image_square)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_images)

        setSupportActionBar(toolbar)

        vpImages.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int) = viewPagerDataSource[position]
            override fun getCount(): Int = viewPagerDataSource.size
            override fun getPageTitle(position: Int) = ""
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_landscape -> {
                    vpImages.currentItem = 0
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_portrait -> {
                    vpImages.currentItem = 1
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_square -> {
                    vpImages.currentItem = 3
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
        val newConfig = when (item.itemId) {
            R.id.glimpse_zoom_optimized -> Config.GlimpseZoom
            R.id.glimpse -> Config.Glimpse
            else -> Config.CenterCrop
        }

        viewPagerDataSource.forEach { fragment -> fragment.updateConfig(newConfig) }

        return super.onOptionsItemSelected(item)
    }
}


class ImagesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_images, container, false)
    }

    private val adapterImages by lazy {
        ImagesAdapter(arguments!!.getInt(resLayoutKey), arguments!!.getSerializable(configKey) as Config)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        rvImages.apply {
            layoutManager = GridLayoutManager(activity, arguments!!.getInt(spanCountKey))
            adapter = adapterImages
        }
    }

    fun updateConfig(config: Config) {
        adapterImages.config = config
        adapterImages.notifyDataSetChanged()
    }
}


private class ImagesAdapter(private val layoutRes: Int, var config: Config) :
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


enum class Config(val zoom: Boolean) {
    GlimpseZoom(true), Glimpse(false), CenterCrop(false)
}