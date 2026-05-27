package com.simple.launcher.retirement.presentation.block

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.text.*
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class BlockContentState(
    val title: RichText,
    val message: RichText,
    val appName: String? = null
)

class BlockViewModel : BaseViewModel() {

    private val _appName = MutableStateFlow<String?>(null)

    fun setAppName(name: String?) {
        _appName.value = name
    }

    val content: StateFlow<BlockContentState> = combineState(
        flow1 = resources,
        flow2 = _appName,
        initialValue = BlockContentState(emptyText(), emptyText())
    ) { resources, appName ->
        val titleColor = resources.getColor(android.R.attr.textColorPrimary)
        val messageColor = resources.getColor(android.R.attr.textColorSecondary)
        BlockContentState(
            title = resources.getString(R.string.block_title)
                .with(ForegroundColor(titleColor))
                .build(),
            message = resources.getString(R.string.block_desc)
                .with(ForegroundColor(messageColor))
                .build(),
            appName = appName
        )
    }

    val action: StateFlow<ActionState> = combineState(
        flow1 = resources,
        initialValue = ActionState.empty()
    ) { resources ->

        val color = resources.getColor(com.google.android.material.R.attr.colorOnPrimary)
        val backgroundColor = resources.getColor(android.R.attr.colorPrimary, android.graphics.Color.LTGRAY)

        buildActionState(
            text = resources.getString(R.string.block_go_home),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }
}
