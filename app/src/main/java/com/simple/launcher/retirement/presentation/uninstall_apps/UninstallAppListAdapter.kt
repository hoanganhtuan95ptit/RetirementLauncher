package com.simple.launcher.retirement.presentation.uninstall_apps

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemUninstallAppBinding
import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.exts.getItem
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.setText

data class UninstallAppItem(
    val label: BigText,
    val icon: BigImage,
    val isSelected: Boolean,
    val entity: AppEntity
) : ViewItem {

    override fun areItemsTheSame(): List<Any> = listOf(entity.packageName)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        label to "label",
        icon to "icon",
        isSelected to "isSelected"
    )
}

@Adapter
class UninstallAppListAdapter : ViewItemAdapter<UninstallAppItem, ItemUninstallAppBinding>() {

    override val viewItemClass: Class<UninstallAppItem> by lazy {

        UninstallAppItem::class.java
    }

    override fun createViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemUninstallAppBinding {

        return ItemUninstallAppBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseBindingViewHolder<ItemUninstallAppBinding> {

        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnSafeClickListener {

            val item = viewHolder.getItem<UninstallAppItem>() ?: return@setOnSafeClickListener
            AppEventBus.post(AppEvent.UninstallAppToggled(item.entity))
        }

        return viewHolder
    }

    override fun onBindViewHolder(
        binding: ItemUninstallAppBinding,
        viewType: Int,
        position: Int,
        item: UninstallAppItem,
        payloads: List<String>
    ) {

        super.onBindViewHolder(binding, viewType, position, item, payloads)
        with(binding) {

            if (payloads.isEmpty() || payloads.contains("label")) {

                tvLabel.setText(item.label)
            }

            if (payloads.isEmpty() || payloads.contains("icon")) {

                ivIcon.setImage(item.icon)
            }

            if (payloads.isEmpty() || payloads.contains("isSelected")) {

                cbSelected.isChecked = item.isSelected
            }
        }
    }
}
