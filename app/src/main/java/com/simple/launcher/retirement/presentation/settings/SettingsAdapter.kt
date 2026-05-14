package com.simple.launcher.retirement.presentation.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemSettingBinding
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.text.setText

@Adapter
class SettingsAdapter : ViewItemAdapter<SettingItem, ItemSettingBinding>() {

    override val viewItemClass: Class<SettingItem> by lazy {
        SettingItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemSettingBinding {
        return ItemSettingBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemSettingBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)
        with(viewHolder.binding) {
            root.setOnClickListener {
                val item = (viewHolder.bindingAdapter as? ListAdapter<*, *>)?.currentList?.getOrNull(viewHolder.absoluteAdapterPosition) as? SettingItem ?: return@setOnClickListener
                if (item.isSwitch) {
                    swSetting.toggle()
                } else {
                    SettingsEventBus.post(item)
                }
            }
            swSetting.setOnCheckedChangeListener { _, isChecked ->
                val item = (viewHolder.bindingAdapter as? ListAdapter<*, *>)?.currentList?.getOrNull(viewHolder.absoluteAdapterPosition) as? SettingItem ?: return@setOnCheckedChangeListener
                if (item.isChecked != isChecked) {
                    item.isChecked = isChecked
                    SettingsEventBus.post(item)
                }
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemSettingBinding, viewType: Int, position: Int, item: SettingItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        with(binding) {
            if (payloads.isEmpty() || payloads.contains("title")) {
                tvSettingTitle.setText(item.title)
            }
            if (payloads.isEmpty() || payloads.contains("icon")) {
                ivSettingIcon.setImage(item.icon)
            }
            if (payloads.isEmpty() || payloads.contains("isSwitch")) {
                swSetting.visibility = if (item.isSwitch) View.VISIBLE else View.GONE
            }
            if (payloads.isEmpty() || payloads.contains("isChecked")) {
                swSetting.isChecked = item.isChecked
            }
        }
    }
}
