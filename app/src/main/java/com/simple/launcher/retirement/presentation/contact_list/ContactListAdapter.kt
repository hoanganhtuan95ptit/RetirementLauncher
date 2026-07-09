package com.simple.launcher.retirement.presentation.contact_list

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemSelectableAppBinding
import com.simple.launcher.retirement.domain.model.SelectableContactEntity
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.getItem
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.setText

data class SelectableContactItem(
    val name: BigText,
    val photo: BigImage,
    val isSelected: Boolean,
    val entity: SelectableContactEntity  // chỉ dùng cho onclick
) : ViewItem {

    override fun areItemsTheSame(): List<Any> = listOf(entity.contact.id)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        name to "name",
        photo to "photo",
        isSelected to "isSelected"
    )
}

@Adapter
class ContactListAdapter : ViewItemAdapter<SelectableContactItem, ItemSelectableAppBinding>() {

    override val viewItemClass: Class<SelectableContactItem> by lazy {
        SelectableContactItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemSelectableAppBinding {
        return ItemSelectableAppBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemSelectableAppBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnSafeClickListener {
            val item = viewHolder.getItem<SelectableContactItem>() ?: return@setOnSafeClickListener
            AppEventBus.post(AppEvent.ContactSelected(item.entity))
        }
        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemSelectableAppBinding, viewType: Int, position: Int, item: SelectableContactItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        with(binding) {
            if (payloads.isEmpty() || payloads.contains("name")) {
                tvLabel.setText(item.name)
            }
            if (payloads.isEmpty() || payloads.contains("photo")) {
                ivIcon.setImage(item.photo)
            }
            if (payloads.isEmpty() || payloads.contains("isSelected")) {
                cbSelected.isChecked = item.isSelected
            }
        }
    }
}
