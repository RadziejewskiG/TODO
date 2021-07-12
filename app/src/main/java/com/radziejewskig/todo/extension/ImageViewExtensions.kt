package com.radziejewskig.todo.extension

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.radziejewskig.todo.R

fun ImageView.loadImage(
    context: Context,
    imageUrl: String?,
    @DrawableRes placeholderDrawableRes: Int? = R.drawable.image_placeholder,
    transformCircle: Boolean = false,
    animate: Boolean = false,
    cacheAll: Boolean = false,
    onLoadingFinished: () -> Unit = {}
) {

    val listener = object : RequestListener<Drawable> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
            onLoadingFinished()
            return false
        }

        override fun onResourceReady(resource: Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            onLoadingFinished()
            return false
        }
    }

    var glide = Glide.with(context)
        .load(imageUrl)
        .listener(listener)

    if(transformCircle) {
        glide = glide.circleCrop()
    }

    if(!animate) {
        glide = glide.dontAnimate()
    }

    if(cacheAll) {
        glide = glide.diskCacheStrategy(DiskCacheStrategy.ALL)
    }

    placeholderDrawableRes?.let {
        glide = glide.placeholder(placeholderDrawableRes)
        glide = glide.error(placeholderDrawableRes)
    }

    glide.into(this)
}

fun ImageView.setTint(@ColorRes color: Int) {
    this.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this.context, color))
}

fun ImageView.greyOut() {
    val grayscaleMatrix = ColorMatrix()
    grayscaleMatrix.setSaturation(0f)
    colorFilter = ColorMatrixColorFilter(grayscaleMatrix)
}
