package com.simple.launcher.retirement.presentation.app_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemSelectableAppBinding
import com.simple.launcher.retirement.domain.model.SelectableAppEntity
import com.simple.launcher.retirement.utils.image.ImageDrawable
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.text.toRich

@Adapter
class AppListAdapter : ViewItemAdapter<SelectableAppEntity, ItemSelectableAppBinding>() {

    override val viewItemClass: Class<SelectableAppEntity> by lazy {
        SelectableAppEntity::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemSelectableAppBinding {
        return ItemSelectableAppBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemSelectableAppBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnClickListener {
            val item = (viewHolder.bindingAdapter as? ListAdapter<*, *>)?.currentList?.getOrNull(viewHolder.absoluteAdapterPosition) as? SelectableAppEntity ?: return@setOnClickListener
            item.isSelected = !item.isSelected
            AppListEventBus.post(item)
        }
        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemSelectableAppBinding, viewType: Int, position: Int, item: SelectableAppEntity, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        with(binding) {
            if (payloads.isEmpty()) {
                tvLabel.setText(item.app.label.toRich())
                ivIcon.setImage(ImageDrawable(item.app.icon))
            }
            if (payloads.isEmpty() || payloads.contains("isSelected")) {
                cbSelected.isChecked = item.isSelected
            }
        }
    }
}
