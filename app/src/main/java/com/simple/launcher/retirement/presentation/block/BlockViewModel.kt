package com.simple.launcher.retirement.presentation.block

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.StateFlow

data class BlockContentState(
    val title: RichText,
    val message: RichText
)

class BlockViewModel : BaseViewModel() {

    val content: StateFlow<BlockContentState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = BlockContentState("".toRich(), "".toRich())
    ) { stringMap, themeMap ->
        val titleColor = themeMap.getColor(android.R.attr.textColorPrimary)
        val messageColor = themeMap.getColor(android.R.attr.textColorSecondary)
        BlockContentState(
            title = stringMap.getString(R.string.block_title).toRich().with(ForegroundColor(titleColor)),
            message = stringMap.getString(R.string.block_desc).toRich().with(ForegroundColor(messageColor))
        )
    }

    val action: StateFlow<ActionState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = ActionState.empty()
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight, android.graphics.Color.LTGRAY)

        buildActionState(
            text = stringMap.getString(R.string.block_go_home),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }
}
