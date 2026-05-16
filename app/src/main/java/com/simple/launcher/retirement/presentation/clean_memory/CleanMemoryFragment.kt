package com.simple.launcher.retirement.presentation.clean_memory

import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentCleanMemoryBinding
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleanMemoryFragment : BaseFragment<FragmentCleanMemoryBinding>() {

    private val viewModel: CleanMemoryViewModel by viewModels()

    // Màu cho các chip app (xoay vòng)
    private val chipColors = listOf(
        Color.parseColor("#E24B4A"),
        Color.parseColor("#378ADD"),
        Color.parseColor("#1D9E75"),
        Color.parseColor("#BA7517"),
        Color.parseColor("#D4537E"),
        Color.parseColor("#7F77DD"),
        Color.parseColor("#E09B3D"),
        Color.parseColor("#4AABD4")
    )

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCleanMemoryBinding =
        FragmentCleanMemoryBinding.inflate(inflater, container, false)

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.toolbar.ivLeft.setOnSafeClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Load RAM info + running apps
        loadRamInfo()
        loadRunningApps()

        binding.btnBoost.setOnSafeClickListener {
            startBoosting()
        }
    }

    // ── RAM info ─────────────────────────────────────────────────────────

    private fun loadRamInfo() {
        val am = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)

        val totalMB = memInfo.totalMem / (1024 * 1024)
        val availMB = memInfo.availMem / (1024 * 1024)
        val usedMB = totalMB - availMB
        val percent = usedMB.toFloat() / totalMB.toFloat()

        val totalGB = "%.1f GB".format(totalMB / 1024f)
        val usedGB = "%.1f GB".format(usedMB / 1024f)
        val freeGB = "%.1f GB".format(availMB / 1024f)

        binding.memoryGauge.setPercent(percent, animate = false)
        binding.tvPercent.text = "${(percent * 100).toInt()}%"
        binding.tvRamDetail.text = "$usedGB / $totalGB"
        binding.tvStatUsed.text = usedGB
        binding.tvStatFree.text = freeGB
        binding.tvStatTotal.text = totalGB
    }

    // ── Running apps chips ────────────────────────────────────────────────

    private fun loadRunningApps() {
        val am = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pm = requireContext().packageManager

        @Suppress("DEPRECATION")
        val processes = am.runningAppProcesses
            ?.filter { it.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE }
            ?.take(9)
            ?: emptyList()

        binding.llAppsContainer.removeAllViews()

        if (processes.isEmpty()) {
            binding.tvAppsTitle.visibility = View.GONE
            return
        }

        binding.tvAppsTitle.visibility = View.VISIBLE

        val chunked = processes.chunked(3)
        chunked.forEachIndexed { rowIndex, row ->
            val rowLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dpToPx(6) }
                weightSum = 3f
            }

            row.forEachIndexed { colIndex, process ->
                val globalIndex = rowIndex * 3 + colIndex
                val appName = try {
                    pm.getApplicationLabel(
                        pm.getApplicationInfo(process.processName, 0)
                    ).toString()
                } catch (e: Exception) {
                    process.processName.substringAfterLast('.')
                }

                val chipColor = chipColors[globalIndex % chipColors.size]
                val chip = buildChip(appName, chipColor)
                val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                if (colIndex > 0) lp.marginStart = dpToPx(6)
                chip.layoutParams = lp
                rowLayout.addView(chip)
            }

            // Nếu hàng chưa đủ 3 chips, thêm spacer
            repeat(3 - row.size) {
                val spacer = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
                }
                rowLayout.addView(spacer)
            }

            binding.llAppsContainer.addView(rowLayout)
        }
    }

    private fun buildChip(name: String, dotColor: Int): LinearLayout {
        val ctx = requireContext()
        val chipBg = ContextCompat.getDrawable(ctx, R.drawable.bg_memory_chip)

        return LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = chipBg
            setPadding(dpToPx(6), dpToPx(5), dpToPx(8), dpToPx(5))

            // Dot
            addView(View(ctx).apply {
                val size = dpToPx(12)
                layoutParams = LinearLayout.LayoutParams(size, size).also {
                    it.marginEnd = dpToPx(5)
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(dotColor)
                    cornerRadius = size / 2f
                }
            })

            // Label
            addView(TextView(ctx).apply {
                text = name
                setTextColor(Color.parseColor("#555555"))
                textSize = 10f
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })
        }
    }

    // ── Boost logic ───────────────────────────────────────────────────────

    private fun startBoosting() {
        binding.btnBoost.isEnabled = false
        binding.btnBoost.text = getString(R.string.clean_memory_running)
        binding.btnBoost.setBackgroundResource(R.drawable.bg_memory_btn_running)
        binding.btnBoost.setTextColor(ContextCompat.getColor(requireContext(), R.color.memory_btn_running_text))
        binding.tvStatus.text = getString(R.string.clean_memory_running)
        binding.cardResult.visibility = View.GONE

        // Animate gauge spinning → crossing out chips
        strikeChips()

        lifecycleScope.launch {
            val freedBytes = withContext(Dispatchers.IO) {
                val result = AppRepository.instance.cleanMemory()
                delay(2000)
                result
            }

            val freedMB = freedBytes / (1024 * 1024)

            // Refresh RAM info
            loadRamInfo()
            // Re-animate gauge to new value
            val am = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memInfo)
            val pct = (memInfo.totalMem - memInfo.availMem).toFloat() / memInfo.totalMem.toFloat()
            binding.memoryGauge.setPercent(pct, animate = true)

            // Show result card
            binding.cardResult.visibility = View.VISIBLE
            if (freedMB > 0) {
                binding.tvResultTitle.text = "Đã giải phóng $freedMB MB RAM"
                binding.tvStatus.text = ""
            } else {
                binding.tvResultTitle.text = getString(R.string.clean_memory_optimal)
                binding.tvStatus.text = ""
            }

            // Update button
            binding.btnBoost.setBackgroundResource(R.drawable.bg_memory_btn_done)
            binding.btnBoost.setTextColor(Color.WHITE)
            binding.btnBoost.text = getString(R.string.clean_memory_retry)
            binding.btnBoost.isEnabled = true

            Toast.makeText(
                requireContext(),
                getString(R.string.clean_memory_toast, freedMB),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /** Gạch ngang các chip app khi đang boost */
    private fun strikeChips() {
        for (i in 0 until binding.llAppsContainer.childCount) {
            val row = binding.llAppsContainer.getChildAt(i) as? LinearLayout ?: continue
            for (j in 0 until row.childCount) {
                val chip = row.getChildAt(j) as? LinearLayout ?: continue
                val label = chip.getChildAt(1) as? TextView ?: continue
                label.paintFlags = label.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                label.setTextColor(Color.parseColor("#BBBBBB"))
            }
        }
    }

    // ── ViewModel observers ───────────────────────────────────────────────

    override fun observeData() {
        super.observeData()

        // Toolbar dùng màu trắng vì background tối
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
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}

@Deeplink
class CleanMemoryDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = "app://clean_memory"

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {
        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CleanMemoryFragment())

        if (extras?.get("addToBackStack") == true) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
        return true
    }
}
