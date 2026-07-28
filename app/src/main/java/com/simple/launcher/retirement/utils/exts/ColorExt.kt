package com.simple.launcher.retirement.utils.exts

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.services.getColor

fun Int.withAlpha(alpha: Float): Int {
    return ColorUtils.setAlphaComponent(this, (alpha * 255).toInt().coerceIn(0, 255))
}

val Map<String, Any>.colorPrimary: Int @ColorInt get() = getColor(android.R.attr.colorPrimary)
val Map<String, Any>.colorOnPrimary: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorOnPrimary)
val Map<String, Any>.colorPrimaryContainer: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorPrimaryContainer)
val Map<String, Any>.colorOnPrimaryContainer: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorOnPrimaryContainer)

val Map<String, Any>.colorSecondary: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorSecondary)
val Map<String, Any>.colorOnSecondary: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorOnSecondary)
val Map<String, Any>.colorSecondaryContainer: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorSecondaryContainer)
val Map<String, Any>.colorOnSecondaryContainer: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorOnSecondaryContainer)

val Map<String, Any>.colorTertiary: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorTertiary)
val Map<String, Any>.colorOnTertiary: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorOnTertiary)
val Map<String, Any>.colorTertiaryContainer: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorTertiaryContainer)
val Map<String, Any>.colorOnTertiaryContainer: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorOnTertiaryContainer)

//val Map<String, Any>.colorError: Int @ColorInt get() = getColor(android.R.attr.colorError)
val Map<String, Any>.colorOnError: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorOnError)
val Map<String, Any>.colorErrorContainer: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorErrorContainer)
val Map<String, Any>.colorOnErrorContainer: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorOnErrorContainer)

val Map<String, Any>.colorBackground: Int @ColorInt get() = getColor(android.R.attr.colorBackground)
val Map<String, Any>.colorOnBackground: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorOnBackground)

val Map<String, Any>.colorSurface: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorSurface)
val Map<String, Any>.colorOnSurface: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorOnSurface)

val Map<String, Any>.colorSurfaceVariant: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorSurfaceVariant)
val Map<String, Any>.colorOnSurfaceVariant: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorOnSurfaceVariant)

val Map<String, Any>.colorOutline: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorOutline)

val Map<String, Any>.colorPrimaryInverse: Int @ColorInt get() = getColor(com.google.android.material.R.attr.colorPrimaryInverse)

// ─── Common Attributes ───────────────────────────────────────────────────────

val Map<String, Any>.textColorPrimary: Int @ColorInt get() = getColor(android.R.attr.textColorPrimary)
val Map<String, Any>.textColorSecondary: Int @ColorInt get() = getColor(android.R.attr.textColorSecondary)
val Map<String, Any>.colorAccent: Int @ColorInt get() = getColor(android.R.attr.colorAccent)

// ─── Custom — Clean Files Stat Card ──────────────────────────────────────────

val Map<String, Any>.colorCleanFilesStatCardBgActive: Int @ColorInt get() = getColor(R.attr.colorCleanFilesStatCardBgActive)
val Map<String, Any>.colorCleanFilesStatCardOnBgActive: Int @ColorInt get() = getColor(R.attr.colorCleanFilesStatCardOnBgActive)
val Map<String, Any>.colorCleanFilesStatCardBgIdle: Int @ColorInt get() = getColor(R.attr.colorCleanFilesStatCardBgIdle)
val Map<String, Any>.colorCleanFilesStatCardOnBgIdle: Int @ColorInt get() = getColor(R.attr.colorCleanFilesStatCardOnBgIdle)

// ─── Custom — Clean Memory Stat Card ─────────────────────────────────────────

val Map<String, Any>.colorCleanMemoryStatCardBgActive: Int @ColorInt get() = getColor(R.attr.colorCleanMemoryStatCardBgActive)
val Map<String, Any>.colorCleanMemoryStatCardOnBgActive: Int @ColorInt get() = getColor(R.attr.colorCleanMemoryStatCardOnBgActive)
val Map<String, Any>.colorCleanMemoryStatCardBgIdle: Int @ColorInt get() = getColor(R.attr.colorCleanMemoryStatCardBgIdle)
val Map<String, Any>.colorCleanMemoryStatCardOnBgIdle: Int @ColorInt get() = getColor(R.attr.colorCleanMemoryStatCardOnBgIdle)
