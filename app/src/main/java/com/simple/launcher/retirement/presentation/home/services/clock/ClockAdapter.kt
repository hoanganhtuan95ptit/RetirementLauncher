package com.simple.launcher.retirement.presentation.home.services.clock

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.launcher.retirement.databinding.ItemClockBinding
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem

object ClockHomeItem : HomeItem {
    override val spanSize: Int = HomeItem.TOTAL_COLUMNS // full width
    override fun areItemsTheSame(): List<Any> = listOf("Clock")
}

@Adapter
class ClockAdapter : ViewItemAdapter<ClockHomeItem, ItemClockBinding>() {

    override val viewItemClass: Class<ClockHomeItem> by lazy {
        ClockHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemClockBinding {
        return ItemClockBinding.inflate(layoutInflater, parent, false)
    }
}
