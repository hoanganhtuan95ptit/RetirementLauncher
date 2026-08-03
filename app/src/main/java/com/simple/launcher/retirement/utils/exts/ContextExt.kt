package com.simple.launcher.retirement.utils.exts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun Context.broadcastReceiverFlow(
    filter: IntentFilter,
    flags: Int = ContextCompat.RECEIVER_NOT_EXPORTED
): Flow<Intent> = callbackFlow {

    val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent != null) {

                trySend(intent)
            }
        }
    }
    ContextCompat.registerReceiver(this@broadcastReceiverFlow, receiver, filter, flags)
    awaitClose {

        unregisterReceiver(receiver)
    }
}
