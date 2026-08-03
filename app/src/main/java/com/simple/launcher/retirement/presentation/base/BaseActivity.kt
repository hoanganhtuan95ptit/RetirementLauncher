package com.simple.launcher.retirement.presentation.base

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    var binding: VB? = null

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {

            // true = icon tối, phù hợp với nền sáng
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        // Bỏ lớp nền mờ của navigation bar khi dùng chế độ 3 nút.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            window.isNavigationBarContrastEnforced = false
        }

        binding = inflateBinding(layoutInflater)
        setContentView(binding?.root ?: return)
        setupViews(savedInstanceState)
        observeData()
    }

    open fun setupViews(savedInstanceState: Bundle?) {}

    open fun observeData() {}

    override fun onDestroy() {

        super.onDestroy()
        binding = null
    }
}
