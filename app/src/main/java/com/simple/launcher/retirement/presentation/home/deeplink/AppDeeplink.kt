package com.simple.launcher.retirement.presentation.home.deeplink

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.sendDeeplinkWithBackStack

@Deeplink
class AppDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.APP

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        val entity = extras?.get("entity") as? AppEntity

        if (entity != null) if (entity.packageName == fragmentActivity.packageName) {

            sendDeeplinkWithBackStack(DeepLinks.SETTINGS)
        } else entity.let {

            fragmentActivity.packageManager.getLaunchIntentForPackage(it.packageName)
        }?.let {

            fragmentActivity.startActivity(it)
        }

        return true
    }
}
