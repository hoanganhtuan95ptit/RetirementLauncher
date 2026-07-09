package com.simple.launcher.retirement.presentation.home.services.contact

import androidx.fragment.app.viewModels
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.presentation.home.HomeFragment
import com.simple.launcher.retirement.presentation.home.services.HomeService
import com.simple.component.service.launchCollect
import kotlinx.coroutines.flow.filterNotNull

@AutoRegister([HomeFragment::class])
class ContactHomeService : HomeService() {

    override fun setup(homeFragment: HomeFragment) {

        val viewModel = homeFragment.viewModels<ContactViewModel>().value

        viewModel.contactViewItemList.filterNotNull().launchCollect(homeFragment.viewLifecycleOwner) {

            homeViewModel.updateItem(it)
        }
    }
}