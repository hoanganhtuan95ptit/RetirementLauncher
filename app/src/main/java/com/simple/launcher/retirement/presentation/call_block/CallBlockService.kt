package com.simple.launcher.retirement.presentation.call_block

import android.database.Cursor
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

    private fun isNumberInContacts(number: String): Boolean {

        if (!PermissionManager.hasContactPermission()) return true

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        var cursor: Cursor? = null
        try {

            cursor = contentResolver.query(uri, projection, null, null, null)
            return cursor?.count?.let { it > 0 } == true
        } catch (exception: Exception) {

            Log.e(TAG, "Error checking contacts", exception)
        } finally {

            cursor?.close()
        }

        return false
    }

    private fun logDebug(message: String) {

        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    companion object {

        private const val TAG = "CallBlockService"
    }
}
