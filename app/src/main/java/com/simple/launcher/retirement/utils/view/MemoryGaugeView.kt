package com.simple.launcher.retirement.utils.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
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

    var colorNormal: Int = Color.parseColor("#7F77DD")
    var colorOptimal: Int = Color.parseColor("#1D9E75")
    var colorTrack: Int = Color.parseColor("#EEEDFE")

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

    fun setPercent(percent: Float, animate: Boolean = true) {
        val clamped = percent.coerceIn(0f, 1f)
        if (animate) {
            animator?.cancel()
            animator = ValueAnimator.ofFloat(animatedPercent, clamped).apply {
                duration = 900
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    animatedPercent = it.animatedValue as Float
                    arcPaint.color = lerpColor(colorNormal, colorOptimal, 1f - animatedPercent)
                    invalidate()
                }
                start()
            }
        } else {
            animatedPercent = clamped
            arcPaint.color = lerpColor(colorNormal, colorOptimal, 1f - clamped)
            invalidate()
        }
        currentPercent = clamped
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        strokeWidth = min(w, h) * 0.07f
        trackPaint.strokeWidth = strokeWidth
        arcPaint.strokeWidth = strokeWidth

        val inset = strokeWidth / 2f
        oval.set(inset, inset, w - inset, h - inset)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        trackPaint.color = colorTrack
        // Track: 270° vòng tròn (bắt đầu từ 135°, tức dưới-trái → qua phải → dưới-phải)
        canvas.drawArc(oval, 135f, 270f, false, trackPaint)

        // Arc: phần sử dụng
        val sweep = animatedPercent * 270f
        if (sweep > 0f) {
            canvas.drawArc(oval, 135f, sweep, false, arcPaint)
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
    }
}
