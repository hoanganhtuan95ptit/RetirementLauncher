package com.simple.launcher.retirement.utils.size

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.asFlow
import com.simple.auto.register.AutoRegister
import com.simple.component.service.ActivityCreatedService
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.utils.view.listenerSize
import kotlinx.coroutines.flow.MutableStateFlow

val sizeMapFlow = MutableStateFlow<Map<String, Int>>(emptyMap())

val Map<String, Any>.width: Int
    get() = get("width") as? Int ?: 0

val Map<String, Any>.height: Int
    get() = get("height") as? Int ?: 0

val Map<String, Any>.statusBarHeight: Int
    get() = get("statusBarHeight") as? Int ?: 0

val Map<String, Any>.navigationBarHeight: Int
    get() = get("navigationBarHeight") as? Int ?: 0

@AutoRegister(apis = [ActivityCreatedService::class])
class SizeService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {

        fragmentActivity.listenerSize()?.asFlow()?.launchCollect(fragmentActivity) {

            sizeMapFlow.value = mutableMapOf<String, Int>().apply {

                put("width", it.width)
                put("height", it.height)
                put("statusBarHeight", it.statusBarHeight)
                put("navigationBarHeight", it.navigationBarHeight)
            }
        }
    }
}