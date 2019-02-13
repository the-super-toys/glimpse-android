package glimpse.sample

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import glimpse.core.crop
import glimpse.sample.Shape.*
import glimpse.sample.TestCropActivity.Companion.goodQuality
import glimpse.sample.TestCropActivity.Companion.sourceKey
import glimpse.sample.TestCropActivity.Companion.targetKey
import glimpse.sample.TestCropActivity.Companion.zoomOptimized
import kotlinx.android.synthetic.main.fragment_crop_landscape.*
import kotlinx.android.synthetic.main.test_activity_crop.*


class TestCropActivity : AppCompatActivity() {
    companion object {
        val sourceKey = "sourceKey"
        val targetKey = "targetKey"
        var zoomOptimized = true
        var goodQuality = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.test_activity_crop)

        setSupportActionBar(toolbar)

        setupAdapter(R.id.zoom_optimized_good_quality)
    }

    private fun setupAdapter(itemId: Int) {
        when (itemId) {
            R.id.zoom_optimized_good_quality -> {
                zoomOptimized = true
                goodQuality = true
            }
            R.id.good_quality -> {
                zoomOptimized = false
                goodQuality = true
            }
            R.id.zoom_optimized_bad_quality -> {
                zoomOptimized = true
                goodQuality = false
            }
            R.id.bad_quality -> {
                zoomOptimized = false
                goodQuality = false
            }
        }

        val position = pager.currentItem
        pager.adapter = TestCropPagerAdapter(supportFragmentManager)
        pager.currentItem = position
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_test_crop, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        setupAdapter(item.itemId)
        return super.onOptionsItemSelected(item)
    }
}

class TestCropPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    private val dataSource = listOf(
        Landscape to Landscape,
        Landscape to Portrait,
        Landscape to Square,
        Square to Landscape,
        Square to Portrait,
        Square to Square,
        Portrait to Landscape,
        Portrait to Portrait,
        Portrait to Square
    )

    override fun getItem(i: Int): Fragment {
        return TestCropFragment().apply {
            arguments = Bundle().apply {
                val (source, target) = dataSource[i]
                putSerializable(sourceKey, source)
                putSerializable(targetKey, target)
            }
        }
    }

    override fun getCount(): Int = dataSource.size

    override fun getPageTitle(position: Int): CharSequence {
        val (source, target) = dataSource[position]
        return "$source-$target"
    }
}

class TestCropFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            when (arguments!!.getSerializable(TestCropActivity.sourceKey) as Shape) {
                Landscape -> R.layout.fragment_crop_landscape
                Portrait -> R.layout.fragment_crop_portrait
                Square -> R.layout.fragment_crop_square
            }, container, false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        ivOriginal.setImageResource(
            when (arguments!!.getSerializable(TestCropActivity.sourceKey) as Shape) {
                Landscape -> if (goodQuality) R.drawable.grid_numbers_landscape else R.drawable.grid_numbers_landscape_low
                Portrait -> if (goodQuality) R.drawable.grid_numbers_portrait else R.drawable.grid_numbers_portrait_low
                Square -> if (goodQuality) R.drawable.grid_numbers_square else R.drawable.grid_numbers_square_low
            }
        )

        val original = (ivOriginal.drawable as BitmapDrawable).bitmap
        //original = Bitmap.createScaledBitmap(original, 500,1000, true)

        for (i in 0..10) {
            val x = i * .1f
            val y = i * .1f

            LayoutInflater.from(activity).inflate(
                when (arguments!!.getSerializable(TestCropActivity.targetKey) as Shape) {
                    Landscape -> R.layout.item_crop_landscape
                    Portrait -> R.layout.item_crop_portrait
                    Square -> R.layout.item_crop_square
                }, llContainer
            )

            val item = llContainer.getChildAt(llContainer.childCount - 1)
            item.findViewById<TextView>(R.id.tvCrop).text = "$i"

            val imageView = item.findViewById<ImageView>(R.id.ivCrop)
            imageView.setImageBitmap(
                original.crop(
                    Pair(x, y),
                    imageView.layoutParams.width,
                    imageView.layoutParams.height,
                    optimizeZoom = zoomOptimized
                )
            )

            /*
            Picasso.get().load(R.drawable.grid_numbers_landscape)
                .transform(GlimpseTransformation(imageView))
                .into(imageView);

            GlideApp.with(this)
                .load(R.drawable.grid_numbers_landscape_low)
                .transform(GlimpseTransformation())
                .into(imageView);*/

        }
    }
}

private enum class Shape {
    Landscape, Portrait, Square,
}