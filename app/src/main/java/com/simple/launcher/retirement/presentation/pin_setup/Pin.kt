package com.simple.launcher.retirement.presentation.pin_setup

import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.utils.EventBus
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

object Pin {

    suspend fun verify(): PinResult {

        if (PreferenceRepository.instance.hasPin()) {

            sendDeeplink(DeepLinks.PIN_VERIFY)
            return PinEventBus.events.filter { it is Pin.PinCancel || it is Pin.PinVerifySuccess }.first()
        } else {

            sendDeeplink(DeepLinks.PIN_SETUP)
            return PinEventBus.events.filter { it is Pin.PinCancel || it is Pin.PinSetupSuccess }.first()
        }
    }

    sealed class PinResult

    object PinCancel : PinResult()

    object PinSetupSuccess : PinResult()

    object PinVerifySuccess : PinResult()


    object PinEventBus : EventBus<Pin.PinResult>()
}