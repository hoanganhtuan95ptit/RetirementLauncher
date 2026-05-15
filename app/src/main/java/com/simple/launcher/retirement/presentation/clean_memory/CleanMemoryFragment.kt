package com.simple.launcher.retirement.presentation.clean_memory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.databinding.FragmentCleanMemoryBinding
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import com.simple.launcher.retirement.utils.lifecycle.observe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleanMemoryFragment : BaseFragment<FragmentCleanMemoryBinding>() {

    private val viewModel: CleanMemoryViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCleanMemoryBinding {
        return FragmentCleanMemoryBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.toolbar.ivLeft.setOnSafeClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnClean.root.setOnSafeClickListener {
            binding.btnClean.root.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            binding.tvStatus.setText(getString(R.string.clean_memory_running).toRich())

            lifecycleScope.launch {
                val freedBytes = withContext(Dispatchers.IO) {
                    val result = AppRepository.instance.cleanMemory()
                    delay(1500)
                    result
                }

                val freedMB = freedBytes / (1024 * 1024)
                binding.progressBar.visibility = View.GONE

                if (freedMB > 0) {
                    binding.tvStatus.setText(getString(R.string.clean_memory_completed, freedMB).toRich())
                } else {
                    binding.tvStatus.setText(getString(R.string.clean_memory_optimal).toRich())
                }

                binding.btnClean.root.isEnabled = true
                viewModel.setActionState(R.string.clean_memory_retry)

                Toast.makeText(requireContext(), getString(R.string.clean_memory_toast, freedMB), Toast.LENGTH_SHORT).show()
            }
        }
    }

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
    }
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
