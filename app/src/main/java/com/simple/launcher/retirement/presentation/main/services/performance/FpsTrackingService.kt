package com.simple.launcher.retirement.presentation.main.services.performance

import androidx.fragment.app.FragmentActivity
import com.simple.auto.register.AutoRegister
import com.simple.component.service.ActivityCreatedService
import com.simple.launcher.retirement.BuildConfig

@AutoRegister(apis = [ActivityCreatedService::class])
class FpsTrackingService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {
        if (!BuildConfig.DEBUG) return
    }
}