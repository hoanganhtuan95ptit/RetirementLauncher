package com.simple.launcher.retirement.presentation.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.graphics.toColorInt
import kotlin.math.min

/**
 * Vòng tròn hiển thị % RAM đang sử dụng.
 * Màu arc chuyển từ [colorNormal] (tím) sang [colorOptimal] (xanh lá) khi % thấp.
 */
class MemoryGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var colorNormal: Int = "#7F77DD".toColorInt()
    var colorOptimal: Int = "#1D9E75".toColorInt()
    var colorTrack: Int = "#EEEDFE".toColorInt()

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val oval = RectF()

    /** 0f..1f — phần trăm RAM đang dùng */
    private var currentPercent: Float = 0f
    private var animatedPercent: Float = 0f

    private var strokeWidth: Float = 0f
    private var animator: ValueAnimator? = null

    // ── Spinning (BOOSTING state) ─────────────────────────────────────────────

    private var spinAnimator: ValueAnimator? = null
    private var settleAnimator: ValueAnimator? = null
    private var spinStartAngle: Float = -90f
    private var settleSweep: Float = 0f

    private val isSpinning get() = spinAnimator?.isRunning == true
    private val isSettling get() = settleAnimator?.isRunning == true

    fun startSpinning() {
        if (isSpinning) return
        settleAnimator?.cancel()
        animator?.cancel()
        spinAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 1400
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                spinStartAngle = -90f + it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun setPercent(percent: Float, animate: Boolean = true) {
        val clamped = percent.coerceIn(0f, 1f)
        currentPercent = clamped

        if (isSpinning) {
            // Dừng spin và settle thẳng về target mới — không qua percentAnimator
            spinAnimator?.cancel()
            spinAnimator = null
            animatedPercent = clamped
            arcPaint.color = resolveArcColor(clamped)

            settleAnimator?.cancel()
            settleAnimator = ValueAnimator.ofFloat(SPIN_SWEEP, clamped * 360f).apply {
                duration = 700
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    settleSweep = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
            return
        }

        // Không spinning: update percent bình thường
        settleAnimator?.cancel()
        if (animate) {
            animator?.cancel()
            animator = ValueAnimator.ofFloat(animatedPercent, clamped).apply {
                duration = 900
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    animatedPercent = it.animatedValue as Float
                    arcPaint.color = resolveArcColor(animatedPercent)
                    invalidate()
                }
                start()
            }
        } else {
            animator?.cancel()
            animatedPercent = clamped
            arcPaint.color = resolveArcColor(clamped)
            invalidate()
        }
    }

    /**
     * Giữ màu tím (normal) khi RAM >= 35%.
     * Chỉ chuyển dần sang xanh lá (optimal) khi RAM xuống dưới 35% (sau khi boost).
     */
    private fun resolveArcColor(percent: Float): Int {
        val threshold = 0.35f
        return if (percent >= threshold) {
            colorNormal
        } else {
            val t = 1f - (percent / threshold)   // 0 tại 35%, 1 tại 0%
            lerpColor(colorNormal, colorOptimal, t)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        strokeWidth = min(w, h) * 0.10f   // ~20dp tại 200dp — dày hơn, khớp design
        trackPaint.strokeWidth = strokeWidth
        arcPaint.strokeWidth = strokeWidth

        val inset = strokeWidth / 2f
        oval.set(inset, inset, w - inset, h - inset)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Track: vòng tròn đầy đủ 360°
        trackPaint.color = colorTrack
        canvas.drawArc(oval, -90f, 360f, false, trackPaint)

        when {
            isSpinning -> {
                // Cung 90° cố định, xoay liên tục
                arcPaint.color = colorNormal
                canvas.drawArc(oval, spinStartAngle, SPIN_SWEEP, false, arcPaint)
            }
            isSettling -> {
                // Settle: freeze tại -90°, animate sweep về % đích
                canvas.drawArc(oval, -90f, settleSweep, false, arcPaint)
            }
            else -> {
                // Normal: cung theo % RAM từ đỉnh
                val sweep = animatedPercent * 360f
                if (sweep > 0f) {
                    canvas.drawArc(oval, -90f, sweep, false, arcPaint)
                }
            }
        }
    }

    private fun lerpColor(from: Int, to: Int, t: Float): Int {
        val f = t.coerceIn(0f, 1f)
        val r = (Color.red(from) + (Color.red(to) - Color.red(from)) * f).toInt()
        val g = (Color.green(from) + (Color.green(to) - Color.green(from)) * f).toInt()
        val b = (Color.blue(from) + (Color.blue(to) - Color.blue(from)) * f).toInt()
        return Color.rgb(r, g, b)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        spinAnimator?.cancel()
        settleAnimator?.cancel()
    }

    companion object {
        private const val SPIN_SWEEP = 90f
    }
}
