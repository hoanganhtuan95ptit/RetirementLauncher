package com.simple.launcher.retirement.utils.services

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.ComponentCallbacks
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.startup.Initializer
import com.simple.auto.register.AutoRegisterManager
import kotlinx.coroutines.flow.map

class ServiceInitializer : Initializer<Unit> {

    override fun create(context: Context) {

        val application = context.applicationContext as? Application ?: return

        setupApplication(application)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    private fun setupApplication(application: Application) {

        AutoRegisterManager.subscribe(ApplicationService::class.java).map { it.toList() }.launchCollect(ProcessLifecycleOwner.get()) { list ->

            list.setup(application)
        }

        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

                if (activity is FragmentActivity) {

                    activity.updateState(LifecycleState.Created)
                    setupActivity(activity)
                }
            }

            override fun onActivityStarted(activity: Activity) {

                if (activity is FragmentActivity) {

                    activity.updateState(LifecycleState.Started)
                    setupComponentCallbacksLifecycle(activity, ActivityStartedService::class.java)
                }
            }

            override fun onActivityResumed(activity: Activity) {

                if (activity is FragmentActivity) {

                    activity.updateState(LifecycleState.Resumed)
                    setupComponentCallbacksLifecycle(activity, ActivityResumedService::class.java)
                }
            }

            override fun onActivityPaused(activity: Activity) {

                if (activity is FragmentActivity) {

                    activity.updateState(LifecycleState.Paused)
                }
            }

            override fun onActivityStopped(activity: Activity) {

                if (activity is FragmentActivity) {

                    activity.updateState(LifecycleState.Stopped)
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {

                if (activity is FragmentActivity) {

                    activity.updateState(LifecycleState.Destroyed)
                }
            }
        })
    }

    private fun setupActivity(fragmentActivity: FragmentActivity) {

        setupComponentCallbacksLifecycle(fragmentActivity, ActivityCreatedService::class.java)

        fragmentActivity.supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {

            override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {

                f.updateState(LifecycleState.Attached)
                setupComponentCallbacksLifecycle(f, FragmentAttachedService::class.java)
            }

            override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {

                f.updateState(LifecycleState.Created)
                setupComponentCallbacksLifecycle(f, FragmentCreatedService::class.java)
            }

            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {

                f.updateState(LifecycleState.ViewCreated)
                setupComponentCallbacksLifecycle(f, FragmentViewCreatedService::class.java)
            }

            override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {

                f.updateState(LifecycleState.Started)
                setupComponentCallbacksLifecycle(f, FragmentStartedService::class.java)
            }

            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {

                f.updateState(LifecycleState.Resumed)
                setupComponentCallbacksLifecycle(f, FragmentResumedService::class.java)
            }

            override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {

                f.updateState(LifecycleState.Paused)
            }

            override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {

                f.updateState(LifecycleState.Stopped)
            }

            override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {

                f.updateState(LifecycleState.ViewDestroyed)
            }

            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {

                f.updateState(LifecycleState.Destroyed)
            }

            override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
            }
        }, true)
    }

    private fun Any.updateState(state: LifecycleState) {

        (this as? ViewModelStoreOwner)?.let {

            ViewModelProvider(it)[LifecycleStateViewModel::class.java].updateState(state)
        }
    }

    private fun <Y : ComponentCallbacks, T : ComponentService<Y>> setupComponentCallbacksLifecycle(componentCallbacks: Y, api: Class<T>) {

        AutoRegisterManager.subscribe(api).map { it.toList() }.launchCollect(componentCallbacks as LifecycleOwner) { list ->

            list.setup(componentCallbacks)
        }

        AutoRegisterManager.subscribe(componentCallbacks.javaClass.name, api).map { it.toList() }.launchCollect(componentCallbacks as LifecycleOwner) { list ->

            list.setup(componentCallbacks)
        }
    }

    private fun <T : ComponentCallbacks> List<ComponentService<T>>.setup(t: T) {

        sortedBy { it.priority() }.forEach { it.setup(t) }
    }
}
