package com.simple.launcher.retirement.presentation.home.adapter

import android.graphics.Color
import com.simple.adapter.Adapter
import com.simple.launcher.retirement.presentation.base.adapters.PrecomputedAdapter
import com.simple.launcher.retirement.presentation.base.adapters.PrecomputedViewItem
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.sp
import com.simple.ui.precompute.LayoutEngine
import com.simple.ui.precompute.node.Constraints
import com.simple.ui.precompute.node.EdgeInsets
import com.simple.ui.precompute.node.TextNode
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigBold
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.span.BigTextSize
import com.simple.ui.precompute.text.toBuilder
import com.simple.ui.precompute.text.with

data class HeaderHomeItem(
    val title: String,
    val screenWidth: Int
) : PrecomputedViewItem(), HomeItem {

    override val spanSize: Int = HomeItem.TOTAL_COLUMNS

    override fun buildDrawSpec(resources: Map<String, Any>) {
        spec = LayoutEngine.measure(
            node = TextNode(
                text = title
                    .toBuilder()
                    .with(BigBold, BigTextSize(22.sp()), BigForegroundColor(Color.WHITE))
                    .build(),
                padding = EdgeInsets(left = 8.dp().toInt(), right = 8.dp().toInt(), top = 24.dp().toInt())
            ),
            constraints = Constraints(maxWidth = screenWidth),
            id = title
        )
    }

    override fun areItemsTheSame(): List<Any> = listOf(
        title
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        title to "title",
        screenWidth to "screenWidth"
    )
}

@Adapter
class HeaderAdapter : PrecomputedAdapter<HeaderHomeItem>() {

    override val viewItemClass: Class<HeaderHomeItem> by lazy {

        HeaderHomeItem::class.java
    }

    override fun onItemCLick(item: HeaderHomeItem) {

    }
}
