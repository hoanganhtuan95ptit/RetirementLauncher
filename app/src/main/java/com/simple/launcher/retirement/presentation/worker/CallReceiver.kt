package com.simple.launcher.retirement.presentation.worker

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
import com.simple.launcher.retirement.domain.repository.PreferenceRepository

class CallReceiver : BroadcastReceiver() {

    private val TAG = "CallReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val repository = PreferenceRepository.instance
        if (!repository.isCallBlockEnabled()) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if (state == TelephonyManager.EXTRA_STATE_RINGING && incomingNumber != null) {
            Log.d(TAG, "Incoming call from: $incomingNumber")
            if (!isNumberInContacts(context, incomingNumber)) {
                Log.d(TAG, "Number not in contacts, blocking...")
                blockCall(context)
            }
        }
    }

    private fun isNumberInContacts(context: Context, number: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) 
            != PackageManager.PERMISSION_GRANTED) {
            return true // Không có quyền thì coi như cho phép để an toàn
        }

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.count > 0) {
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking contacts: ${e.message}")
        } finally {
            cursor?.close()
        }
        return false
    }

    private fun blockCall(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) 
                == PackageManager.PERMISSION_GRANTED) {
                try {
                    telecomManager.endCall()
                    Log.d(TAG, "Call ended successfully via TelecomManager")
                } catch (e: Exception) {
                    Log.e(TAG, "Error ending call: ${e.message}")
                }
            }
        }
    }
}
