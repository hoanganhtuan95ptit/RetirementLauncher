package com.simple.launcher.retirement.presentation.home.adapter

import android.net.Uri
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.databinding.ItemContactBinding
import com.simple.launcher.retirement.domain.model.ContactEntity

data class ContactHomeItem(val entity: ContactEntity) : HomeItem {
    override fun areItemsTheSame(): List<Any> = listOf(entity.id)
    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        entity.name to "name",
        entity.phoneNumber to "phoneNumber",
        (entity.photoUri ?: "") to "photoUri"
    )
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
            binding.tvName.text = item.entity.name
        }

        if (payloads.isEmpty() || payloads.contains("photoUri") || payloads.contains("phoneNumber")) {
            if (item.entity.photoUri != null) {
                binding.ivPhoto.setImageURI(Uri.parse(item.entity.photoUri))
            } else {
                binding.ivPhoto.setImageResource(android.R.drawable.sym_def_app_icon)
            }
        }
    }
}
