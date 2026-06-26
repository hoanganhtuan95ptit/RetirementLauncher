package com.simple.launcher.retirement.presentation.home.services.time

import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.presentation.home.HomeFragment
import com.simple.launcher.retirement.presentation.home.services.HomeService
import com.simple.launcher.retirement.utils.services.launchCollect

@AutoRegister([HomeFragment::class])
class TimeHomeService : HomeService() {

    override fun setup(homeFragment: HomeFragment) {

        val viewModel = homeFragment.viewModels<TimeViewModel>().value
        val homeViewModel = homeFragment.viewModel

        viewModel.timeViewItemList.launchCollect(homeFragment.viewLifecycleOwner) {

            homeViewModel.updateItem(it.first, it.second)
        }
    }
}