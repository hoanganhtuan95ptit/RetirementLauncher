package com.simple.launcher.retirement.presentation.clean_memory

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
import com.simple.launcher.retirement.databinding.FragmentCleanMemoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.text.toRich

class CleanMemoryFragment : Fragment() {

    private var _binding: FragmentCleanMemoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCleanMemoryBinding.inflate(inflater, container, false)
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
            binding.tvStatus.setText(getString(R.string.clean_memory_running).toRich())

            lifecycleScope.launch {
                val repository = AppRepository.instance
                val freedBytes = withContext(Dispatchers.IO) {
                    val result = repository.cleanMemory()
                    delay(1500) // Tạo hiệu ứng quét cho người dùng
                    result
                }
                
                val freedMB = freedBytes / (1024 * 1024)
                binding.progressBar.visibility = View.GONE
                
                if (freedMB > 0) {
                    binding.tvStatus.setText(getString(R.string.clean_memory_completed, freedMB).toRich())
                } else {
                    binding.tvStatus.setText(getString(R.string.clean_memory_optimal).toRich())
                }
                
                binding.btnClean.isEnabled = true
                binding.btnClean.setText(getString(R.string.clean_memory_retry).toRich())
                
                Toast.makeText(requireContext(), getString(R.string.clean_memory_toast, freedMB), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
