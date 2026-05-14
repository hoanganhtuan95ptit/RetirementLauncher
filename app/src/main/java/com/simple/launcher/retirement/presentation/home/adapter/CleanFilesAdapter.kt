package com.simple.launcher.retirement.presentation.home.adapter

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.ItemUtilityBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

data class CleanFilesHomeItem(val fileCount: Int) : HomeItem {
    override fun areItemsTheSame(): List<Any> = listOf("CleanFiles")
    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        fileCount to "fileCount"
    )
}

@Adapter
class CleanFilesAdapter : ViewItemAdapter<CleanFilesHomeItem, ItemUtilityBinding>() {
    override val viewItemClass: Class<CleanFilesHomeItem> by lazy {
        CleanFilesHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemUtilityBinding {
        return ItemUtilityBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemUtilityBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnClickListener {
            val item = (viewHolder.bindingAdapter as? ListAdapter<*, *>)?.currentList?.getOrNull(viewHolder.absoluteAdapterPosition) as? CleanFilesHomeItem ?: return@setOnClickListener
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            HomeEventBus.post(item)
        }
        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemUtilityBinding, viewType: Int, position: Int, item: CleanFilesHomeItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)

        if (payloads.isEmpty() || payloads.contains("fileCount")) {
            binding.tvLabel.text = "Clean up (${item.fileCount})"
            Glide.with(binding.ivIcon.context)
                .load(R.drawable.ic_home_cleanup_24dp)
                .transform(CenterInside(), RoundedCorners(24))
                .into(binding.ivIcon)
        }
    }
}
