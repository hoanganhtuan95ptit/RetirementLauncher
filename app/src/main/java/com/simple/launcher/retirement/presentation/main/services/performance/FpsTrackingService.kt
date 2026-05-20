package com.simple.launcher.retirement.presentation.main.services.performance

import android.util.Log
import android.view.Choreographer
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.services.FragmentCreatedService

/**
 * Service tự động theo dõi FPS của từng màn hình Fragment.
 * Sử dụng [AutoRegister] để tự động đăng ký với mọi Fragment.
 */
@AutoRegister(apis = [FragmentCreatedService::class])
class FpsTrackingService : FragmentCreatedService {

    override fun setup(fragment: Fragment) {
        val screenName = fragment.javaClass.simpleName
        val tracker = FpsTracker(screenName)

        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                tracker.start()
            }

            override fun onPause(owner: LifecycleOwner) {
                tracker.stop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                tracker.stop()
                fragment.lifecycle.removeObserver(this)
            }
        })
    }

    private class FpsTracker(private val screenName: String) : Choreographer.FrameCallback {
        private var frameCount = 0
        private var startTimeNanos: Long = 0L
        private var isTracking = false

        fun start() {
            if (isTracking) return
            isTracking = true
            frameCount = 0
            startTimeNanos = 0L
            Choreographer.getInstance().postFrameCallback(this)
        }

        fun stop() {
            isTracking = false
            Choreographer.getInstance().removeFrameCallback(this)
        }

        override fun doFrame(frameTimeNanos: Long) {
            if (!isTracking) return

            if (startTimeNanos == 0L) {
                startTimeNanos = frameTimeNanos
            } else {
                frameCount++
                val elapsedNanos = frameTimeNanos - startTimeNanos
                if (elapsedNanos >= 1_000_000_000L) { // 1 second interval
                    val fps = (frameCount * 1_000_000_000.0 / elapsedNanos).toInt()
                    
                    Log.d("FpsTracking", "Screen: $screenName, FPS: $fps")
                    AppEventBus.post(AppEvent.FpsUpdated(screenName, fps))
                    
                    // Reset for next interval
                    frameCount = 0
                    startTimeNanos = frameTimeNanos
                }
            }
            Choreographer.getInstance().postFrameCallback(this)
        }
    }
}
