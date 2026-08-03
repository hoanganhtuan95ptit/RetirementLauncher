package com.simple.launcher.retirement.presentation.base.adapters

import com.simple.adapter.Adapter
import com.simple.launcher.retirement.presentation.base.services.width
import com.simple.launcher.retirement.utils.exts.SpanSizeLookupViewItem
import com.simple.ui.precompute.LayoutEngine
import com.simple.ui.precompute.node.Constraints
import com.simple.ui.precompute.node.LayoutDimension.Companion.toLayoutDimension
import com.simple.ui.precompute.node.SpaceNode

data class SpaceViewItem(
    val width: Int = -1,
    val height: Int = -1,

    val span: Int = 1
) : PrecomputedViewItem(), SpanSizeLookupViewItem {

    override fun buildDrawSpec(resources: Map<String, Any>) {

        spec = LayoutEngine.measure(
            node = SpaceNode(
                layoutWidth = width.toLayoutDimension(),
                layoutHeight = height.toLayoutDimension()
            ),
            constraints = Constraints(maxWidth = resources.width),
            id = "space_${width}_${height}"
        )
    }

    override fun getSpanSize(): Int {

        return span
    }

    override fun areItemsTheSame(): List<Any> = listOf(
        width, height
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        width to "width",
        height to "height"
    )
}

@Adapter
class SpaceAdapter : PrecomputedAdapter<SpaceViewItem>() {

    override val viewItemClass: Class<SpaceViewItem> by lazy {

        SpaceViewItem::class.java
    }

    override fun onItemCLick(item: SpaceViewItem) {
    }
}
