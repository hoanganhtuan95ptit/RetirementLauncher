package com.simple.launcher.retirement.presentation.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow

class EmergencyCallWorker(context: Context) : BackgroundWorker(context) {

    private val TAG = "EmergencyCallWorker"
    private val TIMEOUT = 12 * 60 * 60 * 1000L // 12 hours

    private val handler = Handler(Looper.getMainLooper())
    private val repository = PreferenceRepository.instance
    private var lastCallTime = 0L

    private val activityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BuildConfig.DEBUG) Log.d(TAG, "User activity detected: ${intent?.action}")
            repository.setLastUserActivity(System.currentTimeMillis())
        }
    }

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkInactivity()
            handler.postDelayed(this, 30 * 60 * 1000L) // Check every 30 mins
        }
    }

    override fun observeEnabled(): Flow<Boolean> = repository.isEmergencyCallEnabledFlow()

    override fun onStart() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStart")
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(activityReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(activityReceiver, filter)
        }
        
        handler.post(checkRunnable)
        // Cập nhật hoạt động lần đầu khi bắt đầu để tránh trigger ngay lập tức nếu chưa có dữ liệu cũ
        repository.setLastUserActivity(System.currentTimeMillis())
    }

    override fun onStop() {
        if (BuildConfig.DEBUG) Log.d(TAG, "onStop")
        try {
            context.unregisterReceiver(activityReceiver)
        } catch (e: Exception) {
            // Ignore
        }
        handler.removeCallbacks(checkRunnable)
    }

    private fun checkInactivity() {
        val lastActivity = repository.getLastUserActivity()
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastActivity > TIMEOUT) {
            // Nếu đã 12h không hoạt động, kiểm tra xem vừa gọi cách đây bao lâu
            // Nếu vừa gọi trong vòng 10 phút thì chưa gọi tiếp để tránh spam
            if (currentTime - lastCallTime < 10 * 60 * 1000L) return

            val contacts = com.simple.launcher.retirement.domain.repository.ContactRepository.instance.getSelectedContacts()
            if (contacts.isEmpty()) return

            var nextIndex = repository.getLastEmergencyIndex() + 1
            if (nextIndex >= contacts.size) nextIndex = 0

            val contact = contacts[nextIndex]
            val phoneNumber = contact.phoneNumber
            if (!phoneNumber.isNullOrEmpty()) {
                if (makeEmergencyCall(phoneNumber)) {
                    repository.setLastEmergencyIndex(nextIndex)
                    lastCallTime = currentTime
                } else {
                    // Nếu lỗi hệ thống không gọi được số này, thử số kế tiếp ngay lập tức
                    repository.setLastEmergencyIndex(nextIndex)
                    checkInactivity()
                }
            }
        } else {
            // Có hoạt động, reset index về -1 để lần sau gọi từ đầu
            repository.setLastEmergencyIndex(-1)
            lastCallTime = 0L
        }
    }

    private fun makeEmergencyCall(phoneNumber: String): Boolean {
        if (BuildConfig.DEBUG) Log.d(TAG, "Triggering emergency call to $phoneNumber")
        
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        // 1. Kiểm tra nhanh tình trạng SIM/Sóng (nếu máy đang ở chế độ máy bay hoặc không có SIM thì bỏ qua luôn)
        if (telephonyManager.simState != TelephonyManager.SIM_STATE_READY) {
            Log.e(TAG, "SIM not ready, skipping call")
            return false
        }

        return try {
            val uri = Uri.fromParts("tel", phoneNumber, null)
            
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                telecomManager.placeCall(uri, null)
                // Chúng ta không biết chắc họ có nghe máy không, nhưng lệnh gọi đã được gửi đi thành công
                true
            } else {
                Log.e(TAG, "No permission to make call (CALL_PHONE)")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "System failed to place call", e)
            false
        }
    }
}
