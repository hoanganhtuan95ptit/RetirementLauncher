package com.simple.launcher.retirement.presentation.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemSettingBinding
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.exts.SpanSizeLookupViewItem
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
    var isChecked: Boolean = false,
) : ViewItem, SpanSizeLookupViewItem {

    override fun getSpanSize(): Int = 1

    override fun areItemsTheSame(): List<Any> = listOf(id)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        title to "title",
        icon to "icon",
        isSwitch to "isSwitch",
        isChecked to "isChecked",
    )

    companion object {
        // ── Item IDs ─────────────────────────────────────────────────────────
        const val ID_PIN = 1
        const val ID_APP_LIST = 2
        const val ID_DEFAULT_LAUNCHER = 3
        const val ID_CLEAN_FILES = 4
        const val ID_CLEAN_MEMORY = 5
        const val ID_CONTACT_LIST = 6
        const val ID_TOGGLE_BLOCK = 7
        const val ID_TOGGLE_CLEANUP = 8
        const val ID_TOGGLE_CALL_BLOCK = 9
        const val ID_TOGGLE_POCKET_MODE = 10
        const val ID_EMERGENCY_CALL_TOGGLE = 11

        // ── Slot orders (dùng cho _itemMap trong SettingsViewModel) ──────────
        const val ORDER_HEADER_GENERAL = 10.0
        const val ORDER_DEFAULT_LAUNCHER = 11.0
        const val ORDER_APP_LIST = 12.0
        const val ORDER_CONTACT_LIST = 13.0

        const val ORDER_HEADER_SECURITY = 20.0
        const val ORDER_PIN = 21.0
        const val ORDER_TOGGLE_BLOCK = 22.0
        const val ORDER_TOGGLE_CALL_BLOCK = 23.0
        const val ORDER_TOGGLE_CLEANUP = 24.0
        const val ORDER_EMERGENCY_CALL_TOGGLE = 25.0

        const val ORDER_HEADER_OPTIMIZATION = 30.0
        const val ORDER_CLEAN_FILES = 31.0
        const val ORDER_CLEAN_MEMORY = 32.0
        const val ORDER_TOGGLE_POCKET_MODE = 33.0

        // ── Debug-only ────────────────────────────────────────────────────────
        const val ID_DEBUG_BLOCK_SCREEN = 100
        const val ORDER_HEADER_DEBUG = 90.0
        const val ORDER_DEBUG_BLOCK_SCREEN = 91.0
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

        viewHolder.binding.root.setOnSafeClickListener {

            val item = viewHolder.getItem<SettingItem>() ?: return@setOnSafeClickListener
            if (item.isSwitch) {
                viewHolder.binding.swSetting.toggle()
            } else {
                AppEventBus.post(AppEvent.SettingClicked(item))
            }
        }

        viewHolder.binding.swSetting.setOnCheckedChangeListener { _, isChecked ->

            val item = viewHolder.getItem<SettingItem>() ?: return@setOnCheckedChangeListener
            if (item.isChecked != isChecked) {
                item.isChecked = isChecked
                AppEventBus.post(AppEvent.SettingClicked(item))
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemSettingBinding, viewType: Int, position: Int, item: SettingItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)

        if (payloads.isEmpty() || payloads.contains("title")) {
            binding.tvSettingTitle.setText(item.title)
        }

        if (payloads.isEmpty() || payloads.contains("icon")) {
            binding.ivSettingIcon.setImage(item.icon)
        }

        if (payloads.isEmpty() || payloads.contains("isSwitch")) {
            binding.swSetting.isVisible = item.isSwitch
        }

        if (payloads.isEmpty() || payloads.contains("isChecked")) {
            binding.swSetting.isChecked = item.isChecked
        }
    }
}
