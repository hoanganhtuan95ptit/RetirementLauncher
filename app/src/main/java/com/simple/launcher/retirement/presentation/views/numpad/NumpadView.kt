package com.simple.launcher.retirement.presentation.views.numpad

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.simple.launcher.retirement.databinding.LayoutNumpadBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NumpadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutNumpadBinding = LayoutNumpadBinding.inflate(LayoutInflater.from(context), this)

    /** Callback khi người dùng nhấn phím số — giá trị là [NumpadKeyItem.value] */
    var onDigitClick: ((String) -> Unit)? = null

    /** Callback khi người dùng nhấn phím xoá */
    var onDeleteClick: (() -> Unit)? = null

    private var observeJob: Job? = null

    /**
     * Thứ tự các nút khớp với [NumpadState.keys]:
     * 1, 2, 3, 4, 5, 6, 7, 8, 9, 0
     */
    private val orderedButtons by lazy {
        listOf(
            binding.btnKey1, binding.btnKey2, binding.btnKey3,
            binding.btnKey4, binding.btnKey5, binding.btnKey6,
            binding.btnKey7, binding.btnKey8, binding.btnKey9,
            binding.btnKey0
        )
    }

    init {
        orientation = VERTICAL
        binding.btnKeyDelete.setOnClickListener { onDeleteClick?.invoke() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val lifecycleOwner = findViewTreeLifecycleOwner() ?: return
        val vmOwner = findViewTreeViewModelStoreOwner() ?: return
        val viewModel = ViewModelProvider(vmOwner)[NumpadViewModel::class.java]

        observeJob = lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.numpadState.collect { state ->
                    applyState(state)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        observeJob?.cancel()
        observeJob = null
    }

    private fun applyState(state: NumpadViewModel.NumpadState) {
        state.keys.forEachIndexed { index, key ->
            val btn = orderedButtons.getOrNull(index) ?: return@forEachIndexed
            btn.text = key.label
            btn.textSize = state.textSize
            btn.setTextColor(state.textColor)
            btn.backgroundTintList = ColorStateList.valueOf(state.rippleColor)
            btn.setOnClickListener { onDigitClick?.invoke(key.value) }
        }
        binding.btnKeyDelete.imageTintList = ColorStateList.valueOf(state.deleteIconColor)
        binding.btnKeyDelete.backgroundTintList = ColorStateList.valueOf(state.rippleColor)
    }

    fun setIsClickable(isClickable: Boolean) {

        orderedButtons.forEach {
            it.isClickable = isClickable
        }
    }
}
