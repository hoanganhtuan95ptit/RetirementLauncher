package com.simple.launcher.retirement.utils.background

import android.graphics.Color

class BackgroundBuilder {

    private var backgroundColor: Int = Color.TRANSPARENT

    private var cornerRadius_TL: Int = 0
    private var cornerRadius_TR: Int = 0
    private var cornerRadius_BL: Int = 0
    private var cornerRadius_BR: Int = 0

    private var strokeWidth: Int = 0
    private var strokeColor: Int = Color.TRANSPARENT
    private var strokeDashGap: Int = 0
    private var strokeDashWidth: Int = 0

    // ── Background color ────────────────────────────────────────────────

    fun backgroundColor(color: Int): BackgroundBuilder {
        this.backgroundColor = color
        return this
    }

    // ── Corner radius ───────────────────────────────────────────────────

    /** Bo tròn tất cả 4 góc cùng giá trị. */
    fun cornerRadius(radius: Int): BackgroundBuilder {
        this.cornerRadius_TL = radius
        this.cornerRadius_TR = radius
        this.cornerRadius_BL = radius
        this.cornerRadius_BR = radius
        return this
    }

    /** Bo tròn 2 góc trên. */
    fun cornerRadiusTop(radius: Int): BackgroundBuilder {
        this.cornerRadius_TL = radius
        this.cornerRadius_TR = radius
        return this
    }

    /** Bo tròn 2 góc dưới. */
    fun cornerRadiusBottom(radius: Int): BackgroundBuilder {
        this.cornerRadius_BL = radius
        this.cornerRadius_BR = radius
        return this
    }

    /** Bo tròn từng góc riêng lẻ. */
    fun cornerRadius(
        topLeft: Int = 0,
        topRight: Int = 0,
        bottomLeft: Int = 0,
        bottomRight: Int = 0
    ): BackgroundBuilder {
        this.cornerRadius_TL = topLeft
        this.cornerRadius_TR = topRight
        this.cornerRadius_BL = bottomLeft
        this.cornerRadius_BR = bottomRight
        return this
    }

    // ── Stroke ──────────────────────────────────────────────────────────

    fun stroke(
        width: Int,
        color: Int,
        dashWidth: Int = 0,
        dashGap: Int = 0
    ): BackgroundBuilder {
        this.strokeWidth = width
        this.strokeColor = color
        this.strokeDashWidth = dashWidth
        this.strokeDashGap = dashGap
        return this
    }

    // ── Build ───────────────────────────────────────────────────────────

    fun build(): Background {

        return Background(
            backgroundColor = backgroundColor,
            cornerRadius_TL = cornerRadius_TL,
            cornerRadius_TR = cornerRadius_TR,
            cornerRadius_BL = cornerRadius_BL,
            cornerRadius_BR = cornerRadius_BR,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            strokeDashGap = strokeDashGap,
            strokeDashWidth = strokeDashWidth
        )
    }
}
