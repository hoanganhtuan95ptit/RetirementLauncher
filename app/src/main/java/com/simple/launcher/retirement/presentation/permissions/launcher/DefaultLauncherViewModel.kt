package com.simple.launcher.retirement.presentation.permissions.launcher

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.text.*
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.getColor
import kotlinx.coroutines.flow.StateFlow

class DefaultLauncherViewModel : BaseViewModel() {

    val title: StateFlow<RichText> = combineState(
        flow1 = resources,
        initialValue = RichText("")
    ) { resources ->
        val color = resources.getColor(android.R.attr.textColorPrimary)
        resources.getString(R.string.default_launcher_title)
            .with(ForegroundColor(color), Bold)
            .build()
    }

    val description: StateFlow<RichText> = combineState(
        flow1 = resources,
        initialValue = RichText("")
    ) { resources ->
        val color = resources.getColor(android.R.attr.textColorSecondary)
        val highlightColor = resources.getColor(android.R.attr.colorAccent)

        resources.getString(R.string.default_launcher_desc)
            .with(ForegroundColor(color))
            .withFirst(resources.getString(R.string.default_launcher_highlight), Bold, ForegroundColor(highlightColor))
            .build()
    }

    val action: StateFlow<ActionState> = combineState(
        flow1 = resources,
        initialValue = ActionState.empty()
    ) { resources ->

        val color = resources.getColor(com.google.android.material.R.attr.colorOnPrimary)
        val backgroundColor = resources.getColor(android.R.attr.colorPrimary, android.graphics.Color.LTGRAY)

        buildActionState(
            text = resources.getString(R.string.default_launcher_setup),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }
}
