package com.simple.launcher.retirement.presentation.app_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemSelectableAppBinding
import com.simple.launcher.retirement.domain.model.SelectableAppEntity
import com.simple.launcher.retirement.utils.image.RichImage
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.setText

data class SelectableAppItem(
    val label: RichText,
    val icon: RichImage,
    val isSelected: Boolean,
    val entity: SelectableAppEntity  // chỉ dùng cho onclick
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

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemSelectableAppBinding {
        return ItemSelectableAppBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemSelectableAppBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnClickListener {
            val item = (viewHolder.bindingAdapter as? ListAdapter<*, *>)?.currentList?.getOrNull(viewHolder.bindingAdapterPosition) as? SelectableAppItem ?: return@setOnClickListener
            AppListEventBus.post(item.entity)  // chỉ gửi entity, ViewModel xử lý toggle
        }
        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemSelectableAppBinding, viewType: Int, position: Int, item: SelectableAppItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        with(binding) {
            if (payloads.isEmpty()) {
                tvLabel.setText(item.label)
                ivIcon.setImage(item.icon)
            }
            if (payloads.isEmpty() || payloads.contains("isSelected")) {
                cbSelected.isChecked = item.isSelected
            }
        }
    }
}
