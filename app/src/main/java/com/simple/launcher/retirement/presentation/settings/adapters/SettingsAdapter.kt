package com.simple.launcher.retirement.presentation.settings.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemSettingBinding
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.exts.SpanSizeLookupViewItem
import com.simple.launcher.retirement.utils.getItem
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.setText

data class SettingItem(
    val id: Int,
    val title: BigText,

    val icon: BigImage,
    val iconBackground: Background = Background(),

    val isSwitch: Boolean = false,
    var isChecked: Boolean = false,

    val background: Background = Background(),
) : ViewItem, SpanSizeLookupViewItem {

    override fun getSpanSize(): Int = 1

    override fun areItemsTheSame(): List<Any> = listOf(id)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        title to "title",

        icon to "icon",
        iconBackground to "iconBackground",

        isSwitch to "isSwitch",
        isChecked to "isChecked",

        background to "background"
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
        const val ORDER_TOGGLE_BLOCK = 22.0
        const val ORDER_TOGGLE_CALL_BLOCK = 23.0
        const val ORDER_TOGGLE_CLEANUP = 24.0
        const val ORDER_TOGGLE_POCKET_MODE = 33.0

        // ── Debug-only ────────────────────────────────────────────────────────
        const val ID_DEBUG_BLOCK_SCREEN = 100
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

        viewHolder.binding.swSetting.disableUserChange()

        viewHolder.binding.vClick.setOnSafeClickListener {

            val item = viewHolder.getItem<SettingItem>() ?: return@setOnSafeClickListener
            AppEventBus.post(AppEvent.SettingClicked(item))
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

        if (payloads.isEmpty() || payloads.contains("iconBackground")) {
            binding.ivSettingIcon.setBackground(item.iconBackground)
        }

        if (payloads.isEmpty() || payloads.contains("isSwitch")) {
            binding.swSetting.isVisible = item.isSwitch
        }

        if (payloads.isEmpty() || payloads.contains("isChecked")) {
            binding.swSetting.isChecked = item.isChecked
        }

        if (payloads.isEmpty() || payloads.contains("background")) {
            binding.content.setBackground(item.background)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun SwitchCompat.disableUserChange() = setOnTouchListener { _, _ ->
        true
    }
}
