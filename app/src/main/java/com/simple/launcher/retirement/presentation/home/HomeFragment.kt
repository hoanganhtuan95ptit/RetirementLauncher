package com.simple.launcher.retirement.presentation.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simple.adapter.ViewItemAdapterProvider
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.auto.register.AutoRegisterManager
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.data.repository.AppRepositoryImpl
import com.simple.launcher.retirement.databinding.FragmentHomeBinding
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase
import com.simple.launcher.retirement.presentation.home.adapter.AppHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.CleanFilesHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.CleanMemoryHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.ClockHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.ContactHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.HeaderHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.HomeEventBus
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.launcher.retirement.presentation.main.MainActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
        val layoutManager = GridLayoutManager(requireContext(), 6)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val adapter = binding.rvApps.adapter as? com.simple.adapter.MultiAdapter
                val item = adapter?.currentList?.getOrNull(position)
                return when (item) {
                    is ClockHomeItem -> 6
                    is HeaderHomeItem -> 6
                    is CleanFilesHomeItem -> 3
                    is CleanMemoryHomeItem -> 3
                    is AppHomeItem -> 2
                    is ContactHomeItem -> 3
                    else -> 2
                }
            }
        }
        binding.rvApps.layoutManager = layoutManager
        
        // Thêm khoảng cách giữa các item
        binding.rvApps.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val spacing = 12 // Khoảng cách
                outRect.set(spacing, spacing, spacing, spacing)
            }
        })

        binding.btnSettings.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            sendDeeplink("app://settings", extras = mapOf("addToBackStack" to true))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.items.asFlow().attachAdapter().collectLatest { (items, adapters) ->
                Log.d("tuanha", "onViewCreated: $adapters")
                binding.rvApps.submitListAndAwait(items, adapters, true)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            HomeEventBus.events.collectLatest { item ->
                handleHomeItemClick(item)
            }
        }

        viewModel.loadApps()
    }

    private fun handleHomeItemClick(item: HomeItem) {
        when (item) {
            is AppHomeItem -> {
                if (item.entity.packageName == requireContext().packageName) {
                    sendDeeplink("app://settings", extras = mapOf("addToBackStack" to true))
                } else {
                    val launchIntent =
                        requireContext().packageManager.getLaunchIntentForPackage(item.entity.packageName)
                    if (launchIntent != null) {
                        startActivity(launchIntent)
                    }
                }
            }

            is ContactHomeItem -> {
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

            is CleanFilesHomeItem -> {
                sendDeeplink("app://clean_files", extras = mapOf("addToBackStack" to true))
            }

            is CleanMemoryHomeItem -> {
                sendDeeplink("app://clean_memory", extras = mapOf("addToBackStack" to true))
            }

            is ClockHomeItem -> {
                // Do nothing
            }
        }
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

@Deeplink
class HomeDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = "app://home"

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {
        if (fragmentActivity is MainActivity) {
            val fragmentManager = fragmentActivity.supportFragmentManager
            val currentFragment = fragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment !is HomeFragment) {
                // Xóa backstack và chuyển về HomeFragment
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
            return true
        }
        return false
    }
}
