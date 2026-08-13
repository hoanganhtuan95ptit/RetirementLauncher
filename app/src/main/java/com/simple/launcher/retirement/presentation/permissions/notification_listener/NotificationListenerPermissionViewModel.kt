package com.simple.launcher.retirement.presentation.permissions.notification_listener

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.utils.exts.colorAccent
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.launcher.retirement.utils.exts.withStyleTitleLarge
import com.simple.launcher.retirement.utils.exts.withStyleTitleMedium
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigBold
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with
import com.simple.ui.precompute.text.withFirst
import kotlinx.coroutines.flow.StateFlow

class NotificationListenerPermissionViewModel : BaseViewModel() {

    val horizontalPadding: StateFlow<Int> = combineState(resources, 24.dp()) {

        value = 24.dp()
    }

    val topPadding: StateFlow<Int> = combineState(resources, 24.dp()) {

        value = 24.dp()
    }

    val descriptionMarginTop: StateFlow<Int> = combineState(resources, 16.dp()) {

        value = 16.dp()
    }

    val grantButtonMarginTop: StateFlow<Int> = combineState(resources, 24.dp()) {

        value = 24.dp()
    }

    val title: StateFlow<BigText> = combineState(
        flow1 = resources,
        initialValue = BigText("")
    ) { resources ->

        val color = resources.textColorPrimary
        value = resources.getString(R.string.notification_listener_permission_title)
            .withStyleTitleLarge()
            .with(BigForegroundColor(color), BigBold)
            .build()
    }

    val description: StateFlow<BigText> = combineState(
        flow1 = resources,
        initialValue = BigText("")
    ) { resources ->

        val color = resources.textColorSecondary
        val highlightColor = resources.colorAccent

        value = resources.getString(R.string.notification_listener_permission_desc)
            .withStyleTitleMedium()
            .with(BigForegroundColor(color))
            .withFirst(
                resources.getString(R.string.notification_listener_permission_highlight),
                BigBold,
                BigForegroundColor(highlightColor)
            )
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
