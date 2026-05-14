package com.simple.launcher.retirement.presentation.clean_files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.databinding.FragmentCleanFilesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleanFilesFragment : Fragment() {

    private var _binding: FragmentCleanFilesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCleanFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnClean.setOnClickListener {
            binding.btnClean.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            binding.tvStatus.text = getString(R.string.clean_files_running)

            lifecycleScope.launch {
                val repository = AppRepository.instance
                withContext(Dispatchers.IO) {
                    repository.deleteStrangeFiles()
                }
                
                binding.progressBar.visibility = View.GONE
                binding.tvStatus.text = getString(R.string.clean_files_completed)
                binding.btnClean.isEnabled = true
                binding.btnClean.text = getString(R.string.clean_files_retry)
                
                Toast.makeText(requireContext(), R.string.clean_files_toast, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
