package com.simple.launcher.retirement.presentation.home.services.contact

import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.simple.adapter.Adapter
import com.simple.deeplink.sendDeeplink
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.adapters.PrecomputedAdapter
import com.simple.launcher.retirement.presentation.base.adapters.PrecomputedViewItem
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.sp
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.ui.precompute.LayoutEngine
import com.simple.ui.precompute.image.BigImageBuilder
import com.simple.ui.precompute.image.CircleCrop
import com.simple.ui.precompute.image.ColorFilter
import com.simple.ui.precompute.image.addTransform
import com.simple.ui.precompute.image.build
import com.simple.ui.precompute.node.BackgroundNode
import com.simple.ui.precompute.node.ConstraintChild
import com.simple.ui.precompute.node.ConstraintNode
import com.simple.ui.precompute.node.Constraints
import com.simple.ui.precompute.node.CrossAlign
import com.simple.ui.precompute.node.EdgeInsets
import com.simple.ui.precompute.node.ImageNode
import com.simple.ui.precompute.node.LayoutDimension
import com.simple.ui.precompute.node.LayoutNode
import com.simple.ui.precompute.node.LinearNode
import com.simple.ui.precompute.node.Orientation
import com.simple.ui.precompute.node.TextNode
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigBold
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.span.BigTextSize
import com.simple.ui.precompute.text.toBuilder
import com.simple.ui.precompute.text.with

data class ContactHomeItem(
    val entity: ContactEntity,
    val screenWidth: Int
) : PrecomputedViewItem(), HomeItem {

    override val spanSize: Int = HomeItem.TOTAL_COLUMNS / 2 // half width

    override fun buildDrawSpec(resources: Map<String, Any>) {
        val textColor = resources.textColorPrimary
        val tapToCallLabel = resources.getString(R.string.contact_tap_to_call)

        spec = LayoutEngine.measure(
            node = createContactCardNode(
                textColor = textColor,
                tapToCallLabel = tapToCallLabel
            ),
            constraints = Constraints(maxWidth = screenWidth / 2),
            id = entity.id
        )
    }

    private fun createContactCardNode(
        textColor: Int,
        tapToCallLabel: String
    ): ConstraintNode = ConstraintNode(
        children = listOf(
            ConstraintChild(
                id = "background",
                node = BackgroundNode(
                    backgroundColor = Color.WHITE,
                    cornerRadius = 24.dp().toFloat()
                ),
                startToStartOf = "content",
                endToEndOf = "content",
                topToTopOf = "content",
                bottomToBottomOf = "content",
                width = LayoutDimension.MatchParent,
                height = LayoutDimension.MatchParent
            ),
            ConstraintChild(
                id = "content",
                node = LinearNode(
                    orientation = Orientation.VERTICAL,
                    crossAlign = CrossAlign.CENTER,
                    padding = EdgeInsets.all(16.dp()),
                    gap = 12.dp(),
                    children = listOf(
                        createPhotoNode(),
                        createNameNode(textColor),
                        createTapToCallNode(textColor, tapToCallLabel)
                    )
                ),
                startToStartOf = ConstraintNode.PARENT,
                endToEndOf = ConstraintNode.PARENT,
                topToTopOf = ConstraintNode.PARENT,
                width = LayoutDimension.MatchParent
            )
        ),
        padding = EdgeInsets.symmetric(h = 8.dp(), v = 8.dp()),
        layoutWidth = LayoutDimension.MatchParent
    )

    private fun createPhotoNode(): LayoutNode = ImageNode(
        source = BigImageBuilder(entity.photoUri ?: R.drawable.ic_home_contact_24dp)
            .addTransform(CircleCrop)
            .build(),
        layoutWidth = LayoutDimension.Fixed(80.dp()),
        layoutHeight = LayoutDimension.Fixed(80.dp())
    )

    private fun createNameNode(textColor: Int): LayoutNode = TextNode(
        text = entity.name.toBuilder()
            .with(BigForegroundColor(textColor), BigTextSize(18.sp().toFloat()), BigBold)
            .build(),
        maxLines = 1
    )

    private fun createTapToCallNode(
        textColor: Int,
        tapToCallLabel: String
    ): LayoutNode = ConstraintNode(
        children = listOf(
            ConstraintChild(
                id = "tapToCallBackground",
                node = BackgroundNode(
                    backgroundColor = "#F0F0F0".toColorInt(),
                    cornerRadius = 24.dp().toFloat()
                ),
                startToStartOf = "tapToCallContent",
                endToEndOf = "tapToCallContent",
                topToTopOf = "tapToCallContent",
                bottomToBottomOf = "tapToCallContent",
                width = LayoutDimension.MatchParent,
                height = LayoutDimension.MatchParent
            ),
            ConstraintChild(
                id = "tapToCallContent",
                node = LinearNode(
                    orientation = Orientation.HORIZONTAL,
                    crossAlign = CrossAlign.CENTER,
                    gap = 8.dp(),
                    padding = EdgeInsets.symmetric(h = 16.dp(), v = 8.dp()),
                    children = listOf(
                        ImageNode(
                            source = BigImageBuilder(android.R.drawable.ic_menu_call)
                                .addTransform(ColorFilter(Color.BLACK))
                                .build(),
                            layoutWidth = LayoutDimension.Fixed(20.dp()),
                            layoutHeight = LayoutDimension.Fixed(20.dp())
                        ),
                        TextNode(
                            text = tapToCallLabel.toBuilder()
                                .with(BigForegroundColor(textColor), BigTextSize(14.sp().toFloat()), BigBold)
                                .build(),
                            maxLines = 1
                        )
                    )
                ),
                startToStartOf = ConstraintNode.PARENT,
                endToEndOf = ConstraintNode.PARENT,
                topToTopOf = ConstraintNode.PARENT
            )
        ),
        layoutWidth = LayoutDimension.WrapContent,
        layoutHeight = LayoutDimension.WrapContent
    )

    override fun areItemsTheSame(): List<Any> = listOf(
        entity.id
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        entity to "entity",
        screenWidth to "screenWidth"
    )
}

@Adapter
class ContactAdapter : PrecomputedAdapter<ContactHomeItem>() {

    override val viewItemClass: Class<ContactHomeItem> by lazy {

        ContactHomeItem::class.java
    }

    override fun onItemCLick(item: ContactHomeItem) {
        sendDeeplink(DeepLinks.CALL, mapOf("entity" to item.entity))
    }
}
