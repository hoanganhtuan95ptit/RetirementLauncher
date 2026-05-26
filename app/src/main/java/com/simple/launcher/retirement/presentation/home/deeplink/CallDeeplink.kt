package com.simple.launcher.retirement.presentation.home.deeplink

import android.content.Intent
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.presentation.DeepLinks

@Deeplink
class CallDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.CALL

    override suspend fun navigate(fragmentActivity: FragmentActivity, deeplink: String, extras: Map<String, Any?>?, sharedElement: Map<String, View>?): Boolean {

        val entity = extras?.get("entity") as? ContactEntity

        if (entity != null) try {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = "tel:${entity.phoneNumber}".toUri()
            fragmentActivity.startActivity(callIntent)
        } catch (_: Exception) {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = "tel:${entity.phoneNumber}".toUri()
            fragmentActivity.startActivity(dialIntent)
        }

        return true
    }
}
