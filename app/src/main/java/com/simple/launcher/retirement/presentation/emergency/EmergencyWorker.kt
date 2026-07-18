package com.simple.launcher.retirement.presentation.emergency

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.emergency.utils.EmergencyUtils
import com.simple.launcher.retirement.presentation.services.worker.BackgroundWorker
import com.simple.launcher.retirement.utils.permission.PermissionManager
import kotlinx.coroutines.flow.Flow

/**
 * Worker theo doi muc do tuong tac voi thiet bi de tu dong goi lien he khan cap
 * khi nguoi dung khong hoat dong qua nguong da cau hinh.
 */
class EmergencyWorker(context: Context) : BackgroundWorker(context) {

    private val handler = Handler(Looper.getMainLooper())
    private val repository = PreferenceRepository.instance
    private val contactRepository = ContactRepository.instance
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

    private var isStarted = false
    private var isSosSessionActive = false
    private var sosSessionStartedAt = 0L
    private var sosCallAttemptCount = 0

    private val checkRunnable = object : Runnable {

        override fun run() {

            if (!isStarted) {

                logDebug { "checkRunnable ignored because worker is stopped" to null }
                return
            }

            // Poll dinh ky thay vi dung alarm vi worker nay chi can chay khi background service dang bat.
            checkInactivity()
            if (isStarted) {

                val nextInterval = resolveNextCheckInterval()
                logDebug { "Scheduling next inactivity check in ${nextInterval / 1000}s" to null }
                handler.postDelayed(this, nextInterval)
            }
        }
    }

    override fun observeEnabled(): Flow<Boolean> = repository.emergencyCallEnabledFlow()

    override fun onStart() {

        logDebug { "onStart" to null }
        isStarted = true
        handler.removeCallbacks(checkRunnable)
        handler.post(checkRunnable)
    }

    override fun onStop() {

        logDebug { "onStop" to null }
        isStarted = false
        handler.removeCallbacks(checkRunnable)
        resetEmergencyState()
    }

    private fun checkInactivity() {

        if (!repository.isEmergencyCallEnabled()) {

            logDebug { "Emergency call disabled while checking, resetting state" to null }
            resetEmergencyState()
            return
        }

        val lastActivity = repository.getLastUserActivity()
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastActivity

        logDebug {

            "checkInactivity: elapsed=${elapsed / 1000}s, " +
                    "lastActivity=$lastActivity, " +
                    "sessionActive=$isSosSessionActive, " +
                    "sessionStartedAt=$sosSessionStartedAt, " +
                    "attemptCount=$sosCallAttemptCount, " +
                    "lastEmergencyIndex=${repository.getLastEmergencyIndex()}" to null
        }

        if (!PermissionManager.hasUserActivityAccessibilityPermission()) {

            logDebug { "Accessibility service is disabled, skipping emergency check" to null }
            return
        }

        if (shouldResetSosSession(lastActivity, elapsed)) {

            logDebug { "Resetting SOS state because recent user activity was detected" to null }
            resetEmergencyState()
            return
        }

        // Gioi han cung bao ve truong hop exclusion period qua dai lam active timeout khong bao gio cham nguong.
        if (elapsed >= ABSOLUTE_HARD_LIMIT_MILLIS) {

            logDebug { "Hard limit reached, triggering emergency" to null }
            startOrContinueSosSession(currentTime)
            return
        }

        // Active timeout chi tinh cac khoang nam ngoai exclusion period do nguoi dung cau hinh.
        val timeoutMillis = repository.getEmergencyTimeout()
        val activeElapsed = EmergencyUtils.calculateActiveTime(lastActivity, currentTime, repository.getExclusionPeriods())
        logDebug {

            "Active time check: activeElapsed=${activeElapsed / 1000}s, " +
                    "timeout=${timeoutMillis / 1000}s, " +
                    "exclusionPeriods=${repository.getExclusionPeriods().size}" to null
        }

        if (activeElapsed >= timeoutMillis) {

            logDebug { "Active timeout reached (${activeElapsed / 1000 / 60} min active), triggering emergency" to null }
            startOrContinueSosSession(currentTime)
            return
        }
    }

    private fun startOrContinueSosSession(currentTime: Long) {

        if (!isSosSessionActive) {

            logDebug { "Starting SOS session" to null }
            isSosSessionActive = true
            sosSessionStartedAt = currentTime
            sosCallAttemptCount = 0
            repository.setLastEmergencyIndex(NO_CONTACT_INDEX)
            tryCallNextContact()
            return
        }

        logDebug { "Continuing SOS session" to null }
        tryCallNextContact()
    }

    private fun shouldResetSosSession(lastActivity: Long, elapsed: Long): Boolean {

        // Neu Accessibility ghi nhan tuong tac sau khi SOS bat dau, dung session va quay ve chu ky binh thuong.
        if (isSosSessionActive && lastActivity > sosSessionStartedAt) {

            return true
        }

        // Truong hop co tuong tac moi thi reset vong quay contact de lan canh bao sau bat dau lai tu dau.
        return !isSosSessionActive && elapsed <= CHECK_INTERVAL_MILLIS
    }

