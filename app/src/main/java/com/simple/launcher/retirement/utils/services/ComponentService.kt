@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package com.simple.launcher.retirement.utils.services

import android.app.Application
import android.content.ComponentCallbacks
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

interface ComponentService<T : ComponentCallbacks> {

    fun priority(): Int = 0

    fun setup(t: T)
}


interface ApplicationService : ComponentService<Application> {
    override fun setup(application: Application)
}


// Activity Services
interface ActivityCreatedService : ComponentService<FragmentActivity> {
    override fun setup(fragmentActivity: FragmentActivity)
}

interface ActivityStartedService : ComponentService<FragmentActivity> {
    override fun setup(fragmentActivity: FragmentActivity)
}

interface ActivityResumedService : ComponentService<FragmentActivity> {
    override fun setup(fragmentActivity: FragmentActivity)
}


// Fragment Services
interface FragmentAttachedService : ComponentService<Fragment> {
    override fun setup(fragment: Fragment)
}

interface FragmentCreatedService : ComponentService<Fragment> {
    override fun setup(fragment: Fragment)
}

interface FragmentViewCreatedService : ComponentService<Fragment> {
    override fun setup(fragment: Fragment)
}

interface FragmentStartedService : ComponentService<Fragment> {
    override fun setup(fragment: Fragment)
}

interface FragmentResumedService : ComponentService<Fragment> {
    override fun setup(fragment: Fragment)
}