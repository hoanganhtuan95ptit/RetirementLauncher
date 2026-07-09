package com.simple.launcher.retirement.presentation.permissions.overlay

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.*
import com.simple.launcher.retirement.utils.text.*
import kotlinx.coroutines.flow.StateFlow

class OverlayPermissionViewModel : BaseViewModel() {

    val title: StateFlow<RichText> = combineState(
        flow1 = resources,
        initialValue = RichText("")
    ) { resources ->
        val color = resources.textColorPrimary
        value = resources.getString(R.string.overlay_permission_title)
            .with(ForegroundColor(color), Bold)
            .build()
    }

    val description: StateFlow<RichText> = combineState(
        flow1 = resources,
        initialValue = RichText("")
    ) { resources ->
        val color = resources.textColorSecondary
        val highlightColor = resources.colorAccent

        value = resources.getString(R.string.overlay_permission_desc)
            .with(ForegroundColor(color))
            .withFirst(resources.getString(R.string.overlay_permission_highlight), Bold, ForegroundColor(highlightColor))
            .build()
    }

    val action: StateFlow<ActionState> = combineState(
        flow1 = resources,
        initialValue = ActionState.empty()
    ) { resources ->

        val color = resources.colorOnPrimary
        val backgroundColor = resources.colorPrimary

        value = buildActionState(
            text = resources.getString(R.string.permission_grant),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }
}
