package com.simple.launcher.retirement

import android.app.Application
import com.simple.launcher.retirement.utils.string.StringResStore
import com.simple.launcher.retirement.utils.theme.ThemeColorStore

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        StringResStore.load(this)
        ThemeColorStore.load(this)
    }

    companion object {
        lateinit var instance: MainApplication
            private set
    }
}
