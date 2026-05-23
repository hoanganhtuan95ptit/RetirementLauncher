package com.simple.launcher.retirement.presentation.main.services.performance

import androidx.fragment.app.FragmentActivity
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.utils.services.ActivityCreatedService

@AutoRegister(apis = [ActivityCreatedService::class])
class FpsTrackingService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {
        if (!BuildConfig.DEBUG) return
    }
}