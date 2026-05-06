package com.simple.launcher.retirement.presentation.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Rect
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.databinding.FragmentHomeBinding
import com.simple.launcher.retirement.domain.model.HomeItem
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase
import com.simple.launcher.retirement.presentation.clean_files.CleanFilesFragment
import com.simple.launcher.retirement.presentation.clean_memory.CleanMemoryFragment
import com.simple.launcher.retirement.presentation.settings.SettingsFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        val repository = AppRepositoryImpl(requireContext())
        val getHomeAppsUseCase = GetHomeAppsUseCase(repository)
        HomeViewModelFactory(getHomeAppsUseCase, repository)
    }

    private val fileChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.loadSystemStatus()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup LayoutManager
        val layoutManager = GridLayoutManager(requireContext(), 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (binding.rvApps.adapter?.getItemViewType(position) == 0) 2 else 1
            }
        }
        binding.rvApps.layoutManager = layoutManager
        
        // Thêm khoảng cách giữa các item
        binding.rvApps.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val spacing = 24 // Khoảng cách 20px
                outRect.set(spacing, spacing, spacing, spacing)
            }
        })

        binding.btnSettings.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            binding.rvApps.adapter = HomeAdapter(items) { item ->
                when (item) {
                    is HomeItem.App -> {
                        val launchIntent = requireContext().packageManager.getLaunchIntentForPackage(item.entity.packageName)
                        if (launchIntent != null) {
                            startActivity(launchIntent)
                        }
                    }
                    is HomeItem.Contact -> {
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:${item.entity.phoneNumber}")
                        try {
                            startActivity(callIntent)
                        } catch (e: Exception) {
                            val dialIntent = Intent(Intent.ACTION_DIAL)
                            dialIntent.data = Uri.parse("tel:${item.entity.phoneNumber}")
                            startActivity(dialIntent)
                        }
                    }
                    is HomeItem.CleanFiles -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, CleanFilesFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                    is HomeItem.CleanMemory -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, CleanMemoryFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                    is HomeItem.Clock -> {
                        // Do nothing
                    }
                }
            }
        }

        viewModel.loadApps()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSystemStatus()
        
        val filter = IntentFilter("com.simple.launcher.retirement.FILE_CHANGED")
        ContextCompat.registerReceiver(
            requireContext(),
            fileChangeReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(fileChangeReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
