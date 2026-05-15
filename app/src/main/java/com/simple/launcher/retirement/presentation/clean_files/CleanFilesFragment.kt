package com.simple.launcher.retirement.presentation.clean_files

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
import com.simple.launcher.retirement.databinding.FragmentCleanFilesBinding
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleanFilesFragment : BaseFragment<FragmentCleanFilesBinding>() {

    private val viewModel: CleanFilesViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCleanFilesBinding {
        return FragmentCleanFilesBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        binding.toolbar.ivLeft.setOnSafeClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnClean.root.setOnSafeClickListener {
            binding.btnClean.root.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            binding.tvStatus.setText(getString(R.string.clean_files_running).toRich())

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    AppRepository.instance.deleteStrangeFiles()
                }

                binding.progressBar.visibility = View.GONE
                binding.tvStatus.setText(getString(R.string.clean_files_completed).toRich())
                binding.btnClean.root.isEnabled = true
                viewModel.setActionState(R.string.clean_files_retry)

                Toast.makeText(requireContext(), R.string.clean_files_toast, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun observeData() {
        super.observeData()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.toolbar.collectLatest { state ->
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.action.collectLatest { state ->
                binding.btnClean.tvAction.setText(state.text)
                binding.btnClean.tvAction.setBackground(state.background)
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
