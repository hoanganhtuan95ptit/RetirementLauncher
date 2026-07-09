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
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.size.toPx
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseBottomSheetDialogFragment<VB : ViewBinding, VM : BaseViewModel> : BottomSheetDialogFragment() {

    var binding: VB? = null

    protected abstract val viewModel: VM

    private lateinit var anchorView: View

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = inflateBinding(inflater, container)
        binding?.root?.setBackgroundColor(Color.TRANSPARENT)

        return createRootContainer(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        setupViews(view, savedInstanceState)
        observeData()
        observeBottomSheetState()
    }

    private fun createRootContainer(inflater: LayoutInflater): View {

        val context = inflater.context
        return LinearLayout(context).apply {

            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            anchorView = createAnchorView(context)
            addView(anchorView)
            binding?.root?.let(::addView)
        }
    }

    private fun createAnchorView(context: android.content.Context): View {

        return View(context).apply {

            val width = 40.toPx()
            val height = 4.toPx()
            layoutParams = LinearLayout.LayoutParams(width, height).apply {

                gravity = Gravity.CENTER_HORIZONTAL
                topMargin = 12.toPx()
                bottomMargin = 8.toPx()
            }
            visibility = View.GONE
        }
    }

    private fun observeBottomSheetState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.bottomSheet.collectLatest { state ->

                updateAnchor(state.showAnchor, state.anchorBackground)
                updateDialogState(state.background)
            }
        }
    }

    private fun updateAnchor(
        showAnchor: Boolean,
        anchorBackground: Background?
    ) {

        anchorView.visibility = if (showAnchor) View.VISIBLE else View.GONE
        anchorView.setBackground(anchorBackground)
    }

    private fun updateDialogState(background: Background?) {

        val bottomSheetDialog = dialog as? BottomSheetDialog ?: return
        val designBottomSheet = findDesignBottomSheet(bottomSheetDialog) ?: return

        view?.setBackground(background)
        updateNavigationBarAppearance(bottomSheetDialog, background)
        updateBottomSheetOutline(designBottomSheet, background)
    }

    private fun updateNavigationBarAppearance(
        bottomSheetDialog: BottomSheetDialog,
        background: Background?
    ) {

        val backgroundColor = background?.backgroundColor ?: return
        val window = bottomSheetDialog.window ?: return
        val isLightNavigationBar = ColorUtils.calculateLuminance(backgroundColor) > 0.5

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = isLightNavigationBar
    }

    private fun updateBottomSheetOutline(
        designBottomSheet: View,
        background: Background?
    ) {

        val radius = background?.cornerRadius_TL ?: 0
        if (radius <= 0) {

            designBottomSheet.clipToOutline = false
            designBottomSheet.outlineProvider = null
            return
        }

        designBottomSheet.clipToOutline = true
        designBottomSheet.outlineProvider = createOutlineProvider(radius)
    }

    private fun createOutlineProvider(radius: Int): ViewOutlineProvider {

        return object : ViewOutlineProvider() {

            override fun getOutline(view: View, outline: Outline) {

                outline.setRoundRect(0, 0, view.width, view.height + radius, radius.toFloat())
            }
        }
    }

    override fun onStart() {

        super.onStart()

        val bottomSheetDialog = dialog as? BottomSheetDialog ?: return
        configureWindow(bottomSheetDialog)
        configureGestureInset(bottomSheetDialog)
        configureInsets(bottomSheetDialog)
    }

    private fun configureWindow(bottomSheetDialog: BottomSheetDialog) {

        val window = bottomSheetDialog.window ?: return
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            window.isNavigationBarContrastEnforced = false
        }
    }

    private fun configureGestureInset(bottomSheetDialog: BottomSheetDialog) {

        val designBottomSheet = findDesignBottomSheet(bottomSheetDialog) ?: return
        BottomSheetBehavior.from(designBottomSheet).isGestureInsetBottomIgnored = true
    }

    private fun configureInsets(bottomSheetDialog: BottomSheetDialog) {

        buildInsetTargets(bottomSheetDialog).forEach { targetView ->

            prepareInsetTarget(targetView)
        }
    }

    private fun buildInsetTargets(bottomSheetDialog: BottomSheetDialog): List<View> {

        return listOfNotNull(
            bottomSheetDialog.findViewById(com.google.android.material.R.id.container),
            bottomSheetDialog.findViewById(com.google.android.material.R.id.coordinator),
            findDesignBottomSheet(bottomSheetDialog)
        )
    }

    private fun prepareInsetTarget(targetView: View) {

        targetView.fitsSystemWindows = false
        targetView.setBackgroundColor(Color.TRANSPARENT)
        targetView.setPadding(0, 0, 0, 0)

        ViewCompat.setOnApplyWindowInsetsListener(targetView) { view, insets ->

            view.setPadding(0, 0, 0, 0)
            dispatchInsetsToContentIfNeeded(view, insets)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun dispatchInsetsToContentIfNeeded(
        targetView: View,
        insets: WindowInsetsCompat
    ) {

        if (targetView.id != com.google.android.material.R.id.container) return

        binding?.root?.let { contentView ->
            ViewCompat.dispatchApplyWindowInsets(contentView, insets)
        }
    }

    private fun findDesignBottomSheet(bottomSheetDialog: BottomSheetDialog): View? {

        return bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
    }

    open fun setupViews(view: View, savedInstanceState: Bundle?) {
    }

    open fun observeData() {
    }

    override fun onDestroyView() {

        super.onDestroyView()
        binding = null
    }
}
