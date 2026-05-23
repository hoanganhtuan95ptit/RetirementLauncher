package com.simple.launcher.retirement.presentation.settings

import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first

/**
 * Yêu cầu xác thực PIN (verify nếu đã có, setup nếu chưa có).
 * @return true nếu xác thực/thiết lập thành công, false nếu user huỷ.
 */
suspend fun requirePin(): Boolean {

    if (PreferenceRepository.instance.hasPin()) {

        sendDeeplink(DeepLinks.PIN_VERIFY)
    } else {

        sendDeeplink(DeepLinks.PIN_SETUP)
    }

    return AppEventBus.events.filterIsInstance<AppEvent.PinResult>().first() !is AppEvent.PinCancel
}
