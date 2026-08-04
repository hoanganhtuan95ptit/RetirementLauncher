package com.simple.launcher.retirement.presentation.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.utils.exts.dp

/**
 * Custom view vẽ vòng tròn scanner với 3 trạng thái:
 *   IDLE     – cung tĩnh màu tím nhạt
 *   SCANNING – cung xoay liên tục màu tím đậm
 *   DONE     – cung fill hết vòng màu xanh lá
 */
class ScannerRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ── 1. Fields — Paints & state ────────────────────────────────────────

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {

        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.scanner_ring_track)
    }

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {

        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val oval = RectF()
    private var sweepAngle = IDLE_SWEEP
    private var startAngle = -90f         // -90° = 12 o'clock

    private var spinAnimator: ValueAnimator? = null
    private var fillAnimator: ValueAnimator? = null

    // ------- Public API -------

    var ringState: RingState = RingState.IDLE
        set(value) {

            if (field == value) return
            field = value
            applyState(value)
        }

    // ------- State transitions -------

    private fun applyState(state: RingState) {

        stopAnimations()
        when (state) {

            RingState.IDLE -> {

                arcPaint.color = ContextCompat.getColor(context, R.color.scanner_ring_idle)
                sweepAngle = IDLE_SWEEP
                startAngle = -90f
                invalidate()
            }
            RingState.SCANNING -> {

                arcPaint.color = ContextCompat.getColor(context, R.color.scanner_ring_scanning)
                sweepAngle = SCAN_SWEEP
                startSpinAnimation()
            }
            RingState.DONE -> {

                arcPaint.color = ContextCompat.getColor(context, R.color.scanner_ring_done)
                startAngle = -90f
                startFillAnimation()
            }
        }
    }

    private fun startSpinAnimation() {

        spinAnimator = ValueAnimator.ofFloat(0f, 360f).apply {

            duration = 1400
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {

                startAngle = -90f + it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun startFillAnimation() {

        fillAnimator = ValueAnimator.ofFloat(sweepAngle, 360f).apply {

            duration = 700
            interpolator = LinearInterpolator()
            addUpdateListener {

                sweepAngle = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun stopAnimations() {

        spinAnimator?.cancel(); spinAnimator = null
        fillAnimator?.cancel(); fillAnimator = null
    }

    // ------- Drawing -------

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        val strokeWidth = 20.dp().toFloat()
        trackPaint.strokeWidth = strokeWidth
        arcPaint.strokeWidth = strokeWidth
        val inset = strokeWidth / 2f + 2.dp().toFloat()
        oval.set(inset, inset, w - inset, h - inset)
    }

    override fun onDraw(canvas: Canvas) {

        // Track ring
        canvas.drawArc(oval, -90f, 360f, false, trackPaint)
        // Animated arc
        canvas.drawArc(oval, startAngle, sweepAngle, false, arcPaint)
    }

    override fun onDetachedFromWindow() {

        stopAnimations()
        super.onDetachedFromWindow()
    }

    // ── 5. Nested classes ─────────────────────────────────────────────────

    enum class RingState { IDLE, SCANNING, DONE }

    // ── 6. Companion object ───────────────────────────────────────────────

    companion object {

        private const val IDLE_SWEEP = 290f
        private const val SCAN_SWEEP = 90f
    }
}