package com.simple.launcher.retirement.presentation.call_block

import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.utils.permission.PermissionManager

@RequiresApi(Build.VERSION_CODES.Q)
class CallBlockService : CallScreeningService() {

    // ── 3. Public API ─────────────────────────────────────────────────────

    override fun onScreenCall(callDetails: Call.Details) {

        logDebug("onScreenCall called: ${callDetails.handle}")

        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) return

        val repository = PreferenceRepository.instance
        val isEnabled = repository.isCallBlockEnabled()
        logDebug("Call block enabled setting: $isEnabled")

        if (!isEnabled) {

            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val handle = callDetails.handle
        val incomingNumber = handle?.schemeSpecificPart ?: ""

        if (incomingNumber.isEmpty()) {

            logDebug("Incoming number is empty, allowing call.")
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        logDebug("Incoming call from: $incomingNumber")

        if (isNumberInContacts(incomingNumber)) {

            logDebug("Number $incomingNumber found in contacts, allowing call.")
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        logDebug("Number $incomingNumber NOT in contacts, blocking...")
        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(true)
            .build()

        respondToCall(callDetails, response)
    }

    // ── 4. Private helpers ────────────────────────────────────────────────

    private fun isNumberInContacts(number: String): Boolean {

        if (!PermissionManager.hasContactPermission()) {

            // Fail-open: mất quyền READ_CONTACTS → không block để tránh chặn nhầm
            // cuộc gọi hợp lệ khi user vô tình gỡ quyền. Log rõ để dev/QA biết
            // call block đang bypass ngoài ý muốn.
            Log.w(TAG, "READ_CONTACTS missing — allowing call as fail-open bypass")
            return true
        }

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        return try {

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->

                cursor.count > 0
            } == true
        } catch (exception: Exception) {

            Log.e(TAG, "Error checking contacts", exception)
            false
        }
    }

    private fun logDebug(message: String) {

        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    // ── 6. Companion object ───────────────────────────────────────────────

    companion object {

        private const val TAG = "CallBlockService"
    }
}
