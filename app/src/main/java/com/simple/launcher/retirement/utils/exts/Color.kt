package com.simple.launcher.retirement.utils.exts

import androidx.core.graphics.ColorUtils

fun Int.withAlpha(alpha: Float): Int {
    return ColorUtils.setAlphaComponent(this, (alpha * 255).toInt().coerceIn(0, 255))
}