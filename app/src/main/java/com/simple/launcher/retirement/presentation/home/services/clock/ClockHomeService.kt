package com.simple.launcher.retirement.presentation.home.services.clock

import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.presentation.home.HomeFragment
import com.simple.launcher.retirement.presentation.home.services.HomeService
import com.simple.component.service.launchCollect
import kotlinx.coroutines.flow.filterNotNull

@AutoRegister([HomeFragment::class])
class ClockHomeService : HomeService() {

    override fun setup(homeFragment: HomeFragment) {

        val viewModel = homeFragment.viewModels<ClockViewModel>().value

        viewModel.timeViewItemList.filterNotNull().launchCollect(homeFragment.viewLifecycleOwner) {

            homeViewModel.updateItem(it)
        }
    }
}