package com.simple.launcher.retirement.presentation.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.simple.launcher.retirement.R

class WindowInsetsFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var insetStatusBars = false
    private var insetNavigationBars = false

    init {

        context.withStyledAttributes(attrs, R.styleable.WindowInsetsFrameLayout) {

            insetStatusBars = getBoolean(R.styleable.WindowInsetsFrameLayout_insetStatusBars, false)
            insetNavigationBars = getBoolean(R.styleable.WindowInsetsFrameLayout_insetNavigationBars, false)
        }

        setupInsets()
    }

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            val paddingTop = if (insetStatusBars) statusBars.top else 0
            val paddingBottom = if (insetNavigationBars) navigationBars.bottom else 0

            view.updatePadding(
                top = paddingTop,
                bottom = paddingBottom
            )

            insets
        }
    }
}
