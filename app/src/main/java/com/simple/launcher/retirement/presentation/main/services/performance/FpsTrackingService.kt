package com.simple.launcher.retirement.presentation.main.services.performance

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.FrameMetrics
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.services.ActivityCreatedService
import java.util.Locale

@AutoRegister(apis = [ActivityCreatedService::class])
class FpsTrackingService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {
        if (!BuildConfig.DEBUG) return

        Log.d("tuanha", "setup: ")
        fragmentActivity.lifecycle.addObserver(
            ActivityPerformanceTracker(fragmentActivity)
        )
    }

    private class ActivityPerformanceTracker(private val activity: FragmentActivity) : DefaultLifecycleObserver, Window.OnFrameMetricsAvailableListener {

        companion object {

            private const val REPORT_INTERVAL_MS = 500L

            private const val NS_TO_MS = 1_000_000.0

            private const val FROZEN_FRAME_NS = 700_000_000L
            private const val SLOW_FRAME_NS = 32_000_000L
        }

        // ─────────────────────────────────────────────────────────────
        // UI
        // ─────────────────────────────────────────────────────────────

        private var overlayView: TextView? = null

        private val mainHandler =
            Handler(Looper.getMainLooper())

        // ─────────────────────────────────────────────────────────────
        // Metrics
        // ─────────────────────────────────────────────────────────────

        private var trackingStartNs = 0L

        private var totalFrames = 0
        private var jankFrames = 0
        private var severeJankFrames = 0
        private var slowFrames = 0
        private var frozenFrames = 0

        private var totalFrameNs = 0L
        private var totalInputNs = 0L
        private var totalAnimationNs = 0L
        private var totalLayoutNs = 0L
        private var totalDrawNs = 0L
        private var totalSyncNs = 0L
        private var totalCommandNs = 0L
        private var totalSwapNs = 0L

        private var maxFrameMs = 0.0

        private val refreshRate: Float
            @RequiresApi(Build.VERSION_CODES.R)
            get() = activity.display?.refreshRate ?: 60f

        private val frameBudgetNs: Double
            @RequiresApi(Build.VERSION_CODES.R)
            get() = 1_000_000_000.0 / refreshRate

        // ─────────────────────────────────────────────────────────────
        // Lifecycle
        // ─────────────────────────────────────────────────────────────

        override fun onResume(owner: LifecycleOwner) {
            start()
        }

        override fun onPause(owner: LifecycleOwner) {
            stop()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            removeOverlay()
        }

        // ─────────────────────────────────────────────────────────────
        // Start / Stop
        // ─────────────────────────────────────────────────────────────

        private fun start() {

            resetMetrics()

            showOverlay()

            activity.window.addOnFrameMetricsAvailableListener(
                this,
                mainHandler,
            )

            scheduleReport()
        }

        private fun stop() {

            try {
                activity.window
                    .removeOnFrameMetricsAvailableListener(this)
            } catch (_: Exception) {
            }

            mainHandler.removeCallbacksAndMessages(null)
        }

        // ─────────────────────────────────────────────────────────────
        // Metrics Callback
        // ─────────────────────────────────────────────────────────────

        @RequiresApi(Build.VERSION_CODES.R)
        override fun onFrameMetricsAvailable(
            window: Window,
            frameMetrics: FrameMetrics,
            dropCountSinceLastInvocation: Int,
        ) {

            if (trackingStartNs == 0L) {
                trackingStartNs = System.nanoTime()
            }

            totalFrames++

            val total =
                frameMetrics.metric(FrameMetrics.TOTAL_DURATION)

            val input =
                frameMetrics.metric(FrameMetrics.INPUT_HANDLING_DURATION)

            val animation =
                frameMetrics.metric(FrameMetrics.ANIMATION_DURATION)

            val layout =
                frameMetrics.metric(FrameMetrics.LAYOUT_MEASURE_DURATION)

            val draw =
                frameMetrics.metric(FrameMetrics.DRAW_DURATION)

            val sync =
                frameMetrics.metric(FrameMetrics.SYNC_DURATION)

            val command =
                frameMetrics.metric(FrameMetrics.COMMAND_ISSUE_DURATION)

            val swap =
                frameMetrics.metric(FrameMetrics.SWAP_BUFFERS_DURATION)

            totalFrameNs += total
            totalInputNs += input
            totalAnimationNs += animation
            totalLayoutNs += layout
            totalDrawNs += draw
            totalSyncNs += sync
            totalCommandNs += command
            totalSwapNs += swap

            val frameMs = total / NS_TO_MS

            if (frameMs > maxFrameMs) {
                maxFrameMs = frameMs
            }

            if (total > frameBudgetNs) {
                jankFrames++
            }

            if (total > frameBudgetNs * 2) {
                severeJankFrames++
            }

            if (total > SLOW_FRAME_NS) {
                slowFrames++
            }

            if (total > FROZEN_FRAME_NS) {
                frozenFrames++
            }
        }

        // ─────────────────────────────────────────────────────────────
        // Report
        // ─────────────────────────────────────────────────────────────

        private fun scheduleReport() {

            mainHandler.postDelayed(
                object : Runnable {
                    override fun run() {

                        updateOverlay()

                        mainHandler.postDelayed(
                            this,
                            REPORT_INTERVAL_MS,
                        )
                    }
                },
                REPORT_INTERVAL_MS,
            )
        }

        private fun updateOverlay() {

            val frameCount =
                totalFrames.coerceAtLeast(1)

            val elapsedNs =
                System.nanoTime() - trackingStartNs

            val fps =
                (totalFrames * 1_000_000_000.0 / elapsedNs)

            val avgFrameMs =
                nsToMs(totalFrameNs / frameCount)

            val jankPercent =
                (jankFrames.toDouble() / frameCount) * 100

            val builder = SpannableStringBuilder()

            appendMetric(
                builder,
                "FPS",
                "%.1f",
                fps,
                fpsColor(fps),
            )

            appendMetric(
                builder,
                "\nJank",
                "%.1f%%",
                jankPercent,
                percentColor(jankPercent, 5.0, 10.0),
            )

            appendMetric(
                builder,
                "\nAvg",
                "%.2f ms",
                avgFrameMs,
                durationColor(avgFrameMs, 16.0, 20.0),
            )

            appendMetric(
                builder,
                "\nMax",
                "%.2f ms",
                maxFrameMs,
                durationColor(maxFrameMs, 20.0, 33.0),
            )

            builder.append("\n")

            appendMetric(
                builder,
                "\nLayout",
                "%.2f",
                avgNs(totalLayoutNs, frameCount),
                durationColor(avgNs(totalLayoutNs, frameCount), 4.0, 8.0),
            )

            appendMetric(
                builder,
                "\nDraw",
                "%.2f",
                avgNs(totalDrawNs, frameCount),
                durationColor(avgNs(totalDrawNs, frameCount), 3.0, 6.0),
            )

            appendMetric(
                builder,
                "\nSync",
                "%.2f",
                avgNs(totalSyncNs, frameCount),
                durationColor(avgNs(totalSyncNs, frameCount), 2.0, 5.0),
            )

            appendMetric(
                builder,
                "\nGPU",
                "%.2f",
                avgNs(totalCommandNs + totalSwapNs, frameCount),
                durationColor(
                    avgNs(totalCommandNs + totalSwapNs, frameCount),
                    4.0,
                    8.0,
                ),
            )

            appendMetric(
                builder,
                "\nSlow",
                "%d",
                slowFrames,
                countColor(slowFrames, 3, 10),
            )

            appendMetric(
                builder,
                "\nFrozen",
                "%d",
                frozenFrames,
                countColor(frozenFrames, 0, 1),
            )

            overlayView?.text = builder

            AppEventBus.post(
                AppEvent.FpsUpdated(
                    activity.javaClass.simpleName,
                    fps.toInt(),
                )
            )
        }

        // ─────────────────────────────────────────────────────────────
        // Overlay
        // ─────────────────────────────────────────────────────────────

        private fun showOverlay() {

            if (overlayView != null) return

            val view =
                TextView(activity).apply {

                    setBackgroundColor(
                        Color.argb(180, 0, 0, 0)
                    )

                    setTextColor(Color.WHITE)

                    textSize = 10f

                    typeface = Typeface.MONOSPACE

                    setPadding(24, 16, 24, 16)

                    setLineSpacing(0f, 1.1f)
                }

            val params =
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                ).apply {

                    gravity = Gravity.TOP or Gravity.START

                    topMargin = 120
                    leftMargin = 20
                }

            (activity.window.decorView as? ViewGroup)
                ?.addView(view, params)

            overlayView = view
        }

        private fun removeOverlay() {

            overlayView?.let {

                (activity.window.decorView as? ViewGroup)
                    ?.removeView(it)
            }

            overlayView = null
        }

        // ─────────────────────────────────────────────────────────────
        // Utils
        // ─────────────────────────────────────────────────────────────

        private fun resetMetrics() {

            trackingStartNs = System.nanoTime()

            totalFrames = 0
            jankFrames = 0
            severeJankFrames = 0
            slowFrames = 0
            frozenFrames = 0

            totalFrameNs = 0
            totalInputNs = 0
            totalAnimationNs = 0
            totalLayoutNs = 0
            totalDrawNs = 0
            totalSyncNs = 0
            totalCommandNs = 0
            totalSwapNs = 0

            maxFrameMs = 0.0
        }

        private fun appendMetric(
            builder: SpannableStringBuilder,
            title: String,
            format: String,
            value: Any,
            color: Int,
        ) {

            builder.append("$title: ")

            val start = builder.length

            builder.append(
                String.format(
                    Locale.US,
                    format,
                    value,
                )
            )

            builder.setSpan(
                ForegroundColorSpan(color),
                start,
                builder.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }

        private fun avgNs(
            value: Long,
            frameCount: Int,
        ): Double {
            return nsToMs(value / frameCount)
        }

        private fun nsToMs(
            ns: Long,
        ): Double {
            return ns / NS_TO_MS
        }

        private fun fpsColor(fps: Double): Int {
            return when {
                fps >= 58 -> Color.GREEN
                fps >= 50 -> Color.YELLOW
                else -> Color.RED
            }
        }

        private fun durationColor(
            value: Double,
            warning: Double,
            bad: Double,
        ): Int {
            return when {
                value >= bad -> Color.RED
                value >= warning -> Color.YELLOW
                else -> Color.GREEN
            }
        }

        private fun percentColor(
            value: Double,
            warning: Double,
            bad: Double,
        ): Int {
            return when {
                value >= bad -> Color.RED
                value >= warning -> Color.YELLOW
                else -> Color.GREEN
            }
        }

        private fun countColor(
            value: Int,
            warning: Int,
            bad: Int,
        ): Int {
            return when {
                value >= bad -> Color.RED
                value >= warning -> Color.YELLOW
                else -> Color.GREEN
            }
        }

        private fun FrameMetrics.metric(
            metric: Int,
        ): Long {
            return getMetric(metric)
                .takeIf { it >= 0 }
                ?: 0L
        }
    }
}