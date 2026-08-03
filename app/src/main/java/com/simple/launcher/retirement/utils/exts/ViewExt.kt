package com.simple.launcher.retirement.utils.exts

import android.view.HapticFeedbackConstants
import android.view.View
import com.simple.launcher.retirement.utils.size.Padding

fun View.setPadding(padding: Padding) {

    setPadding(padding.start, padding.top, padding.end, padding.bottom)
}

fun View.setOnSafeClickListener(onSafeClick: (View) -> Unit) {

    var lastTimeClicked: Long = 0

    setOnClickListener {

        if (System.currentTimeMillis() - lastTimeClicked < 1000) return@setOnClickListener
        lastTimeClicked = System.currentTimeMillis()

        onSafeClick(it)
    }
}

fun View.setOnSafeWithPerformHapticFeedbackClickListener(onSafeClick: (View) -> Unit) {

    setOnSafeClickListener {

        it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        onSafeClick(it)
    }
}