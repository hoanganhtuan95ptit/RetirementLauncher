package com.simple.launcher.retirement.utils.background

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View

fun View.setBackground(background: Background?) {
    this.background = background?.drawable
}

private val EMPTY by lazy {
    Background()
}

fun emptyBackground() = EMPTY

data class Background(
    var backgroundColor: Int = Color.TRANSPARENT,

    val cornerRadius_TL: Int = 0,
    val cornerRadius_TR: Int = 0,
    val cornerRadius_BL: Int = 0,
    val cornerRadius_BR: Int = 0,

    val strokeWidth: Int = 0,
    val strokeColor: Int = Color.TRANSPARENT,
    val strokeDashGap: Int = 0,
    val strokeDashWidth: Int = 0
) {

    var drawable: Drawable? = null

    init {
        refresh()
    }

    fun refresh(): Background {

        drawable = GradientDrawable().apply {

            setColor(backgroundColor)

            this.shape = GradientDrawable.RECTANGLE

            // Gán corner
            this.cornerRadii = floatArrayOf(
                cornerRadius_TL.toFloat(), cornerRadius_TL.toFloat(),
                cornerRadius_TR.toFloat(), cornerRadius_TR.toFloat(),
                cornerRadius_BR.toFloat(), cornerRadius_BR.toFloat(),
                cornerRadius_BL.toFloat(), cornerRadius_BL.toFloat()
            )

            // Gán stroke nếu có
            if (strokeWidth > 0) {

                setStroke(strokeWidth, strokeColor, strokeDashWidth.toFloat(), strokeDashGap.toFloat())
            }
        }

        return this
    }

    companion object {

        fun Builder() = BackgroundBuilder()
    }
}
