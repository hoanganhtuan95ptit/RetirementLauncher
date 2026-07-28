package com.simple.launcher.retirement.presentation.home.services.app

import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.presentation.home.HomeFragment
import com.simple.launcher.retirement.presentation.home.services.HomeService
import com.simple.launcher.retirement.utils.exts.observe
import kotlinx.coroutines.flow.filterNotNull

@AutoRegister([HomeFragment::class])
class AppHomeService : HomeService() {

    override fun setup(homeFragment: HomeFragment) {

        val viewModel = homeFragment.viewModels<AppViewModel>().value

        viewModel.appViewItemList.filterNotNull().observe(homeFragment.viewLifecycleOwner) {

            homeViewModel.updateItem(it)
        }
    }
}
