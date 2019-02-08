package com.cookpad.saliencytest

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_crop.*

class CropActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        val original = (ivOriginal.drawable as BitmapDrawable).bitmap

        for (i in 0..10) {
            val x = i * .1f
            val y = i * .1f

            LayoutInflater.from(this).inflate(R.layout.item_crop, llContainer)
            val item =  llContainer.getChildAt(llContainer.childCount-1)
            item.findViewById<TextView>(R.id.tvCrop).text = "x:$x,y:$y"
            item.findViewById<ImageView>(R.id.ivCrop).setImageBitmap(Utils.crop(original, x, y))
        }
    }
}