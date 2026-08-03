package com.simple.launcher.retirement

import android.app.Application
import android.content.Context
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.ui.precompute.loader.GlideImageLoader
import com.simple.ui.precompute.loader.ImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainApplication : Application() {

    // Scope dùng cho các tác vụ warm-up chạy suốt vòng đời app.
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {

        super.onCreate()
        instance = this

        // Precompute node/image rendering dùng chung một image loader toàn app.
        ImageLoader.install(GlideImageLoader(this))

        // Warm up SharedPreferences + repository singleton trên IO thread.
        // - getSharedPreferences() lần đầu chỉ kích hoạt load XML async;
        //   gọi .all buộc load thật sự → sau đó mọi read đều hit RAM cache.
        // - Touch PreferenceRepository.instance để trigger `by lazy` init
        //   (đọc 9 pref cho các StateFlow) chạy ngay ở IO chứ không đợi
        //   MainActivity gọi đầu tiên trên main.
        appScope.launch(Dispatchers.IO) {

            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).all
            PreferenceRepository.instance
            AppRepository.instance
            ContactRepository.instance
        }
    }

    companion object {

        // Tên file khớp với các Repository (launcher_prefs).
        private const val PREFS_NAME = "launcher_prefs"

        lateinit var instance: MainApplication
            private set
    }
}
