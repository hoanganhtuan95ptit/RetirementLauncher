package com.simple.launcher.retirement.presentation.home.services

import androidx.fragment.app.Fragment
import com.simple.component.service.FragmentViewCreatedService
import com.simple.launcher.retirement.presentation.home.HomeFragment
import com.simple.launcher.retirement.presentation.home.HomeViewModel

abstract class HomeService : FragmentViewCreatedService {

    protected lateinit var homeViewModel: HomeViewModel

    abstract fun setup(homeFragment: HomeFragment)

    final override fun setup(fragment: Fragment) {

        if (fragment is HomeFragment) {

            homeViewModel = fragment.viewModel

            setup(homeFragment = fragment)
        }
    }
}