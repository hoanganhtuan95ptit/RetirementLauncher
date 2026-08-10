package com.simple.launcher.retirement.presentation.emergency

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.emergency.utils.EmergencyUtils
import com.simple.launcher.retirement.presentation.services.worker.BackgroundWorker
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.permission.PermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * Worker theo doi muc do tuong tac voi thiet bi de tu dong goi lien he khan cap
 * khi nguoi dung khong hoat dong qua nguong da cau hinh.
 */
class EmergencyWorker(context: Context) : BackgroundWorker(context) {

    // ── 1. Fields ─────────────────────────────────────────────────────────

    // Không dùng main looper — các lần poll đọc SharedPreferences + telecom IPC,
    // không nên chiếm main thread. HandlerThread sẽ được tạo/dọn trong onStart/onStop.
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    private val repository = PreferenceRepository.instance
    private val contactRepository = ContactRepository.instance
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

    // Snapshot của contact được chọn, cập nhật liên tục từ getSelectedContactsFlow().
    // Đọc từ Handler thread trong callNextEmergencyContactIfPossible() nên @Volatile.
    @Volatile
    private var selectedContacts: List<ContactEntity> = emptyList()

    @Volatile
    private var isStarted = false
    @Volatile
    private var isSosSessionActive = false
    @Volatile
    private var sosSessionStartedAt = 0L
    // Man hinh canh bao (Screen A) dang hien thi. Trong luc nay khong duoc phep
    // launch lai activity hay chuyen thang qua SOS session — cho ket qua tu alert.
    @Volatile
    private var isAlertShowing = false
    // Timestamp luc launch alert de bao ve khi event ket qua khong toi (activity bi kill
    // giua chung...): sau ALERT_SAFETY_TIMEOUT_MILLIS ma van chua co ket qua, coi nhu
    // co the phat hien lai timeout va lam lai.
    @Volatile
    private var alertLaunchedAt = 0L
    // Backed by SharedPreferences (KEY_SOS_CALL_ATTEMPT_COUNT) — persist qua process kill
    // để session SOS đang chạy dở không bị reset về contact đầu.
    @Volatile
    private var sosCallAttemptCount = 0

    private val checkRunnable = object : Runnable {

        override fun run() = runCheckCycle(this)
    }

    // ── 3. Public API (overrides từ BackgroundWorker) ─────────────────────

    override fun observeEnabled(): Flow<Boolean> = repository.emergencyCallEnabledFlow()

    override fun attach(scope: CoroutineScope) {

        super.attach(scope)
        scope.launch {

            contactRepository.getSelectedContactsFlow().collect { contacts ->

                selectedContacts = contacts
            }
        }
        scope.launch {

            AppEventBus.events
                .filterIsInstance<AppEvent.EmergencyAlertResult>()
                .collect { result -> handleAlertResult(result) }
        }
    }

    override fun onStart() {

        logDebug { "onStart" to null }
        if (handlerThread?.isAlive == true) return

        val thread = HandlerThread(WORKER_THREAD_NAME).also { it.start() }
        handlerThread = thread
        handler = Handler(thread.looper)

        isStarted = true
        // Khôi phục attempt count từ preference (persist qua process kill giữa SOS session).
        sosCallAttemptCount = repository.getSosCallAttemptCount()
        handler?.post(checkRunnable)
    }

    override fun onStop() {

        logDebug { "onStop" to null }
        isStarted = false
        handler?.removeCallbacks(checkRunnable)
        handlerThread?.quitSafely()
        handler = null
        handlerThread = null
        resetSosCallingState()
        isAlertShowing = false
        alertLaunchedAt = 0L
    }

    // ── 4. Private helpers ────────────────────────────────────────────────

    private fun runCheckCycle(runnable: Runnable) {

        if (!isStarted) {

            logDebug { "checkRunnable ignored because worker is stopped" to null }
            return
        }

        // Poll dinh ky thay vi dung alarm vi worker nay chi can chay khi background service dang bat.
        checkEmergencyTriggerConditions()
        if (isStarted) scheduleNextCheck(runnable)
    }

