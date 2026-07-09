package com.simple.launcher.retirement.presentation.home.services.clock

import android.graphics.Color
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.utils.combineState
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
import kotlinx.coroutines.flow.StateFlow

class ClockViewModel : BaseViewModel() {

    val timeViewItemList: StateFlow<GroupViewItem?> = combineState(
        flow1 = resources,
        initialValue = null
    ) { resources ->

        val screenWidth = android.content.res.Resources.getSystem().displayMetrics.widthPixels

        val spec = LayoutEngine.measure(
            node = createClockCardNode(),
            constraints = Constraints(maxWidth = screenWidth - 2 * 12.dp().toInt()),
            id = "clock"
        )
        val list = listOf(ClockHomeItem(spec))

        value = GroupViewItem(0, list)
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
}
