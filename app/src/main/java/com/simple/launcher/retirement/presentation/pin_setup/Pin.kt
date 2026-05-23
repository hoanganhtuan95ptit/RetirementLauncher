package com.simple.launcher.retirement.presentation.pin_setup

import android.util.Log
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.utils.EventBus
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object Pin {

    suspend fun verify(): PinResult {

        if (PreferenceRepository.instance.hasPin()) {

            sendDeeplink(DeepLinks.PIN_VERIFY)
            return PinEventBus.events.first()
        } else {

            Log.d("tuanha", "verify: ")
            sendDeeplink(DeepLinks.PIN_SETUP)
            Log.d("tuanha", "verify: 2")
            val result =  PinEventBus.events.map {
                Log.d("tuanha", "verify: $it")
                it
            }.filterNotNull().first()
            Log.d("tuanha", "verify: 3")
            return result
        }
    }

    sealed class PinResult

    object PinCancel : PinResult()

    object PinSetupSuccess : PinResult()

    object PinVerifySuccess : PinResult()


    object PinEventBus : EventBus<Pin.PinResult>()
}