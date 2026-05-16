package com.simple.launcher.retirement.presentation.main.services

import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.utils.services.ActivityStartedService

@AutoRegister(apis = [ActivityStartedService::class])
class TrackingMainService: ActivityStartedService {
    override fun setup(fragmentActivity: FragmentActivity) {
        Log.d("tuanha", "setup: ")
    }
}