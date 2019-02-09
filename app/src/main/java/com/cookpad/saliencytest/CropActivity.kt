package com.cookpad.saliencytest

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.cookpad.saliencytest.Utils.crop
import kotlinx.android.synthetic.main.activity_crop.*

class CropActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        var original = (ivOriginal.drawable as BitmapDrawable).bitmap
        original = Bitmap.createScaledBitmap(original, 1000,500, true)

        LayoutInflater.from(this).inflate(R.layout.item_crop, llContainer)
        val item =  llContainer.getChildAt(llContainer.childCount-1)
        item.findViewById<ImageView>(R.id.ivCrop).setImageBitmap(Utils.crop(original, 0f, 1f))


        for (i in 0..10) {
            val x = i * .1f
            val y = i * .1f

            LayoutInflater.from(this).inflate(R.layout.item_crop, llContainer)
            val item =  llContainer.getChildAt(llContainer.childCount-1)
            item.findViewById<TextView>(R.id.tvCrop).text = "x:$x,y:$y"
            item.findViewById<ImageView>(R.id.ivCrop).setImageBitmap(crop(original, x, y))
        }
    }


}