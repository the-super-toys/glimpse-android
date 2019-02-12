package glimpse.sample

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import glimpse.core.crop
import kotlinx.android.synthetic.main.activity_crop.*

class TestCropActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)


        inflateViews()
    }

    private fun inflateViews() {
        val original = (ivOriginal.drawable as BitmapDrawable).bitmap
        //original = Bitmap.createScaledBitmap(original, 500,1000, true)

        for (i in 0..9) {
            val x = i * .1f
            val y = i * .1f

            LayoutInflater.from(this).inflate(R.layout.item_crop, llContainer)

            val item = llContainer.getChildAt(llContainer.childCount - 1)
            item.findViewById<TextView>(R.id.tvCrop).text = "x:$x,y:$y"

            val imageView = item.findViewById<ImageView>(R.id.ivCrop)
            imageView.setImageBitmap(original.crop(x, y, imageView.layoutParams.width, imageView.layoutParams.height))

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