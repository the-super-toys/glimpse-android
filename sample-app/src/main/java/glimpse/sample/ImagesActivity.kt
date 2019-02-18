package glimpse.sample

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.squareup.picasso.Picasso
import com.vansuita.pickimage.bean.PickResult
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import com.vansuita.pickimage.listeners.IPickResult
import glimpse.glide.GlimpseTransformation
import glimpse.sample.ImagesActivity.Companion.configKey
import glimpse.sample.ImagesActivity.Companion.resLayoutKey
import glimpse.sample.ImagesActivity.Companion.spanCountKey
import kotlinx.android.synthetic.main.activity_images.*
import kotlinx.android.synthetic.main.fragment_images.*
import kotlinx.android.synthetic.main.item_image_landscape.view.*


class ImagesActivity : AppCompatActivity(), IPickResult {
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
        menu.colorizeItems(this, R.color.colorWhite)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.pick_image) {
            PickImageDialog.build(PickSetup()).show(this)
            return super.onOptionsItemSelected(item)
        }

        val newConfig = when (item.itemId) {
            R.id.glimpse_zoom_optimized -> Config.GlimpseZoom
            R.id.glimpse -> Config.Glimpse
            else -> Config.CenterCrop
        }

        viewPagerDataSource.forEach { fragment -> fragment.updateConfig(newConfig) }

        return super.onOptionsItemSelected(item)
    }

    override fun onPickResult(result: PickResult) {
        if (result.error != null) {
            Toast.makeText(this, result.error.message, Toast.LENGTH_LONG).show();
            return
        }

        ImageActivity.launch(this, result.uri)
    }
}


class ImagesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_images, container, false)
    }

    private val adapterImages by lazy {
        ImagesAdapter(arguments!!.getInt(resLayoutKey), arguments!!.getSerializable(configKey) as Config) { url ->
            ImageActivity.launch(activity!!, url)
        }
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


private class ImagesAdapter(private val layoutRes: Int, var config: Config, val onUrlClick: (String) -> Unit) :
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
        holder.itemView.setOnClickListener { onUrlClick(urlsSample[position]) }
        setupWithGlide(holder.itemView.ivImage, position)
        //setupWithPicasso(holder.itemView.ivImage, position)
    }

    override fun getItemCount() = urlsSample.size

    private fun setupWithGlide(imageView: ImageView, position: Int) {
        if (config == Config.CenterCrop) {
            GlideApp.with(imageView.context)
                .load(urlsSample[position])
                .dontTransform()
                .centerCrop()
                .into(imageView)
        } else {
            GlideApp.with(imageView.context)
                .load(urlsSample[position])
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .transform(GlimpseTransformation(optimizeZoom = config.zoom))
                .into(imageView)
        }
    }

    private fun setupWithPicasso(imageView: ImageView, position: Int) {
        if (config == Config.CenterCrop) {
            Picasso.get()
                .load(urlsSample[position])
                .fit()
                .centerCrop()
                .into(imageView)
        } else {
            Picasso.get()
                .load(urlsSample[position])
                //.fit()
                .transform(glimpse.picasso.GlimpseTransformation(imageView, optimizeZoom = config.zoom))
                .into(imageView)
        }
    }

}


enum class Config(val zoom: Boolean) {
    GlimpseZoom(true), Glimpse(false), CenterCrop(false)
}