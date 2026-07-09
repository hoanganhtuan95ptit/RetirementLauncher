package com.simple.launcher.retirement.utils.view

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
import com.simple.launcher.retirement.utils.combineState
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.utils.size.DP
import kotlinx.coroutines.flow.StateFlow

class BackgroundLinearLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val viewModel: BackgroundViewModel by lazy {
        ViewModelProvider(findViewTreeViewModelStoreOwner()!!)["PhoneticLayout:$id", BackgroundViewModel::class.java]
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val lifecycleOwner = findViewTreeLifecycleOwner() ?: return

        viewModel.background2.launchCollect(lifecycleOwner) {
            setBackground(it)
        }
    }
}

class BackgroundViewModel : BaseViewModel() {

    val background2: StateFlow<Background?> = combineState(resources, null) {

        value = Background.Builder()
            .backgroundColor(Color.WHITE)
            .cornerRadius(DP.DP_16)
            .build()
    }
}