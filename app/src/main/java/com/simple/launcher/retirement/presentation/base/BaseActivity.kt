package com.simple.launcher.retirement.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        setupViews(savedInstanceState)
        observeData()
    }

    open fun setupViews(savedInstanceState: Bundle?) {}

    open fun observeData() {}

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
