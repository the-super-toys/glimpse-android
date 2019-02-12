package glimpse.sample

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import glimpse.core.crop
import glimpse.sample.Shape.*
import glimpse.sample.TestCropActivity.Companion.sourceKey
import glimpse.sample.TestCropActivity.Companion.targetKey
import kotlinx.android.synthetic.main.fragment_crop_landscape.*
import kotlinx.android.synthetic.main.test_activity_crop.*

class TestCropActivity : AppCompatActivity() {
    companion object {
        val sourceKey = "sourceKey"
        val targetKey = "targetKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity_crop)
        pager.adapter = TestCropPagerAdapter(supportFragmentManager)
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
            when (arguments!!.getSerializable(TestCropActivity.targetKey) as Shape) {
                Landscape -> R.layout.fragment_crop_landscape
                Portrait -> R.layout.fragment_crop_portrait
                Square -> R.layout.fragment_crop_square
            }, container, false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        when (arguments?.getSerializable(TestCropActivity.sourceKey) as? Shape) {
            Landscape -> ivOriginal.setImageResource(R.drawable.grid_numbers_landscape)
            Portrait -> ivOriginal.setImageResource(R.drawable.grid_numbers_portrait)
            Square -> ivOriginal.setImageResource(R.drawable.grid_numbers_square)
        }

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
                    x,
                    y,
                    imageView.layoutParams.width,
                    imageView.layoutParams.height
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