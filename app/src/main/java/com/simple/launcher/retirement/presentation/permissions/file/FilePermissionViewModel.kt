package com.simple.launcher.retirement.presentation.permissions.file

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.colorAccent
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.launcher.retirement.utils.exts.withAlpha
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigBold
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with
import com.simple.ui.precompute.text.withFirst
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FilePermissionViewModel : BaseViewModel() {

    val isAgreed = MutableStateFlow(false)

    val title: StateFlow<BigText> = combineState(
        flow1 = resources,
        initialValue = BigText("")
    ) { resources ->
        val color = resources.textColorPrimary
        value = resources.getString(R.string.file_permission_title)
            .with(BigForegroundColor(color), BigBold)
            .build()
    }

    val message: StateFlow<BigText> = combineState(
        flow1 = resources,
        initialValue = BigText("")
    ) { resources ->
        val color = resources.textColorSecondary
        val highlightColor = resources.colorAccent

        value = resources.getString(R.string.file_permission_desc)
            .with(BigForegroundColor(color))
            .withFirst(resources.getString(R.string.file_permission_highlight), BigBold, BigForegroundColor(highlightColor))
            .build()
    }

    val checkboxText: StateFlow<BigText> = combineState(
        resources,
        BigText("")
    ) { resources ->

        val color = resources.textColorPrimary
        value = resources.getString(R.string.permission_checkbox_understand)
            .with(BigForegroundColor(color))
            .build()
    }

    val action: StateFlow<ActionState> = combineState(
        flow1 = resources,
        flow2 = isAgreed,
        initialValue = ActionState.empty()
    ) { resources, isAgreed ->

        val color = resources.colorOnPrimary
        val backgroundColor = resources.colorPrimary

        value = buildActionState(
            text = resources.getString(R.string.permission_grant),
            textColor = color,
            backgroundColor = backgroundColor,
            isEnabled = isAgreed
        ).run {
            if (isAgreed) this
            else copy(background = background?.copy(backgroundColor = backgroundColor.withAlpha(0.5f))?.refresh())
        }
    }

    val declineAction: StateFlow<ActionState> = combineState(
        resources,
        ActionState.empty()
    ) { resources ->

        val color = resources.textColorSecondary
        value = buildActionState(
            text = resources.getString(R.string.permission_decline),
            textColor = color,
            backgroundColor = android.graphics.Color.TRANSPARENT
        )
    }
}
