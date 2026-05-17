package com.simple.launcher.retirement.presentation.home

import android.content.Context
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simple.adapter.MultiAdapter
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentHomeBinding
import com.simple.launcher.retirement.domain.repository.FileRepository
import com.simple.launcher.retirement.domain.repository.MemoryRepository
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.launcher.retirement.presentation.main.MainActivity
import com.simple.launcher.retirement.utils.broadcastReceiverFlow
import com.simple.launcher.retirement.utils.lifecycle.observe
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    // UUID chỉ tạo ở debug build — tránh tốn tài nguyên ở production
    val id = if (BuildConfig.DEBUG) UUID.randomUUID().toString() else ""

    private val viewModel: HomeViewModel by activityViewModels {
        HomeViewModelFactory(GetHomeAppsUseCase.instance, FileRepository.instance, MemoryRepository.instance)
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)
        if (BuildConfig.DEBUG) Log.d("tuanha", "setupViews: $id")

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

        viewModel.items.attachAdapter().observe(this) { (items, adapters) ->
            if (BuildConfig.DEBUG) Log.d("tuanha", "observeData: ${items.size} $id $adapters")
            binding.rvApps.submitListAndAwait(items, adapters, true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.loadSystemStatus()
                requireContext().broadcastReceiverFlow(IntentFilter("com.simple.launcher.retirement.FILE_CHANGED"))
                    .collectLatest {
                        viewModel.loadSystemStatus()
                    }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (BuildConfig.DEBUG) Log.d("tuanha", "onAttach: $id")
    }

    override fun onDetach() {
        super.onDetach()
        if (BuildConfig.DEBUG) Log.d("tuanha", "onDetach: $id")
    }

    override fun onResume() {
        super.onResume()
        if (BuildConfig.DEBUG) Log.d("tuanha", "onResume: $id")
    }

    override fun onPause() {
        super.onPause()
        if (BuildConfig.DEBUG) Log.d("tuanha", "onPause: $id")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (BuildConfig.DEBUG) Log.d("tuanha", "onDestroyView: $id")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BuildConfig.DEBUG) Log.d("tuanha", "onDestroy: $id")
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
                Log.d("tuanha", "navigate: ")
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
