package com.simple.launcher.retirement.presentation.clock.adapters

import android.graphics.Color
import com.simple.adapter.Adapter
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.presentation.base.adapters.PrecomputedAdapter
import com.simple.launcher.retirement.presentation.base.adapters.PrecomputedViewItem
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.sp
import com.simple.ui.precompute.LayoutEngine
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.node.BackgroundNode
import com.simple.ui.precompute.node.ConstraintChild
import com.simple.ui.precompute.node.ConstraintNode
import com.simple.ui.precompute.node.Constraints
import com.simple.ui.precompute.node.EdgeInsets
import com.simple.ui.precompute.node.ImageNode
import com.simple.ui.precompute.node.LayoutDimension
import com.simple.ui.precompute.node.LinearNode
import com.simple.ui.precompute.node.Orientation

data class ClockHomeItem(
    val screenWidth: Int,
    val is24h: Boolean = false,
    val isAmPm: Boolean = false,
    val isSolar: Boolean = true,
    val isLunar: Boolean = false
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
                    children = buildList {
                        add(
                            TimeNode(
                                pattern = if (is24h) "HH:mm" else "hh:mm",
                                showAmPm = !is24h && isAmPm,
                                textSizePx = 48.sp(),
                                color = Color.BLACK,
                                isBold = true
                            )
                        )
                        if (isSolar) {
                            add(
                                TimeNode(
                                    pattern = "EEEE, MMM/dd/yyyy",
                                    textSizePx = 18.sp(),
                                    color = Color.parseColor("#666666")
                                )
                            )
                        }
                        if (isLunar) {
                            add(
                                LinearNode(
                                    orientation = Orientation.HORIZONTAL,
                                    padding = EdgeInsets(top = 8.dp().toInt()),
                                    children = listOf(
                                        ImageNode(
                                            source = BigImage(R.drawable.ic_moon),
                                            layoutWidth = LayoutDimension.Fixed(20.dp().toInt()),
                                            layoutHeight = LayoutDimension.Fixed(20.dp().toInt()),
                                            padding = EdgeInsets(right = 8.dp().toInt())
                                        ),
                                        TimeNode(
                                            pattern = "",
                                            isLunar = true,
                                            textSizePx = 20.sp(),
                                            color = Color.parseColor("#6C63FF"),
                                            isBold = true
                                        )
                                    )
                                )
                            )
                        }
                    },
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
        screenWidth to "screenWidth",
        is24h to "is24h",
        isAmPm to "isAmPm",
        isSolar to "isSolar",
        isLunar to "isLunar"
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
