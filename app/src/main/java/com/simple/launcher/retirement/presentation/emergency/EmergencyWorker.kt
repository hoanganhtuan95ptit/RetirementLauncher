package com.simple.launcher.retirement.presentation.emergency

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.services.worker.BackgroundWorker
import kotlinx.coroutines.flow.Flow

class EmergencyWorker(context: Context) : BackgroundWorker(context) {

    private val handler = Handler(Looper.getMainLooper())
    private val repository = PreferenceRepository.instance
    private val contactRepository = ContactRepository.instance
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    private var lastCallTime = 0L
    private var lastAutoCallTriggerTime = 0L

    private val activityReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {

            val action = intent?.action ?: return
            if (shouldIgnoreActivityAction(action)) {

                logDebug("Ignoring system screen event after app-triggered call: $action")
                return
            }

            logDebug("User activity detected: $action")
            updateLastUserActivity()
        }
    }

    private val checkRunnable = object : Runnable {

        override fun run() {

            checkInactivity()
            handler.postDelayed(this, CHECK_INTERVAL_MILLIS)
        }
    }

    override fun observeEnabled(): Flow<Boolean> = repository.isEmergencyCallEnabledFlow()

    override fun onStart() {

        logDebug("onStart")
        registerActivityReceiver()
        handler.post(checkRunnable)
        updateLastUserActivity()
    }

    override fun onStop() {

        logDebug("onStop")
        try {

            context.unregisterReceiver(activityReceiver)
        } catch (_: Exception) {
        }
        handler.removeCallbacks(checkRunnable)
    }

    private fun checkInactivity() {

        val lastActivity = repository.getLastUserActivity()
        val currentTime = System.currentTimeMillis()

        // Có tương tác mới thì reset vòng quay contact để lần cảnh báo sau bắt đầu lại từ đầu.
        if (currentTime - lastActivity <= INACTIVITY_TIMEOUT_MILLIS) {

            logDebug("Có tương tác mới thì reset vòng quay contact để lần cảnh báo sau bắt đầu lại từ đầu.")
            resetEmergencyState()
            return
        }

        if (currentTime - lastCallTime < CALL_COOLDOWN_MILLIS) {

            logDebug("checkInactivity")
            return
        }

        tryCallNextContact(currentTime)
    }

    private fun tryCallNextContact(currentTime: Long) {

        val contacts = contactRepository.getSelectedContacts()
        if (contacts.isEmpty()) {

            return
        }

        repeat(contacts.size) {

            val nextIndex = resolveNextContactIndex(contacts.size)
            val phoneNumber = contacts.getOrNull(nextIndex)?.phoneNumber

            repository.setLastEmergencyIndex(nextIndex)

            if (phoneNumber.isNullOrEmpty()) {

                logDebug("Contact at index=$nextIndex has empty phone number, skipping")
                return@repeat
            }

            if (!makeEmergencyCall(phoneNumber)) {

                logDebug("Failed to call contact at index=$nextIndex, trying next contact")
                return@repeat
            }

            lastCallTime = currentTime
            return
        }

        logDebug("Unable to place emergency call to any selected contact")
    }

    private fun resolveNextContactIndex(contactCount: Int): Int {

        val nextIndex = repository.getLastEmergencyIndex() + 1
        return if (nextIndex >= contactCount) 0 else nextIndex
    }

    private fun makeEmergencyCall(phoneNumber: String): Boolean {

        logDebug("Triggering emergency call to $phoneNumber")

        if (telephonyManager.simState != TelephonyManager.SIM_STATE_READY) {

            logDebug("SIM not ready, skipping call")
            return false
        }

        if (!hasCallPermission()) {

            logDebug("No permission to make call (CALL_PHONE)")
            return false
        }

        return try {

            val uri = Uri.fromParts("tel", phoneNumber, null)
            telecomManager.placeCall(uri, null)
            lastAutoCallTriggerTime = System.currentTimeMillis()
            logDebug("call $phoneNumber")
            true
        } catch (securityException: SecurityException) {
            logDebug("Missing permission to place call", securityException)
            false
        } catch (exception: Exception) {
            logDebug("System failed to place call", exception)
            false
        }
    }

    private fun registerActivityReceiver() {

        val filter = IntentFilter().apply {

            // Nhóm action này đủ để xem người dùng còn tương tác thiết bị hay không.
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            context.registerReceiver(activityReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            return
        }

        context.registerReceiver(activityReceiver, filter)
    }

    private fun hasCallPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldIgnoreActivityAction(action: String): Boolean {

        if (action != Intent.ACTION_SCREEN_ON && action != Intent.ACTION_SCREEN_OFF) {

            return false
        }

        if (lastAutoCallTriggerTime == 0L) {

            return false
        }

        return System.currentTimeMillis() - lastAutoCallTriggerTime <= AUTO_CALL_SCREEN_EVENT_IGNORE_WINDOW_MILLIS
    }

    private fun updateLastUserActivity() {

        lastAutoCallTriggerTime = 0L
        repository.setLastUserActivity(System.currentTimeMillis())
    }

    private fun resetEmergencyState() {

        repository.setLastEmergencyIndex(NO_CONTACT_INDEX)
        lastCallTime = 0L
        lastAutoCallTriggerTime = 0L
    }

    private fun logDebug(message: String, throwable: Throwable? = null) {

        if (BuildConfig.DEBUG) {

            Log.d(TAG, message, throwable)
        }
    }

    companion object {

        private const val TAG = "EmergencyCallWorker"
        private val INACTIVITY_TIMEOUT_MILLIS = if (BuildConfig.DEBUG) 10 * 60 * 1000L else 12 * 60 * 60 * 1000L
        private val CALL_COOLDOWN_MILLIS = if (BuildConfig.DEBUG) 1 * 60 * 1000L else 10 * 60 * 1000L
        private val CHECK_INTERVAL_MILLIS = if (BuildConfig.DEBUG) 30 * 1000L else 30 * 60 * 1000L
        private const val AUTO_CALL_SCREEN_EVENT_IGNORE_WINDOW_MILLIS = 60 * 1000L
        private const val NO_CONTACT_INDEX = -1
    }
}
