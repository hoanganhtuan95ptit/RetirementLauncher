package com.simple.launcher.retirement.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    var binding: VB? = null
    
    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = inflateBinding(inflater, container)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setOnClickListener {  }
        super.onViewCreated(view, savedInstanceState)
        setupViews(view, savedInstanceState)
        observeData()
    }

    open fun setupViews(view: View, savedInstanceState: Bundle?) {}

    open fun observeData() {}

    override fun onDestroyView() {

        super.onDestroyView()
        binding = null
    }
}
