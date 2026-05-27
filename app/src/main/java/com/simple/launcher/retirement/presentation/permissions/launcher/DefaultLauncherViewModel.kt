package com.simple.launcher.retirement.presentation.permissions.launcher

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.*
import com.simple.launcher.retirement.utils.text.*
import kotlinx.coroutines.flow.StateFlow

class DefaultLauncherViewModel : BaseViewModel() {

    val title: StateFlow<RichText> = combineState(
        flow1 = resources,
        initialValue = RichText("")
    ) { resources ->
        val color = resources.textColorPrimary
        resources.getString(R.string.default_launcher_title)
            .with(ForegroundColor(color), Bold)
            .build()
    }

    val description: StateFlow<RichText> = combineState(
        flow1 = resources,
        initialValue = RichText("")
    ) { resources ->
        val color = resources.textColorSecondary
        val highlightColor = resources.colorAccent

        resources.getString(R.string.default_launcher_desc)
            .with(ForegroundColor(color))
            .withFirst(resources.getString(R.string.default_launcher_highlight), Bold, ForegroundColor(highlightColor))
            .build()
    }

    val action: StateFlow<ActionState> = combineState(
        flow1 = resources,
        initialValue = ActionState.empty()
    ) { resources ->

        val color = resources.colorOnPrimary
        val backgroundColor = resources.colorPrimary

        buildActionState(
            text = resources.getString(R.string.default_launcher_setup),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }
}
