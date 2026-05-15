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
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simple.adapter.MultiAdapter
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentHomeBinding
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase
import com.simple.launcher.retirement.presentation.home.adapter.AppHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.CleanFilesHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.CleanMemoryHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.ContactHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.HomeEventBus
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.launcher.retirement.presentation.main.MainActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.presentation.home.adapter.ClockHomeItem
import androidx.core.net.toUri

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by activityViewModels {
        HomeViewModelFactory(GetHomeAppsUseCase.instance, AppRepository.instance)
    }

    // Flag để tránh crash khi unregisterReceiver gọi trước registerReceiver
    private var isReceiverRegistered = false

    private val fileChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.loadSystemStatus()
        }
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        // Setup LayoutManager
        val layoutManager = GridLayoutManager(requireContext(), HomeItem.TOTAL_COLUMNS)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return ((binding.rvApps.adapter as? MultiAdapter)?.currentList?.getOrNull(position) as? HomeItem)?.spanSize ?: 2
            }
        }
        binding.rvApps.layoutManager = layoutManager

        // Thêm khoảng cách giữa các item (dùng dp thay vì pixel cứng)
        val spacingDp = (12 * resources.displayMetrics.density).toInt()
        binding.rvApps.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(spacingDp, spacingDp, spacingDp, spacingDp)
            }
        })
    }

    override fun observeData() {
        super.observeData()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.items.attachAdapter().collectLatest { (items, adapters) ->
                binding.rvApps.submitListAndAwait(items, adapters, true)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            HomeEventBus.events.collectLatest { item ->
                handleHomeItemClick(item)
            }
        }
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
                callIntent.data = "tel:${item.entity.phoneNumber}".toUri()
                try {
                    startActivity(callIntent)
                } catch (e: Exception) {
                    val dialIntent = Intent(Intent.ACTION_DIAL)
                    dialIntent.data = "tel:${item.entity.phoneNumber}".toUri()
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
        isReceiverRegistered = true
    }

    override fun onPause() {
        super.onPause()
        if (isReceiverRegistered) {
            requireContext().unregisterReceiver(fileChangeReceiver)
            isReceiverRegistered = false
        }
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
