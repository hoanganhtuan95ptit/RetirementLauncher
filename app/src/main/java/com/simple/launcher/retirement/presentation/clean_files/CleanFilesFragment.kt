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
import androidx.lifecycle.lifecycleScope
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentCleanFilesBinding
import com.simple.launcher.retirement.databinding.ItemCleanCategoryBinding
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.StrangeFileCategory
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleanFilesFragment : BaseFragment<FragmentCleanFilesBinding>() {

    private val viewModel: CleanFilesViewModel by viewModels()

    // 4 category rows – được bind theo đúng thứ tự StrangeFileCategory.values()
    private val categoryBindings = mutableListOf<ItemCleanCategoryBinding>()

    // ─── Category metadata ──────────────────────────────────────────────────
    private data class CategoryMeta(
        val labelRes: Int,
        val iconRes: Int,
        val iconBgRes: Int,
        val iconTintRes: Int
    )

    private val categoryMeta = listOf(
        CategoryMeta(
            R.string.clean_cat_system_temp,
            R.drawable.ic_home_drives_24dp,
            R.drawable.bg_category_icon_purple,
            R.color.clean_cat_purple
        ),
        CategoryMeta(
            R.string.clean_cat_compressed,
            R.drawable.ic_home_drives_24dp,
            R.drawable.bg_category_icon_amber,
            R.color.clean_cat_amber
        ),
        CategoryMeta(
            R.string.clean_cat_no_extension,   // label đã đổi → "Tài liệu (.pdf, .doc…)"
            R.drawable.ic_home_family_24dp,
            R.drawable.bg_category_icon_red,
            R.color.clean_cat_red
        ),
        CategoryMeta(
            R.string.clean_cat_apk_cache,
            R.drawable.ic_home_boost_24dp,
            R.drawable.bg_category_icon_green,
            R.color.clean_cat_green
        )
    )

    // ─── Binding inflation ──────────────────────────────────────────────────

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCleanFilesBinding = FragmentCleanFilesBinding.inflate(inflater, container, false)

    // ─── View setup ─────────────────────────────────────────────────────────

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.toolbar.ivLeft.setOnSafeClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        inflateCategoryRows()

        binding.btnClean.root.setOnSafeClickListener {
            when (viewModel.screenState.value) {
                CleanScreenState.IDLE, CleanScreenState.DONE -> startCleaning()
                CleanScreenState.SCANNING -> { /* no-op while scanning */ }
            }
        }
    }

    // ─── Category rows ──────────────────────────────────────────────────────

    private fun inflateCategoryRows() {
        val inflater = LayoutInflater.from(requireContext())
        categoryMeta.forEachIndexed { index, meta ->
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
        b.tvCatCount.text = "$count file"
        b.tvCatCount.visibility = View.VISIBLE
        b.ivCatCheck.visibility = View.VISIBLE
        // Bounce-in animation for the checkmark
        val anim = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)
        b.ivCatCheck.startAnimation(anim)
    }

    // ─── Ring center helpers ─────────────────────────────────────────────────

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

    // ─── Scan logic ─────────────────────────────────────────────────────────

    private fun startCleaning() {
        // Reset to IDLE state first if retrying
        resetCategoryRows()
        binding.cardResult.visibility = View.GONE
        showRingIcon()
        binding.tvStatus.text = getString(R.string.clean_files_running)

        viewModel.setScreenState(CleanScreenState.SCANNING)
        binding.scannerRing.ringState = ScannerRingView.RingState.SCANNING
        binding.btnClean.root.isEnabled = false

        lifecycleScope.launch {
            val categories = StrangeFileCategory.values()
            var totalFiles = 0
            var totalBytes = 0L

            categories.forEachIndexed { index, category ->
                val (count, bytes) = withContext(Dispatchers.IO) {
                    AppRepository.instance.deleteStrangeFilesByCategory(category)
                }
                totalFiles += count
                totalBytes += bytes
                markCategoryDone(index, count)
                delay(350) // Hiệu ứng dần dần tick từng category
            }

            // Transition to DONE
            binding.scannerRing.ringState = ScannerRingView.RingState.DONE
            viewModel.setResult(totalFiles, totalBytes)
            viewModel.setScreenState(CleanScreenState.DONE)
            binding.btnClean.root.isEnabled = true

            showRingResult(totalFiles)
            binding.tvStatus.text = getString(R.string.clean_files_completed)

            // Show result card with fade-in
            binding.cardResult.visibility = View.VISIBLE
            binding.cardResult.alpha = 0f
            binding.cardResult.animate().alpha(1f).setDuration(400).start()

            val result = viewModel.result.value
            if (result != null) {
                binding.tvResultFiles.text = result.totalFiles.toString()
                binding.tvResultSpace.text = result.spaceLabel
            }

            Toast.makeText(requireContext(), R.string.clean_files_toast, Toast.LENGTH_SHORT).show()
        }
    }

    // ─── Observe ─────────────────────────────────────────────────────────────

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
                    showRingIcon()
                }
                CleanScreenState.SCANNING -> { /* handled in startCleaning() */ }
                CleanScreenState.DONE -> { /* handled in startCleaning() */ }
            }
        }
    }
}

@Deeplink
class CleanFilesDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = "app://clean_files"

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
