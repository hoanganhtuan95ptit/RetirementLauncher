package com.simple.launcher.retirement.presentation.emergency.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItem
import com.simple.adapter.ViewItemAdapter
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.ItemSosCardBinding
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.utils.exts.SpanSizeLookupViewItem
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.setText

data class SOSCardViewItem(
    val id: Int,
    val title: BigText,
    val desc: BigText? = null,
    val icon: BigImage,
    val endIcon: BigImage = BigImage(R.drawable.ic_chevron_right),
    val isEnabled: Boolean = true
) : ViewItem, SpanSizeLookupViewItem {

    override fun getSpanSize(): Int = 2

    override fun areItemsTheSame(): List<Any> = listOf(id)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        title to "title",
        (desc ?: "") to "desc",
        icon to "icon",
        endIcon to "endIcon",
        isEnabled to "isEnabled"
    )
}

@Adapter
class SOSCardAdapter : ViewItemAdapter<SOSCardViewItem, ItemSosCardBinding>() {

    override val viewItemClass: Class<SOSCardViewItem> = SOSCardViewItem::class.java

    override fun createViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemSosCardBinding {

        return ItemSosCardBinding.inflate(layoutInflater, parent, false)
    }

    override fun onViewItemClick(item: SOSCardViewItem) {

        super.onViewItemClick(item)

        AppEventBus.post(AppEvent.SOSItemClicked(item.id))
    }

    override fun onBindViewHolder(
        binding: ItemSosCardBinding,
        viewType: Int,
        position: Int,
        item: SOSCardViewItem,
        payloads: List<String>
    ) {

        val enabledAlpha = if (item.isEnabled) 1f else 0.5f

        if (payloads.isEmpty() || payloads.contains("isEnabled")) {

            binding.ivChevron.alpha = enabledAlpha
            binding.ivCardIcon.alpha = enabledAlpha
            binding.tvCardDesc.alpha = enabledAlpha
            binding.tvCardTitle.alpha = enabledAlpha
        }

        if (payloads.isEmpty() || payloads.contains("title")) {

            binding.tvCardTitle.setText(item.title)
        }

        if (payloads.isEmpty() || payloads.contains("icon")) {

            binding.ivCardIcon.setImage(item.icon)
        }

        if (payloads.isEmpty() || payloads.contains("endIcon")) {

            binding.ivChevron.setImage(item.endIcon)
        }

        if (payloads.isEmpty() || payloads.contains("desc")) {

            binding.tvCardDesc.isVisible = item.desc != null
            binding.tvCardDesc.setText(item.desc)
        }
    }
}
