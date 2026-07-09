package com.simple.launcher.retirement

import android.app.Application
import com.simple.ui.precompute.loader.GlideImageLoader
import com.simple.ui.precompute.loader.ImageLoader

class MainApplication : Application() {

    override fun onCreate() {

        super.onCreate()
        instance = this

        // Precompute node/image rendering dùng chung một image loader toàn app.
        ImageLoader.install(GlideImageLoader(this))
    }

    companion object {

        lateinit var instance: MainApplication
            private set
    }
}
