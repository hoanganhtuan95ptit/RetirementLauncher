package com.simple.launcher.retirement.utils.view

import android.view.View
import com.simple.launcher.retirement.utils.size.Padding

fun View.setPadding(padding: Padding) {
    setPadding(padding.start, padding.top, padding.end, padding.bottom)
}

fun View.setOnSafeClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

class SafeClickListener(
    private var defaultInterval: Int = 1000,
    private val onSafeClick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        if (System.currentTimeMillis() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = System.currentTimeMillis()
        onSafeClick(v)
    }
}