    private fun tryCallNextContact() {

        val contacts = contactRepository.getSelectedContacts()
        logDebug {

            "tryCallNextContact: contactCount=${contacts.size}, " +
                    "attemptCount=$sosCallAttemptCount, " +
                    "lastEmergencyIndex=${repository.getLastEmergencyIndex()}" to null
        }
        if (contacts.isEmpty()) {

            logDebug { "No selected emergency contacts" to null }
            finishSosSession()
            return
        }

        if (sosCallAttemptCount >= contacts.size) {

            logDebug { "All selected emergency contacts were attempted" to null }
            finishSosSession()
            return
        }

        // Thu lan luot tung contact de tranh ket o mot so bi thieu hoac khong the goi.
        while (sosCallAttemptCount < contacts.size) {

            val nextIndex = resolveNextContactIndex(contacts.size)
            val phoneNumber = contacts.getOrNull(nextIndex)?.phoneNumber

            // Luu index truoc khi goi de lan sau tiep tuc qua contact ke tiep trong vong tron.
            repository.setLastEmergencyIndex(nextIndex)
            sosCallAttemptCount++
            logDebug {

                "Trying emergency contact index=$nextIndex, " +
                        "attempt=$sosCallAttemptCount/${contacts.size}, " +
                        "phone=${maskPhoneNumber(phoneNumber)}" to null
            }

            if (phoneNumber.isNullOrEmpty()) {

                logDebug { "Contact at index=$nextIndex has empty phone number, skipping" to null }
                continue
            }

            if (!makeEmergencyCall(phoneNumber)) {

                logDebug { "Failed to call contact at index=$nextIndex, trying next contact" to null }
                continue
            }

            return
        }

        logDebug { "Unable to place emergency call to any selected contact" to null }
        finishSosSession()
    }

    private fun resolveNextContactIndex(contactCount: Int): Int {

        // Repository luu contact vua duoc thu gan nhat; contact tiep theo se quay vong ve dau danh sach.
        val nextIndex = repository.getLastEmergencyIndex() + 1
        return if (nextIndex >= contactCount) 0 else nextIndex
    }

    private fun makeEmergencyCall(phoneNumber: String): Boolean {

        if (telephonyManager.simState != TelephonyManager.SIM_STATE_READY) {

            logDebug { "SIM not ready, skipping call. simState=${telephonyManager.simState}" to null }
            return false
        }

        if (!hasCallPermission()) {

            logDebug { "No permission to make call (CALL_PHONE)" to null }
            return false
        }

        return try {

            val uri = Uri.fromParts("tel", phoneNumber, null)
            telecomManager.placeCall(uri, null)
            logDebug { "placeCall is currently disabled in code for ${maskPhoneNumber(phoneNumber)} uri=$uri" to null }
            true
        } catch (securityException: SecurityException) {

            logDebug { "Missing permission to place call" to securityException }
            false
        } catch (exception: Exception) {

            logDebug { "System failed to place call" to exception }
            false
        }
    }

    private fun hasCallPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun resetEmergencyState() {

        // Sau khi nguoi dung hoat dong lai, lan canh bao tiep theo se bat dau tu contact dau tien.
        logDebug {

            "resetEmergencyState: sessionActive=$isSosSessionActive, " +
                    "attemptCount=$sosCallAttemptCount, " +
                    "lastEmergencyIndex=${repository.getLastEmergencyIndex()}" to null
        }
        isSosSessionActive = false
        sosSessionStartedAt = 0L
        sosCallAttemptCount = 0
        repository.setLastEmergencyIndex(NO_CONTACT_INDEX)
    }

    private fun finishSosSession() {

        logDebug {

            "finishSosSession: attemptCount=$sosCallAttemptCount, " +
                    "lastEmergencyIndex=${repository.getLastEmergencyIndex()}" to null
        }
        isSosSessionActive = false
        sosSessionStartedAt = 0L
        sosCallAttemptCount = 0
        repository.setLastEmergencyIndex(NO_CONTACT_INDEX)
    }

    private fun resolveNextCheckInterval(): Long {

        return if (isSosSessionActive) SOS_CHECK_INTERVAL_MILLIS else CHECK_INTERVAL_MILLIS
    }

    private fun maskPhoneNumber(phoneNumber: String?): String {

        if (phoneNumber.isNullOrEmpty()) return "empty"
        val suffix = phoneNumber.takeLast(PHONE_MASK_SUFFIX_LENGTH)
        return "***$suffix"
    }

    private fun logDebug(action: () -> Pair<String, Throwable?>) {

        if (DEBUG) {

            val pair = action.invoke()

            Log.d(TAG, pair.first, pair.second)
        }
    }

    companion object {

        private const val TAG = "EmergencyCallWorker"
        private val DEBUG = BuildConfig.DEBUG

        // Gioi han theo thoi gian thuc, khong tru exclusion period.
        private val ABSOLUTE_HARD_LIMIT_MILLIS = 10 * 60 * 60 * 1000L

        private val CHECK_INTERVAL_MILLIS = if (DEBUG) 30 * 1000L else 30 * 60 * 1000L
        private val SOS_CHECK_INTERVAL_MILLIS = if (DEBUG) 30 * 1000L else 5 * 60 * 1000L

        private const val NO_CONTACT_INDEX = -1
        private const val PHONE_MASK_SUFFIX_LENGTH = 4
    }
}
