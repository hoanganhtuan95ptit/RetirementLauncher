package com.simple.launcher.retirement.presentation.home.services.clock

import android.graphics.Color
import com.simple.adapter.Adapter
import com.simple.launcher.retirement.presentation.base.adapters.PrecomputedAdapter
import com.simple.launcher.retirement.presentation.base.adapters.PrecomputedViewItem
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.sp
import com.simple.ui.precompute.LayoutEngine
import com.simple.ui.precompute.node.BackgroundNode
import com.simple.ui.precompute.node.ConstraintChild
import com.simple.ui.precompute.node.ConstraintNode
import com.simple.ui.precompute.node.Constraints
import com.simple.ui.precompute.node.EdgeInsets
import com.simple.ui.precompute.node.LayoutDimension
import com.simple.ui.precompute.node.LinearNode
import com.simple.ui.precompute.node.Orientation

data class ClockHomeItem(
    val screenWidth: Int
) : PrecomputedViewItem(), HomeItem {

    override val spanSize: Int = HomeItem.TOTAL_COLUMNS

    override fun buildDrawSpec(resources: Map<String, Any>) {

        spec = LayoutEngine.measure(
            node = createClockCardNode(),
            constraints = Constraints(maxWidth = screenWidth),
            id = "clock"
        )
    }

    private fun createClockCardNode(): ConstraintNode = ConstraintNode(
        children = listOf(
            ConstraintChild(
                id = "background",
                node = BackgroundNode(
                    backgroundColor = Color.WHITE,
                    cornerRadius = 24.dp()
                ),
                width = LayoutDimension.MatchParent,
                height = LayoutDimension.MatchParent,
                startToStartOf = "content",
                endToEndOf = "content",
                topToTopOf = "content",
                bottomToBottomOf = "content"
            ),
            ConstraintChild(
                id = "content",
                node = LinearNode(
                    orientation = Orientation.VERTICAL,
                    children = listOf(
                        TimeNode(
                            pattern = "hh:mm",
                            textSizePx = 48.sp(),
                            color = Color.BLACK,
                            isBold = true
                        ),
                        TimeNode(
                            pattern = "EEEE, MMM/dd/yyyy",
                            textSizePx = 18.sp(),
                            color = Color.parseColor("#666666")
                        )
                    ),
                    padding = EdgeInsets.all(24.dp().toInt())
                ),
                width = LayoutDimension.MatchParent,
                startToStartOf = ConstraintNode.PARENT,
                endToEndOf = ConstraintNode.PARENT,
                topToTopOf = ConstraintNode.PARENT
            )
        ),
        layoutWidth = LayoutDimension.MatchParent,
        padding = EdgeInsets.symmetric(h = 8.dp().toInt(), v = 8.dp().toInt())
    )

    override fun areItemsTheSame(): List<Any> = listOf("Clock")

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        screenWidth to "screenWidth"
    )
}

@Adapter
class ClockAdapter : PrecomputedAdapter<ClockHomeItem>() {

    override val viewItemClass: Class<ClockHomeItem> by lazy {

        ClockHomeItem::class.java
    }

    override fun onItemCLick(item: ClockHomeItem) {

    }
}
