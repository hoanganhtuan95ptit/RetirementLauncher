package com.simple.launcher.retirement.presentation.home.adapter

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.adapter.base.BaseBindingViewHolder
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.sendDeeplinkWithBackStack
import com.simple.launcher.retirement.databinding.ItemAppBinding
import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.utils.getItem
import com.simple.launcher.retirement.utils.image.RichImage
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener

data class AppHomeItem(
    val label: RichText,
    val icon: RichImage,
    val entity: AppEntity  // chỉ dùng cho onclick
) : HomeItem {

    override fun areItemsTheSame(): List<Any> = listOf(entity.packageName)
    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        label to "label",
        icon to "icon"
    )
}

@Adapter
class AppAdapter : ViewItemAdapter<AppHomeItem, ItemAppBinding>() {
    override val viewItemClass: Class<AppHomeItem> by lazy {
        AppHomeItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemAppBinding {
        return ItemAppBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemAppBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnSafeClickListener {
            val item = viewHolder.getItem<AppHomeItem>() ?: return@setOnSafeClickListener
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val context = it.context
            if (item.entity.packageName == context.packageName) {
                sendDeeplinkWithBackStack(DeepLinks.SETTINGS)
            } else {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(item.entity.packageName)
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                }
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemAppBinding, viewType: Int, position: Int, item: AppHomeItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        if (payloads.isEmpty() || payloads.contains("label")) {
            binding.tvLabel.setText(item.label)
        }
        if (payloads.isEmpty() || payloads.contains("icon")) {
            binding.ivIcon.setImage(item.icon)
        }
    }
}
