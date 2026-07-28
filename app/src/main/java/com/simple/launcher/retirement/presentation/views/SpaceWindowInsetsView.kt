package com.simple.launcher.retirement.presentation.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.simple.launcher.retirement.R

class SpaceWindowInsetsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context, attrs, defStyleAttr
) {

    private var applyStatusBars = false
    private var applyNavigationBars = false

    init {

        context.withStyledAttributes(attrs, R.styleable.SpaceWindowInsetsView) {
            applyStatusBars = getBoolean(R.styleable.SpaceWindowInsetsView_applyStatusBars, false)
            applyNavigationBars = getBoolean(R.styleable.SpaceWindowInsetsView_applyNavigationBars, false)
        }

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->

            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            var height = 0
            if (applyStatusBars) {
                height += statusBars.top
            }
            if (applyNavigationBars) {
                height += navigationBars.bottom
            }

            updateLayoutParams {
                this.height = height
            }

            insets
        }
    }
}
