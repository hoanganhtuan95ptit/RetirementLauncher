package com.simple.launcher.retirement.presentation.base

import android.graphics.Color
import android.graphics.Outline
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.size.toPx
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseBottomSheetDialogFragment<VB : ViewBinding, VM : BaseViewModel> : BottomSheetDialogFragment() {

    private var _binding: VB? = null

    protected val binding get() = _binding!!

    protected abstract val viewModel: VM

    private lateinit var anchorView: View

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = inflateBinding(inflater, container)
        _binding?.root?.setBackgroundColor(Color.TRANSPARENT)

        val context = inflater.context
        val root = LinearLayout(context).apply {

            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            anchorView = View(context).apply {

                val width = 40.toPx()
                val height = 4.toPx()
                layoutParams = LinearLayout.LayoutParams(width, height).apply {

                    gravity = Gravity.CENTER_HORIZONTAL
                    topMargin = 12.toPx()
                    bottomMargin = 8.toPx()
                }
                visibility = View.GONE
            }
            addView(anchorView)
            addView(binding.root)
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        setupViews(view, savedInstanceState)
        observeData()
        observeBottomSheetState()
    }

    private fun observeBottomSheetState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.bottomSheet.collectLatest { state ->

                anchorView.visibility = if (state.showAnchor) View.VISIBLE else View.GONE
                anchorView.setBackground(state.anchorBackground)

                (dialog as? BottomSheetDialog)?.let { bottomSheetDialog ->

                    bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { designBottomSheet ->

                        view?.setBackground(state.background)

                        state.background?.let { background ->

                            val isLight = ColorUtils.calculateLuminance(background.backgroundColor) > 0.5
                            bottomSheetDialog.window?.let { window ->

                                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = isLight
                            }
                        }

                        val radius = state.background?.cornerRadius_TL ?: 0
                        if (radius > 0) {

                            designBottomSheet.clipToOutline = true
                            designBottomSheet.outlineProvider = object : ViewOutlineProvider() {

                                override fun getOutline(view: View, outline: Outline) {

                                    outline.setRoundRect(0, 0, view.width, view.height + radius, radius.toFloat())
                                }
                            }
                        } else {

                            designBottomSheet.clipToOutline = false
                            designBottomSheet.outlineProvider = null
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {

        super.onStart()
        (dialog as? BottomSheetDialog)?.let { bottomSheetDialog ->

            val window = bottomSheetDialog.window ?: return@let
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                window.isNavigationBarContrastEnforced = false
            }

            val designBottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            designBottomSheet?.let {

                BottomSheetBehavior.from(it).isGestureInsetBottomIgnored = true
            }

            listOfNotNull(
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.container),
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.coordinator),
                designBottomSheet
            ).forEach { view ->

                view.fitsSystemWindows = false
                view.setBackgroundColor(Color.TRANSPARENT)
                view.setPadding(0, 0, 0, 0)

                ViewCompat.setOnApplyWindowInsetsListener(view) { v, i ->

                    v.setPadding(0, 0, 0, 0)
                    if (v.id == com.google.android.material.R.id.container) {

                        _binding?.root?.let {
                            ViewCompat.dispatchApplyWindowInsets(it, i)
                        }
                    }
                    WindowInsetsCompat.CONSUMED
                }
            }
        }
    }

    open fun setupViews(view: View, savedInstanceState: Bundle?) {}

    open fun observeData() {}

    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
    }
}
