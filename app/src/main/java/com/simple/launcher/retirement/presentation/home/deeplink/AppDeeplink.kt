package com.simple.launcher.retirement.presentation.home.deeplink

import android.content.Context
import android.content.Intent
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

        val entity = extras?.get("entity") as? AppEntity ?: return false

        if (entity.packageName == fragmentActivity.packageName) {

            sendDeeplinkWithBackStack(DeepLinks.SETTINGS)
            return true
        }

        val intent = buildLaunchIntent(fragmentActivity, entity)
        if (intent != null) {

            startAppActivity(fragmentActivity, intent, entity.packageName)
        }

        return true
    }

    // ── 4. Private helpers ──────────────────────────────────────────────────────────────

    private fun buildLaunchIntent(context: Context, entity: AppEntity): Intent? {

        if (entity.className.isEmpty()) {

            return context.packageManager.getLaunchIntentForPackage(entity.packageName)
        }

        return try {

            Intent(Intent.ACTION_MAIN).apply {

                addCategory(Intent.CATEGORY_LAUNCHER)
                setClassName(entity.packageName, entity.className)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } catch (_: Exception) {

            null
        }
    }

    private fun startAppActivity(context: Context, intent: Intent, packageName: String) {

        try {

            context.startActivity(intent)
        } catch (_: Exception) {

            // Fallback nếu manual intent thất bại
            try {

                context.packageManager.getLaunchIntentForPackage(packageName)?.let {

                    context.startActivity(it)
                }
            } catch (_: Exception) {

            }
        }
    }
}
