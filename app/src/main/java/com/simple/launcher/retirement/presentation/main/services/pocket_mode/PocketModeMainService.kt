package com.simple.launcher.retirement.presentation.main.services.pocket_mode

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.utils.services.ActivityCreatedService

@AutoRegister(apis = [MainActivity::class])
class PocketModeMainService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {
        val sensorManager = fragmentActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (proximitySensor == null) {
            Log.d(TAG, "setup: proximity sensor not available on this device")
            return
        }

        // View trong suốt phủ toàn màn hình, chặn toàn bộ touch khi điện thoại trong túi
        val overlayView = View(fragmentActivity).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = true
            isFocusable = true
            isFocusableInTouchMode = true
        }

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val repository = PreferenceRepository.Companion.instance
                if (!repository.isPocketModeEnabled()) {
                    removeOverlay(fragmentActivity, overlayView)
                    return
                }
                val isNear = event.values[0] < proximitySensor.maximumRange
                Log.d(TAG, "onSensorChanged: isNear=$isNear")
                if (isNear) showOverlay(fragmentActivity, overlayView)
                else removeOverlay(fragmentActivity, overlayView)
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        fragmentActivity.lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onResume(owner: LifecycleOwner) {
                Log.d(TAG, "onResume: registering proximity sensor listener")
                sensorManager.registerListener(
                    sensorListener,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }

            override fun onPause(owner: LifecycleOwner) {
                Log.d(TAG, "onPause: unregistering proximity sensor listener")
                sensorManager.unregisterListener(sensorListener)
                removeOverlay(fragmentActivity, overlayView)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                sensorManager.unregisterListener(sensorListener)
            }
        })
    }

    private fun showOverlay(activity: FragmentActivity, overlayView: View) {
        val decorView = activity.window.decorView as? ViewGroup ?: return
        if (overlayView.parent == null) {
            Log.d(TAG, "showOverlay: blocking touches (pocket detected)")
            decorView.addView(
                overlayView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    private fun removeOverlay(activity: FragmentActivity, overlayView: View) {
        val decorView = activity.window.decorView as? ViewGroup ?: return
        if (overlayView.parent != null) {
            Log.d(TAG, "removeOverlay: restoring touch input")
            decorView.removeView(overlayView)
        }
    }

    companion object {
        private const val TAG = "tuanha"
    }
}