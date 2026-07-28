package com.simple.launcher.retirement.presentation.home.services.app

import android.graphics.Color
import com.simple.adapter.Adapter
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.adapters.PrecomputedAdapter
import com.simple.launcher.retirement.presentation.base.adapters.PrecomputedViewItem
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem.Companion.TOTAL_COLUMNS
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
import com.simple.ui.precompute.node.TextNode
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigBold
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.span.BigTextSize
import com.simple.ui.precompute.text.toBuilder
import com.simple.ui.precompute.text.with

data class AppHomeItem(
    val entity: AppEntity,
    val screenWidth: Int
) : PrecomputedViewItem(), HomeItem {

    override val spanSize: Int = TOTAL_COLUMNS / 3

    override fun areItemsTheSame(): List<Any> = listOf(
        entity.packageName
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        entity to "entity",
        screenWidth to "screenWidth"
    )

    override fun buildDrawSpec(resources: Map<String, Any>) {
        spec = LayoutEngine.measure(
            node = createAppCardNode(itemWidth = screenWidth / 3 - 2 * 8.dp(), resources = resources),
            constraints = Constraints(maxWidth = screenWidth / 3),
            id = entity.packageName
        )
    }

    private fun createAppCardNode(
        itemWidth: Int,
        resources: Map<String, Any>
    ): ConstraintNode = ConstraintNode(
        children = listOf(
            createBackgroundChild(itemWidth),
            createIconChild(),
            createLabelChild(resources)
        ),
        padding = EdgeInsets.symmetric(h = 8.dp(), v = 8.dp()),
        layoutWidth = LayoutDimension.MatchParent
    )

    private fun createBackgroundChild(itemWidth: Int): ConstraintChild = ConstraintChild(
        id = "background",
        node = BackgroundNode(
            backgroundColor = Color.WHITE,
            cornerRadius = 24.dp().toFloat()
        ),
        startToStartOf = ConstraintNode.PARENT,
        endToEndOf = ConstraintNode.PARENT,
        topToTopOf = ConstraintNode.PARENT,
        width = LayoutDimension.MatchParent,
        height = LayoutDimension.Fixed(itemWidth)
    )

    private fun createIconChild(): ConstraintChild = ConstraintChild(
        id = "icon",
        node = ImageNode(
            source = BigImage(entity.icon),
            layoutWidth = LayoutDimension.Fixed(56.dp()),
            layoutHeight = LayoutDimension.Fixed(56.dp())
        ),
        startToStartOf = "background",
        endToEndOf = "background",
        topToTopOf = "background",
        bottomToBottomOf = "background"
    )

    private fun createLabelChild(resources: Map<String, Any>): ConstraintChild = ConstraintChild(
        id = "label",
        node = TextNode(
            text = entity.label.toBuilder()
                .with(BigBold, BigTextSize(18.sp().toFloat()), BigForegroundColor(Color.WHITE))
                .build(),
            maxLines = 1
        ),
        startToStartOf = ConstraintNode.PARENT,
        endToEndOf = ConstraintNode.PARENT,
        topToBottomOf = "background",
        marginTop = 12.dp(),
        marginBottom = 12.dp(),
        horizontalBias = 0.5f
    )
}

@Adapter
class AppAdapter : PrecomputedAdapter<AppHomeItem>() {

    override val viewItemClass: Class<AppHomeItem> by lazy {

        AppHomeItem::class.java
    }

    override fun onItemCLick(item: AppHomeItem) {
        sendDeeplink(DeepLinks.APP, mapOf("entity" to item.entity))
    }
}
