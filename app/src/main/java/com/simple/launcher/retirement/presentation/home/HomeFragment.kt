package com.simple.launcher.retirement.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.simple.adapter.MultiAdapter
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.launcher.retirement.databinding.FragmentHomeBinding
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.launcher.retirement.utils.lifecycle.observe

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    val viewModel: HomeViewModel by activityViewModels<HomeViewModel>()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        val layoutManager = GridLayoutManager(requireContext(), HomeItem.TOTAL_COLUMNS)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return ((binding.rvApps.adapter as? MultiAdapter)?.currentList?.getOrNull(position) as? HomeItem)?.spanSize ?: 2
            }
        }
        binding.rvApps.layoutManager = layoutManager
    }

    override fun observeData() = with(viewModel) {
        super.observeData()

        viewItemList.attachAdapter().observe(viewLifecycleOwner) { (items, adapters) ->

            binding.rvApps.submitListAndAwait(items, adapters, true)
        }
    }
}