    private fun scheduleNextCheck(runnable: Runnable) {

        val nextInterval = resolveNextPollingInterval()
        logDebug { "Scheduling next inactivity check in ${nextInterval / 1000}s" to null }
        handler?.postDelayed(runnable, nextInterval)
    }

    private fun checkEmergencyTriggerConditions() {

        if (!repository.isEmergencyCallEnabled()) {

            logDebug { "Emergency call disabled while checking, resetting state" to null }
            resetSosCallingState()
            return
        }

        val lastActivity = repository.getLastUserActivity()
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastActivity

        logDebug {

            "checkEmergencyTriggerConditions: elapsed=${elapsed / 1000}s, " +
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

        if (shouldStopSosSessionAfterUserActivity(lastActivity, elapsed)) {

            logDebug { "Resetting SOS state because recent user activity was detected" to null }
            resetSosCallingState()
            return
        }

        // Neu alert dang hien va van con trong thoi han an toan, cho ket qua tu activity.
        if (isAlertShowing) {

            if (currentTime - alertLaunchedAt < ALERT_SAFETY_TIMEOUT_MILLIS) {

                logDebug { "Alert is showing, waiting for user response" to null }
                return
            }
            // Vuot qua thoi han an toan — activity co the da bi kill; reset co de lan sau chay tiep.
            logDebug { "Alert safety timeout exceeded, resetting alert flag" to null }
            isAlertShowing = false
            alertLaunchedAt = 0L
        }

        // Gioi han cung bao ve truong hop exclusion period qua dai lam active timeout khong bao gio cham nguong.
        if (elapsed >= ABSOLUTE_HARD_LIMIT_MILLIS) {

            logDebug { "Hard limit reached, triggering emergency" to null }
            triggerEmergencyFlow(currentTime)
            return
        }

        // Đọc exclusion periods 1 lần cho cả compute + log để tránh deserialize JSON 2 lần.
        val exclusionPeriods = repository.getExclusionPeriods()

        // Active timeout chi tinh cac khoang nam ngoai exclusion period do nguoi dung cau hinh.
        val timeoutMillis = repository.getEmergencyTimeout()
        val activeElapsed = EmergencyUtils.calculateActiveElapsedMillis(
            lastActivity,
            currentTime,
            exclusionPeriods
        )
        logDebug {

            "Active time check: activeElapsed=${activeElapsed / 1000}s, " +
                    "timeout=${timeoutMillis / 1000}s, " +
                    "exclusionPeriods=${exclusionPeriods.size}" to null
        }

        if (activeElapsed >= timeoutMillis) {

            logDebug {

                "Active timeout reached (${activeElapsed / 1000 / 60} min active), " +
                    "triggering emergency" to null
            }
            triggerEmergencyFlow(currentTime)
            return
        }
    }

    /**
     * Diem giao thoa moi: neu SOS session da chay (dang cycle qua cac contact),
     * tiep tuc goi contact ke tiep. Neu chua, hien man hinh canh bao (Screen A)
     * de nguoi dung co co hoi bam "Toi an toan" truoc khi thuc su goi.
     */
    private fun triggerEmergencyFlow(currentTime: Long) {

        if (isSosSessionActive) {

            startOrContinueSosCallingSession(currentTime)
            return
        }

        launchEmergencyAlert(currentTime)
    }

    private fun launchEmergencyAlert(currentTime: Long) {

        isAlertShowing = true
        alertLaunchedAt = currentTime
        logDebug { "Launching EmergencyAlertActivity for safety confirmation" to null }
        try {

            // Overlay perm da duoc yeu cau tu SetEmergencyCallEnabledUseCase — startActivity
            // tu background service se hoat dong nhu BlockActivity dang lam.
            val intent = EmergencyAlertActivity.createLaunchIntent(context)
            context.startActivity(intent)
        } catch (exception: Exception) {

            logDebug { "Failed to launch EmergencyAlertActivity, falling back to direct call" to exception }
            // Neu khong the show activity (edge case hiem), fallback ve luong cu de dam bao an toan.
            isAlertShowing = false
            alertLaunchedAt = 0L
            startOrContinueSosCallingSession(currentTime)
        }
    }

    private fun handleAlertResult(result: AppEvent.EmergencyAlertResult) {

        isAlertShowing = false
        alertLaunchedAt = 0L

        when (result) {

            AppEvent.EmergencyAlertConfirmedSafe -> {

                logDebug { "Alert result: user is safe, resetting SOS state" to null }
                // Activity da cap nhat lastUserActivity roi; chi can bao dam SOS state sach.
                resetSosCallingState()
            }

            AppEvent.EmergencyAlertTimedOut -> {

                logDebug { "Alert result: timed out, starting SOS calling session" to null }
                startOrContinueSosCallingSession(System.currentTimeMillis())
            }
        }
    }

    private fun startOrContinueSosCallingSession(currentTime: Long) {

        if (!isSosSessionActive) {

            logDebug { "Starting SOS session" to null }
            isSosSessionActive = true
            sosSessionStartedAt = currentTime
            sosCallAttemptCount = 0
            repository.setSosCallAttemptCount(0)
            repository.setLastEmergencyIndex(NO_CONTACT_INDEX)
            callNextEmergencyContactIfPossible()
            return
        }

        logDebug { "Continuing SOS session" to null }
        callNextEmergencyContactIfPossible()
    }

    private fun shouldStopSosSessionAfterUserActivity(lastActivity: Long, elapsed: Long): Boolean {

        // Neu Accessibility ghi nhan tuong tac sau khi SOS bat dau, dung session va quay ve chu ky binh thuong.
        if (isSosSessionActive && lastActivity > sosSessionStartedAt) {

            return true
        }

        // Truong hop co tuong tac moi thi reset vong quay contact de lan canh bao sau bat dau lai tu dau.
        return !isSosSessionActive && elapsed <= CHECK_INTERVAL_MILLIS
    }

    private fun callNextEmergencyContactIfPossible() {

        val contacts = selectedContacts
        logDebug {

            "callNextEmergencyContactIfPossible: contactCount=${contacts.size}, " +
                    "attemptCount=$sosCallAttemptCount, " +
                    "lastEmergencyIndex=${repository.getLastEmergencyIndex()}" to null
        }
        if (contacts.isEmpty()) {

            logDebug { "No selected emergency contacts" to null }
            finishSosCallingSession()
            return
        }

        if (sosCallAttemptCount >= contacts.size) {

            logDebug { "All selected emergency contacts were attempted" to null }
            finishSosCallingSession()
            return
        }

        // Thu lan luot tung contact de tranh ket o mot so bi thieu hoac khong the goi.
        while (sosCallAttemptCount < contacts.size) {

            if (tryCallNextContact(contacts)) return
        }

        logDebug { "Unable to place emergency call to any selected contact" to null }
        finishSosCallingSession()
    }

    private fun tryCallNextContact(contacts: List<ContactEntity>): Boolean {

        val nextIndex = resolveNextContactIndex(contacts.size)
        val phoneNumber = contacts.getOrNull(nextIndex)?.phoneNumber

        // Luu index truoc khi goi de lan sau tiep tuc qua contact ke tiep trong vong tron.
        repository.setLastEmergencyIndex(nextIndex)
        sosCallAttemptCount++
        // Persist attempt count để nếu process bị OS kill giữa session, restart worker
        // vẫn tiếp tục từ contact kế thay vì gọi lại từ đầu.
        repository.setSosCallAttemptCount(sosCallAttemptCount)
        logDebug {

            "Trying emergency contact index=$nextIndex, " +
                "attempt=$sosCallAttemptCount/${contacts.size}, " +
                "phone=${maskPhoneNumberForLog(phoneNumber)}" to null
        }

        if (phoneNumber.isNullOrEmpty()) {

            logDebug { "Contact at index=$nextIndex has empty phone number, skipping" to null }
            return false
        }

        if (!placeEmergencyCall(phoneNumber)) {

            logDebug { "Failed to call contact at index=$nextIndex, trying next contact" to null }
            return false
        }

        return true
    }

    private fun resolveNextContactIndex(contactCount: Int): Int {

        // Repository luu contact vua duoc thu gan nhat; contact tiep theo se quay vong ve dau danh sach.
        val nextIndex = repository.getLastEmergencyIndex() + 1
        return if (nextIndex >= contactCount) 0 else nextIndex
    }

    private fun placeEmergencyCall(phoneNumber: String): Boolean {

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
            logDebug {

                "placeCall dispatched to telecom for ${maskPhoneNumberForLog(phoneNumber)} uri=$uri" to null
            }
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

    private fun resetSosCallingState() {

        // Sau khi nguoi dung hoat dong lai, lan canh bao tiep theo se bat dau tu contact dau tien.
        logDebug {

            "resetSosCallingState: sessionActive=$isSosSessionActive, " +
                    "attemptCount=$sosCallAttemptCount, " +
                    "lastEmergencyIndex=${repository.getLastEmergencyIndex()}" to null
        }
        isSosSessionActive = false
        sosSessionStartedAt = 0L
        sosCallAttemptCount = 0
        repository.setSosCallAttemptCount(0)
        repository.setLastEmergencyIndex(NO_CONTACT_INDEX)
    }

    private fun finishSosCallingSession() {

        logDebug {

            "finishSosCallingSession: attemptCount=$sosCallAttemptCount, " +
                    "lastEmergencyIndex=${repository.getLastEmergencyIndex()}" to null
        }
        isSosSessionActive = false
        sosSessionStartedAt = 0L
        sosCallAttemptCount = 0
        repository.setSosCallAttemptCount(0)
        repository.setLastEmergencyIndex(NO_CONTACT_INDEX)
    }

    private fun resolveNextPollingInterval(): Long {

        return if (isSosSessionActive) SOS_CHECK_INTERVAL_MILLIS else CHECK_INTERVAL_MILLIS
    }

    private fun maskPhoneNumberForLog(phoneNumber: String?): String {

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

    // ── 6. Companion object ───────────────────────────────────────────────

    companion object {

        private const val TAG = "EmergencyCallWorker"
        private const val WORKER_THREAD_NAME = "EmergencyWorkerThread"
        private val DEBUG = BuildConfig.DEBUG

        // Gioi han theo thoi gian thuc, khong tru exclusion period.
        // Đồng bộ với README (11 tiếng) — trước đây code là 10h gây lệch tài liệu.
        private val ABSOLUTE_HARD_LIMIT_MILLIS = 11 * 60 * 60 * 1000L

        private val CHECK_INTERVAL_MILLIS = if (DEBUG) 30 * 1000L else 30 * 60 * 1000L
        private val SOS_CHECK_INTERVAL_MILLIS = if (DEBUG) 30 * 1000L else 5 * 60 * 1000L

        // Neu alert launch xong ma qua thoi han nay van chua co ket qua (activity bi kill,
        // event bi mat...), coi nhu alert "chet" va cho phep chu ky ke tiep launch lai.
        // Tinh: countdown + 2 phut buffer (debug 30s -> 2.5 min, release 5 min -> 7 min).
        private val ALERT_SAFETY_TIMEOUT_MILLIS = if (DEBUG) 150 * 1000L else 7 * 60 * 1000L

        private const val NO_CONTACT_INDEX = -1
        private const val PHONE_MASK_SUFFIX_LENGTH = 4
    }
}
