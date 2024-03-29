package com.htchan.callreceiver.helper

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

// Class to download Images which extends [Html.ImageGetter]
class ImageGetter(
    private val res: Resources,
    private val htmlTextView: TextView
) : Html.ImageGetter {

    // Function needs to overridden when extending [Html.ImageGetter] ,
    // which will download the image
    override fun getDrawable(url: String): Drawable {
        val holder = BitmapDrawablePlaceHolder(res, null)

        // Coroutine Scope to download image in Background
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {

                Log.d("checking url", url)
                // downloading image in bitmap format using [Picasso] Library
                val bitmap = Picasso.get().load(url).get()
                val drawable = BitmapDrawable(res, bitmap)

                // To make sure Images don't go out of screen , Setting width less
                // than screen width, You can change image size if you want

                // Images may stretch out if you will only resize width,
                // hence resize height to according to aspect ratio
                val aspectRatio: Float =
                    (drawable.intrinsicWidth.toFloat()) / (drawable.intrinsicHeight.toFloat())
                val height = getScreenHeight() / 20
                val width = height * aspectRatio
                drawable.setBounds(10, 20, width.toInt(), height.toInt())
                holder.setDrawable(drawable)
                holder.setBounds(10, 20, width.toInt(), height.toInt())
                withContext(Dispatchers.Main) {
                    htmlTextView.text = htmlTextView.text
                }
            }
        }
        return holder
    }

    // Actually Putting images
    internal class BitmapDrawablePlaceHolder(res: Resources, bitmap: Bitmap?) :
        BitmapDrawable(res, bitmap) {
        private var drawable: Drawable? = null

        override fun draw(canvas: Canvas) {
            drawable?.run { draw(canvas) }
        }

        fun setDrawable(drawable: Drawable) {
            this.drawable = drawable
        }
    }

    // Function to get screenWidth used above
    fun getScreenHeight() =
        Resources.getSystem().displayMetrics.heightPixels
}