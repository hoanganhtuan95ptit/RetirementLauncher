package com.simple.launcher.retirement.presentation.clean_files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentCleanFilesBinding
import com.simple.launcher.retirement.databinding.ItemCleanCategoryBinding
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

class CleanFilesFragment : BaseFragment<FragmentCleanFilesBinding>() {

    private val viewModel: CleanFilesViewModel by viewModels()

    // 4 category rows – được bind theo đúng thứ tự StrangeFileCategory.values()
    private val categoryBindings = mutableListOf<ItemCleanCategoryBinding>()

    // ─── Binding inflation ──────────────────────────────────────────────────────

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCleanFilesBinding = FragmentCleanFilesBinding.inflate(inflater, container, false)

    // ─── View setup ─────────────────────────────────────────────────────────────

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.toolbar.ivLeft.setOnSafeClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        inflateCategoryRows()

        binding.btnClean.root.setOnSafeClickListener {
            when (viewModel.screenState.value) {
                CleanScreenState.IDLE, CleanScreenState.DONE -> viewModel.startScan()
                CleanScreenState.SCANNING -> { /* no-op while scanning */ }
            }
        }
    }

    // ─── Category rows ───────────────────────────────────────────────────────────

    private fun inflateCategoryRows() {
        val inflater = LayoutInflater.from(requireContext())
        viewModel.categoryMeta.forEachIndexed { index, meta ->
            val itemBinding = ItemCleanCategoryBinding.inflate(inflater, binding.llCategories, true)
            itemBinding.ivCatIcon.setBackgroundResource(meta.iconBgRes)
            itemBinding.ivCatIcon.setImageResource(meta.iconRes)
            itemBinding.ivCatIcon.imageTintList =
                ContextCompat.getColorStateList(requireContext(), meta.iconTintRes)
            itemBinding.tvCatName.text = getString(meta.labelRes)
            itemBinding.tvCatCount.visibility = View.INVISIBLE
            itemBinding.ivCatCheck.visibility = View.INVISIBLE
            categoryBindings.add(itemBinding)
        }
    }

    private fun resetCategoryRows() {
        categoryBindings.forEach { b ->
            b.tvCatCount.visibility = View.INVISIBLE
            b.ivCatCheck.visibility = View.INVISIBLE
        }
    }

    private fun markCategoryDone(index: Int, count: Int) {
        val b = categoryBindings.getOrNull(index) ?: return
        if (b.ivCatCheck.visibility == View.VISIBLE) return // đã đánh dấu rồi, bỏ qua
        b.tvCatCount.text = getString(R.string.clean_cat_file_count, count)
        b.tvCatCount.visibility = View.VISIBLE
        b.ivCatCheck.visibility = View.VISIBLE
        val anim = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)
        b.ivCatCheck.startAnimation(anim)
    }

    // ─── Ring center helpers ─────────────────────────────────────────────────────

    private fun showRingIcon() {
        binding.ivRingIcon.visibility = View.VISIBLE
        binding.tvRingCount.visibility = View.GONE
        binding.tvRingUnit.visibility = View.GONE
    }

    private fun showRingResult(fileCount: Int) {
        binding.ivRingIcon.visibility = View.GONE
        binding.tvRingCount.visibility = View.VISIBLE
        binding.tvRingUnit.visibility = View.VISIBLE
        binding.tvRingCount.text = fileCount.toString()
        binding.tvRingUnit.text = getString(R.string.clean_result_files_deleted)
    }

    // ─── Observe ─────────────────────────────────────────────────────────────────

    override fun observeData() {
        super.observeData()

        viewModel.background.observe(this) { background ->
            binding.root.setBackground(background)
        }

        viewModel.toolbar.observe(this) { state ->
            binding.toolbar.tvTitle.setText(state.title)
            val backIcon = state.backIcon
            if (backIcon != null) {
                binding.toolbar.ivLeft.visibility = View.VISIBLE
                binding.toolbar.ivLeft.setImage(backIcon)
            } else {
                binding.toolbar.ivLeft.visibility = View.GONE
            }
        }

        viewModel.action.observe(this) { state ->
            binding.btnClean.tvAction.setText(state.text)
            binding.btnClean.tvAction.setBackground(state.background)
        }

        viewModel.screenState.observe(this) { state ->
            when (state) {
                CleanScreenState.IDLE -> {
                    binding.scannerRing.ringState = ScannerRingView.RingState.IDLE
                    binding.tvStatus.text = getString(R.string.clean_files_desc)
                    binding.cardResult.visibility = View.GONE
                    binding.btnClean.root.isEnabled = true
                    showRingIcon()
                    resetCategoryRows()
                }
                CleanScreenState.SCANNING -> {
                    binding.scannerRing.ringState = ScannerRingView.RingState.SCANNING
                    binding.tvStatus.text = getString(R.string.clean_files_running)
                    binding.cardResult.visibility = View.GONE
                    binding.btnClean.root.isEnabled = false
                    showRingIcon()
                    resetCategoryRows()
                }
                CleanScreenState.DONE -> {
                    binding.scannerRing.ringState = ScannerRingView.RingState.DONE
                    binding.tvStatus.text = getString(R.string.clean_files_completed)
                    binding.btnClean.root.isEnabled = true

                    val result = viewModel.result.value
                    if (result != null) {
                        showRingResult(result.totalFiles)
                        binding.tvResultFiles.text = result.totalFiles.toString()
                        binding.tvResultSpace.text = result.spaceLabel

                        binding.cardResult.visibility = View.VISIBLE
                        binding.cardResult.alpha = 0f
                        binding.cardResult.animate().alpha(1f).setDuration(400).start()
                    }

                    Toast.makeText(requireContext(), R.string.clean_files_toast, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.categoryResults.observe(this) { results ->
            results.forEachIndexed { index, pair ->
                if (pair != null) markCategoryDone(index, pair.first)
            }
        }
    }
}

@Deeplink
class CleanFilesDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = DeepLinks.CLEAN_FILES

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {
        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CleanFilesFragment())

        if (extras?.get("addToBackStack") == true) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
        return true
    }
}
