package com.simple.launcher.retirement.presentation.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemSettingBinding
import com.simple.launcher.retirement.utils.getItem
import com.simple.launcher.retirement.utils.image.RichImage
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

data class SettingItem(
    val id: Int,
    val title: RichText,
    val icon: RichImage,
    val isSwitch: Boolean = false,
    var isChecked: Boolean = false
) : ViewItem {

    override fun areItemsTheSame(): List<Any> = listOf(id)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        title to "title",   // so sánh full RichText để detect cả thay đổi span (ForegroundColor, ...)
        icon to "icon",
        isSwitch to "isSwitch",
        isChecked to "isChecked"
    )

    companion object {
        const val ID_PIN = 1
        const val ID_APP_LIST = 2
        const val ID_DEFAULT_LAUNCHER = 3
        const val ID_CLEAN_FILES = 4
        const val ID_CLEAN_MEMORY = 5
        const val ID_CONTACT_LIST = 6
        const val ID_TOGGLE_BLOCK = 7
        const val ID_TOGGLE_CLEANUP = 8
        const val ID_TOGGLE_CALL_BLOCK = 9
    }
}

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
            root.setOnSafeClickListener {
                val item = viewHolder.getItem<SettingItem>() ?: return@setOnSafeClickListener
                if (item.isSwitch) {
                    swSetting.toggle()
                } else {
                    SettingsEventBus.post(item)
                }
            }
            swSetting.setOnCheckedChangeListener { _, isChecked ->
                val item = viewHolder.getItem<SettingItem>() ?: return@setOnCheckedChangeListener
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
