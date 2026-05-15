package com.simple.launcher.retirement.presentation.home.adapter

import com.simple.adapter.ViewItemAdapter
import com.simple.launcher.retirement.databinding.ItemUtilityBinding

/**
 * Base adapter cho các utility item (CleanFiles, CleanMemory).
 * Gộp phần logic chung nếu có.
 */
abstract class UtilityAdapter<T : HomeItem> : ViewItemAdapter<T, ItemUtilityBinding>()
