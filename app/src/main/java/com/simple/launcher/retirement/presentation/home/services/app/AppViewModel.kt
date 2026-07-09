package com.simple.launcher.retirement.presentation.home.services.app

import android.graphics.Color
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.usecase.GetHomeAppsUseCase2
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.GroupViewItem
import com.simple.launcher.retirement.presentation.home.adapter.HeaderHomeItem
import com.simple.launcher.retirement.presentation.home.adapter.HomeItem
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.dp
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.sp
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.ui.precompute.LayoutEngine
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.node.BackgroundNode
import com.simple.ui.precompute.node.ConstraintChild
import com.simple.ui.precompute.node.ConstraintNode
import com.simple.ui.precompute.node.Constraints
import com.simple.ui.precompute.node.ImageNode
import com.simple.ui.precompute.node.LayoutDimension
import com.simple.ui.precompute.node.TextNode
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.span.BigTextSize
import com.simple.ui.precompute.text.toBuilder
import com.simple.ui.precompute.text.with
import kotlinx.coroutines.flow.StateFlow

class AppViewModel : BaseViewModel() {

    val appViewItemList: StateFlow<GroupViewItem?> = combineState(
        flow1 = resources,
        flow2 = GetHomeAppsUseCase2.instance.invoke(),
        initialValue = null
    ) { resources, apps ->

        value = buildAppGroup(resources = resources, apps = apps)
    }

    private fun buildAppGroup(
        resources: Map<String, Any>,
        apps: List<HomeContentEntity.App>
    ): GroupViewItem {

        val itemWidth = calculateItemWidth()
        val items = buildList<ViewItem> {

            buildHeader(resources)?.let(::add)
            addAll(apps.map { it.toViewItem(itemWidth = itemWidth, resources = resources) })
        }

        return GroupViewItem(order = 1, list = items)
    }

    private fun buildHeader(resources: Map<String, Any>): HeaderHomeItem? {

        val title = resources.getString(R.string.home_header_apps)
        if (title.isBlank()) {

            return null
        }

        return HeaderHomeItem(
            title = title
                .toBuilder()
                .with(BigTextSize(22), BigForegroundColor(Color.WHITE))
                .build()
        )
    }

    private fun calculateItemWidth(): Int {

        val screenWidth = android.content.res.Resources.getSystem().displayMetrics.widthPixels
        return (screenWidth - 72.dp().toInt()) / (HomeItem.TOTAL_COLUMNS / 2)
    }

    private fun HomeContentEntity.App.toViewItem(
        itemWidth: Int,
        resources: Map<String, Any>
    ): AppHomeItem = AppHomeItem(
        entity = entity,
        spec = LayoutEngine.measure(
            node = createAppCardNode(itemWidth = itemWidth, resources = resources),
            constraints = Constraints(maxWidth = itemWidth),
            id = entity.packageName
        )
    )

    // Node được đo sẵn trong ViewModel để adapter chỉ việc gán spec lên view.
    private fun HomeContentEntity.App.createAppCardNode(
        itemWidth: Int,
        resources: Map<String, Any>
    ): ConstraintNode = ConstraintNode(
        children = listOf(
            createBackgroundChild(itemWidth),
            createIconChild(),
            createLabelChild(resources)
        ),
        layoutWidth = LayoutDimension.MatchParent
    )

    private fun HomeContentEntity.App.createBackgroundChild(itemWidth: Int): ConstraintChild = ConstraintChild(
        id = "background",
        node = BackgroundNode(
            backgroundColor = Color.WHITE,
            cornerRadius = 24.dp()
        ),
        startToStartOf = ConstraintNode.PARENT,
        endToEndOf = ConstraintNode.PARENT,
        topToTopOf = ConstraintNode.PARENT,
        width = LayoutDimension.MatchParent,
        height = LayoutDimension.Fixed(itemWidth)
    )

    private fun HomeContentEntity.App.createIconChild(): ConstraintChild = ConstraintChild(
        id = "icon",
        node = ImageNode(
            source = BigImage(entity.icon),
            layoutWidth = LayoutDimension.Fixed(56.dp().toInt()),
            layoutHeight = LayoutDimension.Fixed(56.dp().toInt())
        ),
        startToStartOf = "background",
        endToEndOf = "background",
        topToTopOf = "background",
        bottomToBottomOf = "background"
    )

    private fun HomeContentEntity.App.createLabelChild(resources: Map<String, Any>): ConstraintChild = ConstraintChild(
        id = "label",
        node = TextNode(
            text = BigText(entity.label),
            textSizePx = 18.sp(),
            color = resources.textColorPrimary,
            maxLines = 1
        ),
        startToStartOf = ConstraintNode.PARENT,
        endToEndOf = ConstraintNode.PARENT,
        topToBottomOf = "background",
        marginTop = 12.dp().toInt(),
        marginBottom = 12.dp().toInt(),
        horizontalBias = 0.5f
    )
}
