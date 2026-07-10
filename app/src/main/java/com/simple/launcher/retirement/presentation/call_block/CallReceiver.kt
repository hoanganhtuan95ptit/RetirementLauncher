package com.simple.launcher.retirement.presentation.call_block

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.domain.repository.PreferenceRepository

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        logDebug("action call")

        val repository = PreferenceRepository.instance
        if (!repository.isCallBlockEnabled()) return
        logDebug("call block enabled")

        val incomingNumber = getIncomingNumber(intent) ?: return
        logDebug("Incoming call from: $incomingNumber")

        if (isNumberInContacts(context, incomingNumber)) return
        logDebug("Number not in contacts, blocking...")

        blockCall(context)
    }

    private fun isNumberInContacts(context: Context, number: String): Boolean {

        // Thiếu quyền đọc contact thì ưu tiên fail-open để tránh chặn nhầm cuộc gọi thật.
        if (!hasReadContactsPermission(context)) return true

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        var cursor: Cursor? = null
        try {

            cursor = context.contentResolver.query(uri, projection, null, null, null)
            return cursor?.count?.let { it > 0 } == true
        } catch (exception: Exception) {
            Log.e(TAG, "Error checking contacts", exception)
        } finally {

            cursor?.close()
        }

        return false
    }

    private fun blockCall(context: Context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        if (!hasAnswerPhoneCallsPermission(context)) return

        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        try {

            telecomManager.endCall()
            logDebug("Call ended successfully via TelecomManager")
        } catch (securityException: SecurityException) {
            Log.e(TAG, "Missing permission to end call", securityException)
        } catch (exception: Exception) {
            Log.e(TAG, "Error ending call", exception)
        }
    }

    private fun getIncomingNumber(intent: Intent): String? {

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        if (state != TelephonyManager.EXTRA_STATE_RINGING) return null

        return intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
    }

    private fun hasReadContactsPermission(context: Context): Boolean {

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasAnswerPhoneCallsPermission(context: Context): Boolean {

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ANSWER_PHONE_CALLS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun logDebug(message: String) {

        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    companion object {

        private const val TAG = "CallReceiver"
    }
}