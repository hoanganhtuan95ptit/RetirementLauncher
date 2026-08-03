package com.simple.launcher.retirement.presentation.views

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.utils.exts.asObjectOrNull

class WindowInsetsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context, attrs, defStyleAttr
) {

    val size = MediatorLiveData<SizeData>()

    init {

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->

            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            size.value = SizeData(
                width = context.resources.displayMetrics.widthPixels,
                height = context.resources.displayMetrics.heightPixels,
                statusBarHeight = statusBars.top,
                navigationBarHeight = navigationBars.bottom
            )
            insets
        }
    }

    data class SizeData(
        val width: Int,
        val height: Int,

        val statusBarHeight: Int,
        val navigationBarHeight: Int
    )
}

fun Activity.listenerSize(): LiveData<WindowInsetsView.SizeData>? {

    val rootView = window.decorView.asObjectOrNull<ViewGroup>() ?: return null

    val windowInsetsView = rootView.findViewById(R.id.window_insets_view_item_id) ?: WindowInsetsView(this).apply {

        id = R.id.window_insets_view_item_id
        rootView.addView(this)
    }

    return windowInsetsView.size
}