package com.simple.launcher.retirement.presentation.base

import androidx.lifecycle.ViewModel
import com.simple.launcher.retirement.utils.string.StringResStore
import com.simple.launcher.retirement.utils.theme.ThemeColorStore

open class BaseViewModel: ViewModel() {
    
    val strings = StringResStore.stringMapFlow
    
    
    val themes = ThemeColorStore.colorMapFlow
}