package com.simple.launcher.retirement.presentation.clean_memory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleanMemoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clean_memory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val btnClean = view.findViewById<Button>(R.id.btnClean)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        btnClean.setOnClickListener {
            btnClean.isEnabled = false
            progressBar.visibility = View.VISIBLE
            tvStatus.text = "Đang tối ưu hệ thống..."

            lifecycleScope.launch {
                val repository = AppRepositoryImpl(requireContext())
                val freedBytes = withContext(Dispatchers.IO) {
                    val result = repository.cleanMemory()
                    delay(1500) // Tạo hiệu ứng quét cho người dùng
                    result
                }
                
                val freedMB = freedBytes / (1024 * 1024)
                progressBar.visibility = View.GONE
                
                if (freedMB > 0) {
                    tvStatus.text = "Hệ thống đã được tối ưu!\nĐã giải phóng khoảng $freedMB MB RAM"
                } else {
                    tvStatus.text = "Hệ thống đang ở trạng thái tối ưu nhất!"
                }
                
                btnClean.isEnabled = true
                btnClean.text = "Tối ưu lại"
                
                Toast.makeText(requireContext(), "Đã giải phóng $freedMB MB RAM", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
