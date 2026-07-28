package com.simple.launcher.retirement.presentation.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.observe
import kotlinx.coroutines.flow.StateFlow

class BackgroundLinearLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val viewModel: BackgroundViewModel by lazy {
        ViewModelProvider(findViewTreeViewModelStoreOwner()!!)["PhoneticLayout:$id", BackgroundViewModel::class.java]
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val lifecycleOwner = findViewTreeLifecycleOwner() ?: return

        viewModel.background2.observe(lifecycleOwner) {
            setBackground(it)
        }
    }
}

class BackgroundViewModel : BaseViewModel() {

    val background2: StateFlow<Background?> = combineState(resources, null) {

        value = Background.Builder()
            .backgroundColor(Color.WHITE)
            .cornerRadius(16.dp())
            .build()
    }
}
