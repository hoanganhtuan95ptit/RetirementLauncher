package com.simple.launcher.retirement.presentation.home.services

import androidx.fragment.app.Fragment
import com.simple.launcher.retirement.presentation.home.HomeFragment
import com.simple.launcher.retirement.utils.services.FragmentViewCreatedService

abstract class HomeService : FragmentViewCreatedService {

    abstract fun setup(homeFragment: HomeFragment)

    final override fun setup(fragment: Fragment) {

        if (fragment is HomeFragment) setup(homeFragment = fragment)
    }
}