package com.simple.launcher.retirement.presentation.clean_files

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleanFilesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clean_files, container, false)
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
            tvStatus.text = getString(R.string.clean_files_running)

            lifecycleScope.launch {
                val repository = AppRepositoryImpl(requireContext())
                withContext(Dispatchers.IO) {
                    repository.deleteStrangeFiles()
                }
                
                progressBar.visibility = View.GONE
                tvStatus.text = getString(R.string.clean_files_completed)
                btnClean.isEnabled = true
                btnClean.text = getString(R.string.clean_files_retry)
                
                Toast.makeText(requireContext(), R.string.clean_files_toast, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
