package glimpse.sample

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import glimpse.picasso.GlimpseTransformation
import kotlinx.android.synthetic.main.activity_crop.*

class CropActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        val original = (ivOriginal.drawable as BitmapDrawable).bitmap
        //original = Bitmap.createScaledBitmap(original, 500,1000, true)

        inflateViews()
        inflateViews()
    }

    private fun inflateViews() {
        for (i in 0..9) {
            val x = i * .1f
            val y = i * .1f

            LayoutInflater.from(this).inflate(R.layout.item_crop, llContainer)
            val item = llContainer.getChildAt(llContainer.childCount - 1)
            item.findViewById<TextView>(R.id.tvCrop).text = "x:$x,y:$y"

            val imageView = item.findViewById<ImageView>(R.id.ivCrop)

            Picasso.get().load(R.drawable.grid_numbers_landscape)
                .transform(GlimpseTransformation(imageView))
                .into(imageView);

/*            GlideApp.with(this)
                .load(R.drawable.grid_numbers_landscape_low)
                .transform(GlimpseTransformation())
                .into(imageView);*/


            //item.findViewById<ImageView>(R.id.ivCrop).setImageBitmap(crop2(original, x, y, width, height))
        }
    }
}