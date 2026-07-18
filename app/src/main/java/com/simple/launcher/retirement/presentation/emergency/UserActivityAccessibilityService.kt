package com.simple.launcher.retirement.presentation.emergency

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.domain.repository.PreferenceRepository

/**
 * Accessibility service chi ghi nhan cac event tuong tac that cua nguoi dung.
 *
 * Service nay khong doc noi dung man hinh; no chi cap nhat timestamp de SOS biet
 * dien thoai vua duoc su dung that su.
 */
class UserActivityAccessibilityService : AccessibilityService() {

    private val repository = PreferenceRepository.instance
    private var lastRecordedTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        val resolvedEvent = event ?: return
        if (!repository.isEmergencyCallEnabled()) return
        if (!isUserInteractionEvent(resolvedEvent.eventType)) return

        recordUserActivity(resolvedEvent.eventType)
    }

    override fun onInterrupt() {

        logDebug("onInterrupt")
    }

    override fun onServiceConnected() {

        super.onServiceConnected()
        logDebug("onServiceConnected")
    }

    override fun onDestroy() {

        logDebug("onDestroy")
        super.onDestroy()
    }

    private fun isUserInteractionEvent(eventType: Int): Boolean {

        return eventType in USER_INTERACTION_EVENTS
    }

    private fun recordUserActivity(eventType: Int) {

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRecordedTime < RECORD_THROTTLE_MILLIS) {

            return
        }

        lastRecordedTime = currentTime
        repository.setLastUserActivity(currentTime)
        logDebug(
            "Real user activity detected from accessibility event=${eventTypeName(eventType)}, " +
                "timestamp=$currentTime"
        )
    }

    private fun eventTypeName(eventType: Int): String {

        return when (eventType) {

            AccessibilityEvent.TYPE_TOUCH_INTERACTION_START -> "TYPE_TOUCH_INTERACTION_START"
            AccessibilityEvent.TYPE_VIEW_CLICKED -> "TYPE_VIEW_CLICKED"
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> "TYPE_VIEW_LONG_CLICKED"
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> "TYPE_VIEW_SCROLLED"
            else -> eventType.toString()
        }
    }

    private fun logDebug(message: String) {

        if (BuildConfig.DEBUG) {

            Log.d(TAG, message)
        }
    }

    companion object {

        private const val TAG = "UserActivityAccessibility"
        private const val RECORD_THROTTLE_MILLIS = 5_000L

        private val USER_INTERACTION_EVENTS = setOf(
            AccessibilityEvent.TYPE_TOUCH_INTERACTION_START,
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED
        )
    }
}
