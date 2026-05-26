package com.simple.launcher.retirement.utils.image

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation

fun ImageView.setImage(source: RichImage, vararg transformations: Transformation<Bitmap>) = when (source) {

    is ImageRes -> Glide.with(context)
        .load(source.data)
        .transform(*transformations, ColorFilterTransformation(source.colorFilter))
        .into(this)

    is RichImageData -> Glide.with(context)
        .load(source.data)
        .transform(*transformations)
        .into(this)
}

private val EMPTY by lazy {
    ImagePath("")
}

fun emptyImage() = EMPTY

sealed class RichImage

sealed class RichImageData(open val data: Any) : RichImage()

data class ImageRes(override val data: Int, val colorFilter: Int = Color.TRANSPARENT) : RichImageData(data)

data class ImagePath(override val data: String) : RichImageData(data)

data class ImageDrawable(override val data: Drawable) : RichImageData(data)

