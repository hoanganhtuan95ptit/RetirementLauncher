package com.simple.launcher.retirement.presentation.main.services

import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.simple.auto.register.AutoRegister
import com.simple.component.service.ActivityCreatedService

@AutoRegister(apis = [ActivityCreatedService::class])
class TrackingMainService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {

    }
}
