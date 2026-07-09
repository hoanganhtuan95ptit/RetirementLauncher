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
import com.simple.component.service.ActivityCreatedService
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.main.MainActivity

@AutoRegister(apis = [MainActivity::class])
class PocketModeMainService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {

        val sensorManager = fragmentActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (proximitySensor == null) {
            Log.d(TAG, "setup: proximity sensor not available on this device")
            return
        }

        val overlayView = createOverlayView(fragmentActivity)
        val sensorListener = createSensorListener(
            fragmentActivity = fragmentActivity,
            overlayView = overlayView,
            proximitySensor = proximitySensor
        )

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

    private fun createOverlayView(fragmentActivity: FragmentActivity): View {

        // View trong suốt phủ toàn màn hình để chặn toàn bộ thao tác khi máy đang ở trong túi.
        return View(fragmentActivity).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = true
            isFocusable = true
            isFocusableInTouchMode = true
        }
    }

    private fun createSensorListener(
        fragmentActivity: FragmentActivity,
        overlayView: View,
        proximitySensor: Sensor
    ): SensorEventListener {

        return object : SensorEventListener {

            override fun onSensorChanged(event: SensorEvent) {

                if (!PreferenceRepository.instance.isPocketModeEnabled()) {
                    removeOverlay(fragmentActivity, overlayView)
                    return
                }

                val isNear = event.values[0] < proximitySensor.maximumRange
                Log.d(TAG, "onSensorChanged: isNear=$isNear")

                if (isNear) {
                    showOverlay(fragmentActivity, overlayView)
                    return
                }

                removeOverlay(fragmentActivity, overlayView)
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            }
        }
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
