package com.simple.launcher.retirement.presentation.home.services.contact

import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.usecase.GetHomeContactUseCase
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.home.adapter.HeaderHomeItem
import com.simple.launcher.retirement.utils.combineState
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
import kotlinx.coroutines.flow.StateFlow

class ContactViewModel : BaseViewModel() {

    val contactViewItemList: StateFlow<GroupViewItem?> = combineState(
        flow1 = resources,
        flow2 = GetHomeContactUseCase.instance.invoke(),
        initialValue = null
    ) { resources, contacts ->

        value = buildContactGroup(resources = resources, contacts = contacts)
    }

    private fun buildContactGroup(
        resources: Map<String, Any>,
        contacts: List<HomeContentEntity.Contact>
    ): GroupViewItem {

        val textColor = resources.textColorPrimary
        val tapToCallLabel = resources.getString(R.string.contact_tap_to_call)
        val screenWidth = android.content.res.Resources.getSystem().displayMetrics.widthPixels - 2 * 12.dp().toInt()
        val items = buildList<ViewItem> {

            buildHeader(resources, screenWidth)?.let(::add)
            addAll(
                contacts.map {
                    it.toViewItem(
                        textColor = textColor,
                        tapToCallLabel = tapToCallLabel,
                        screenWidth = screenWidth
                    )
                }
            )
        }

        return GroupViewItem(order = 2, list = items)
    }

    private fun buildHeader(resources: Map<String, Any>, screenWidth: Int): HeaderHomeItem? {

        val title = resources.getString(R.string.home_header_contacts)
        if (title.isBlank()) {

            return null
        }

        return HeaderHomeItem(
            title = title,
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
        )
    }

    private fun HomeContentEntity.Contact.toViewItem(
        textColor: Int,
        tapToCallLabel: String,
        screenWidth: Int
    ): ContactHomeItem = ContactHomeItem(
        entity = entity,
        spec = LayoutEngine.measure(
            node = createContactCardNode(
                textColor = textColor,
                tapToCallLabel = tapToCallLabel
            ),
            constraints = Constraints(maxWidth = screenWidth / 2),
            id = entity.id
        )
    )

    private fun HomeContentEntity.Contact.createContactCardNode(
        textColor: Int,
        tapToCallLabel: String
    ): ConstraintNode = ConstraintNode(
        children = listOf(
            ConstraintChild(
                id = "background",
                node = BackgroundNode(
                    backgroundColor = Color.WHITE,
                    cornerRadius = 24.dp()
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
                    padding = EdgeInsets.all(16.dp().toInt()),
                    gap = 12.dp().toInt(),
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
        padding = EdgeInsets.symmetric(h = 8.dp().toInt(), v = 8.dp().toInt()),
        layoutWidth = LayoutDimension.MatchParent
    )

    private fun HomeContentEntity.Contact.createPhotoNode(): LayoutNode = ImageNode(
        source = BigImageBuilder(entity.photoUri ?: R.drawable.ic_home_contact_24dp)
            .addTransform(CircleCrop)
            .build(),
        layoutWidth = LayoutDimension.Fixed(80.dp().toInt()),
        layoutHeight = LayoutDimension.Fixed(80.dp().toInt())
    )

    private fun HomeContentEntity.Contact.createNameNode(textColor: Int): LayoutNode = TextNode(
        text = entity.name.toBuilder()
            .with(BigForegroundColor(textColor), BigTextSize(18.sp()), BigBold)
            .build(),
        maxLines = 1
    )

    private fun HomeContentEntity.Contact.createTapToCallNode(
        textColor: Int,
        tapToCallLabel: String
    ): LayoutNode = ConstraintNode(
        children = listOf(
            ConstraintChild(
                id = "tapToCallBackground",
                node = BackgroundNode(
                    backgroundColor = "#F0F0F0".toColorInt(),
                    cornerRadius = 24.dp()
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
                    gap = 8.dp().toInt(),
                    padding = EdgeInsets.symmetric(h = 16.dp().toInt(), v = 8.dp().toInt()),
                    children = listOf(
                        ImageNode(
                            source = BigImageBuilder(android.R.drawable.ic_menu_call)
                                .addTransform(ColorFilter(Color.BLACK))
                                .build(),
                            layoutWidth = LayoutDimension.Fixed(20.dp().toInt()),
                            layoutHeight = LayoutDimension.Fixed(20.dp().toInt())
                        ),
                        TextNode(
                            text = tapToCallLabel.toBuilder()
                                .with(BigForegroundColor(textColor), BigTextSize(14.sp()), BigBold)
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
}
