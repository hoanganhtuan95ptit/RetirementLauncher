package com.simple.launcher.retirement.presentation.contact_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemSelectableAppBinding
import com.simple.launcher.retirement.domain.model.SelectableContactEntity
import com.simple.launcher.retirement.utils.image.ImagePath
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.text.toRich

@Adapter
class ContactListAdapter : ViewItemAdapter<SelectableContactEntity, ItemSelectableAppBinding>() {

    override val viewItemClass: Class<SelectableContactEntity> by lazy {
        SelectableContactEntity::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemSelectableAppBinding {
        return ItemSelectableAppBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemSelectableAppBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnClickListener {
            val item = (viewHolder.bindingAdapter as? ListAdapter<*, *>)?.currentList?.getOrNull(viewHolder.absoluteAdapterPosition) as? SelectableContactEntity ?: return@setOnClickListener
            item.isSelected = !item.isSelected
            ContactListEventBus.post(item)
        }
        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemSelectableAppBinding, viewType: Int, position: Int, item: SelectableContactEntity, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        with(binding) {
            if (payloads.isEmpty()) {
                tvLabel.setText(item.contact.name.toRich())
                val image = if (item.contact.photoUri != null) {
                    ImagePath(item.contact.photoUri)
                } else {
                    ImageRes(android.R.drawable.ic_menu_call)
                }
                ivIcon.setImage(image)
            }
            if (payloads.isEmpty() || payloads.contains("isSelected")) {
                cbSelected.isChecked = item.isSelected
            }
        }
    }
}
