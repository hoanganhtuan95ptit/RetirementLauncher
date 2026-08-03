package com.simple.launcher.retirement.data

import android.content.Context
import com.google.gson.Gson
import com.simple.launcher.retirement.MainApplication

/**
 * Singleton chia sẻ SharedPreferences + Gson cho toàn bộ repository.
 * Tránh mỗi Impl tự new Gson() (mỗi instance ~ tốn heap) và gọi getSharedPreferences()
 * nhiều lần (đi qua synchronized cache trong ContextImpl).
 */
object AppPrefs {

    private const val PREFS_NAME = "launcher_prefs"

    val sharedPrefs by lazy {

        MainApplication.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    val gson: Gson by lazy { Gson() }
}
