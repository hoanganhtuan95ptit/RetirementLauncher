package com.simple.launcher.retirement.presentation

import com.simple.deeplink.sendDeeplink

/**
 * Tập trung toàn bộ deeplink strings của app.
 *
 * Thay vì rải string literals khắp nơi, hãy dùng các constant ở đây:
 *   sendDeeplink(DeepLinks.HOME)
 *   sendDeeplink(DeepLinks.reorder(type = "apps", ids = listOf(...)))
 */
object DeepLinks {

    // ─── Screens ──────────────────────────────────────────────────────────────


    const val CALL          = "app://call"
    const val APP          = "app://app"
    const val HOME          = "app://home"
    const val ONBOARDING    = "app://onboarding"
    const val SETTINGS      = "app://settings"
    const val APP_LIST      = "app://app_list"
    const val CONTACT_LIST  = "app://contact_list"
    const val CLEAN_FILES   = "app://clean_files"
    const val CLEAN_MEMORY  = "app://clean_memory"
    const val REORDER       = "app://reorder"
    const val PIN_SETUP     = "app://pin_setup"
    const val PIN_VERIFY    = "app://pin_verify"
    const val EMERGENCY_CALL = "app://emergency_call"
    const val APP_MONITORING_INTRO = "app://app_monitoring_intro"
    const val EMERGENCY_CALL_INTRO = "app://emergency_call_intro"
    const val FILE_CLEANUP_INTRO = "app://file_cleanup_intro"
    const val CALL_BLOCK_INTRO = "app://call_block_intro"

    // ─── Permissions ──────────────────────────────────────────────────────────

    const val PERMISSION_CALL            = "app://CallPermission"
    const val PERMISSION_FILE           = "app://FilePermission"
    const val PERMISSION_OVERLAY        = "app://OverlayPermission"
    const val PERMISSION_USAGE_STATS    = "app://UsageStatsPermission"
    const val PERMISSION_CALL_BLOCK     = "app://CallBlockPermission"
    const val PERMISSION_DEFAULT_LAUNCHER = "app://DefaultLauncher"

    // ─── Extras keys ──────────────────────────────────────────────────────────

    object Extras {
        const val ADD_TO_BACK_STACK = "addToBackStack"
        const val REORDER_TYPE      = "type"
        const val REORDER_IDS       = "ids"
    }

    // ─── Reorder types ────────────────────────────────────────────────────────

    object ReorderType {
        const val APPS     = "apps"
        const val CONTACTS = "contacts"
    }

    // ─── Helper extras builders ───────────────────────────────────────────────

    /** Extras để navigate với back-stack. */
    fun withBackStack(): Map<String, Any?> =
        mapOf(Extras.ADD_TO_BACK_STACK to true)

    /**
     * Extras cho màn Reorder.
     * @param type  [ReorderType.APPS] hoặc [ReorderType.CONTACTS]
     * @param ids   Danh sách ID theo thứ tự mới
     */
    fun reorderExtras(type: String, ids: List<*>): Map<String, Any?> =
        mapOf(
            Extras.REORDER_TYPE     to type,
            Extras.REORDER_IDS      to ids,
            Extras.ADD_TO_BACK_STACK to true
        )
}

// ─── Convenience send functions ───────────────────────────────────────────────

/** Navigate tới một màn có addToBackStack = true. */
fun sendDeeplinkWithBackStack(deeplink: String) {
    sendDeeplink(deeplink, extras = DeepLinks.withBackStack())
}

/** Navigate tới màn Reorder với type và danh sách IDs. */
fun sendReorderDeeplink(type: String, ids: List<*>) {
    sendDeeplink(DeepLinks.REORDER, extras = DeepLinks.reorderExtras(type, ids))
}

/** Navigate tới màn Reorder app. */
fun sendReorderAppsDeeplink(ids: List<*>) =
    sendReorderDeeplink(DeepLinks.ReorderType.APPS, ids)

/** Navigate tới màn Reorder contact. */
fun sendReorderContactsDeeplink(ids: List<*>) =
    sendReorderDeeplink(DeepLinks.ReorderType.CONTACTS, ids)
