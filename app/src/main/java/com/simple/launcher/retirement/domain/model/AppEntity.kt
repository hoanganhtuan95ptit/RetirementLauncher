package com.simple.launcher.retirement.domain.model

import android.graphics.drawable.Drawable

data class AppEntity(
    val label: String,
    val packageName: String,
    val icon: Drawable
)
