package com.simple.launcher.retirement.presentation.home.services.app

import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.component.service.launchCollect
import com.simple.launcher.retirement.presentation.home.HomeFragment
import com.simple.launcher.retirement.presentation.home.services.HomeService
import kotlinx.coroutines.flow.filterNotNull

@AutoRegister([HomeFragment::class])
class AppHomeService : HomeService() {

    override fun setup(homeFragment: HomeFragment) {

        val viewModel = homeFragment.viewModels<AppViewModel>().value

        viewModel.appViewItemList.filterNotNull().launchCollect(homeFragment.viewLifecycleOwner) {

            homeViewModel.updateItem(it)
        }
    }
}