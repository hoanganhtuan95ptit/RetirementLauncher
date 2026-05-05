package com.simple.launcher.retirement.presentation.clean_memory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.databinding.FragmentCleanMemoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            binding.tvStatus.text = getString(R.string.clean_memory_running)

            lifecycleScope.launch {
                val repository = AppRepositoryImpl(requireContext())
                val freedBytes = withContext(Dispatchers.IO) {
                    val result = repository.cleanMemory()
                    delay(1500) // Tạo hiệu ứng quét cho người dùng
                    result
                }
                
                val freedMB = freedBytes / (1024 * 1024)
                binding.progressBar.visibility = View.GONE
                
                if (freedMB > 0) {
                    binding.tvStatus.text = getString(R.string.clean_memory_completed, freedMB)
                } else {
                    binding.tvStatus.text = getString(R.string.clean_memory_optimal)
                }
                
                binding.btnClean.isEnabled = true
                binding.btnClean.text = getString(R.string.clean_memory_retry)
                
                Toast.makeText(requireContext(), getString(R.string.clean_memory_toast, freedMB), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
