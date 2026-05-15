package com.simple.launcher.retirement.utils.size

import android.content.res.Resources
import android.util.TypedValue

fun Int.toPx(): Int {

    return toFloat().toPx().toInt()
}

fun Float.toPx(): Float {

    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)
}
