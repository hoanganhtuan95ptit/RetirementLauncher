package com.simple.launcher.retirement.presentation.home.adapter

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemContactBinding
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.utils.image.RichImage
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.setText

data class ContactHomeItem(
    val name: RichText,
    val photo: RichImage,
    val entity: ContactEntity  // chỉ dùng cho onclick
) : HomeItem {
    override fun areItemsTheSame(): List<Any> = listOf(entity.id)
    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        name to "name",
        photo to "photo"
    )
    override val spanSize: Int = HomeItem.TOTAL_COLUMNS / 2 // half width
}

@Adapter
class ContactAdapter : ViewItemAdapter<ContactHomeItem, ItemContactBinding>() {
    override val viewItemClass: Class<ContactHomeItem> by lazy {
        ContactHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemContactBinding {
        return ItemContactBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemContactBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnClickListener {
            val item = (viewHolder.bindingAdapter as? ListAdapter<*, *>)?.currentList?.getOrNull(viewHolder.absoluteAdapterPosition) as? ContactHomeItem ?: return@setOnClickListener
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            HomeEventBus.post(item)
        }
        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemContactBinding, viewType: Int, position: Int, item: ContactHomeItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        if (payloads.isEmpty() || payloads.contains("name")) {
            binding.tvName.setText(item.name)
        }
        if (payloads.isEmpty() || payloads.contains("photo")) {
            binding.ivPhoto.setImage(item.photo)
        }
    }
}
