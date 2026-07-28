package com.simple.launcher.retirement.presentation.app_list

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemSelectableAppBinding
import com.simple.launcher.retirement.domain.model.SelectableAppEntity
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.launcher.retirement.utils.exts.getItem
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.setText

data class SelectableAppItem(
    val label: BigText,
    val icon: BigImage,
    val isSelected: Boolean,
    val entity: SelectableAppEntity // chỉ dùng cho onclick
) : ViewItem {

    override fun areItemsTheSame(): List<Any> = listOf(entity.app.packageName)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        label to "label",
        icon to "icon",
        isSelected to "isSelected"
    )
}

@Adapter
class AppListAdapter : ViewItemAdapter<SelectableAppItem, ItemSelectableAppBinding>() {

    override val viewItemClass: Class<SelectableAppItem> by lazy {
        SelectableAppItem::class.java
    }

    override fun createViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemSelectableAppBinding {

        return ItemSelectableAppBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseBindingViewHolder<ItemSelectableAppBinding> {

        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnSafeClickListener {

            val item = viewHolder.getItem<SelectableAppItem>() ?: return@setOnSafeClickListener
            AppEventBus.post(AppEvent.AppSelected(item.entity))
        }

        return viewHolder
    }

    override fun onBindViewHolder(
        binding: ItemSelectableAppBinding,
        viewType: Int,
        position: Int,
        item: SelectableAppItem,
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
