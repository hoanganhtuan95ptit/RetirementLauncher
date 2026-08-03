package com.simple.launcher.retirement.presentation.reorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorOnSurface
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.mutableStateFlow
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.withStyleBodyLarge
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.build
import com.simple.ui.precompute.text.span.BigForegroundColor
import com.simple.ui.precompute.text.with
import kotlinx.coroutines.flow.StateFlow

enum class ReorderType {

    APPS,
    CONTACTS
}

class ReorderViewModel(
    val type: ReorderType,
    private val initialIds: List<String>,
    private val appRepository: AppRepository,
    private val contactRepository: ContactRepository
) : BaseViewModel() {

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = resources,
        initialValue = ToolbarState.empty()
    ) { resources ->

        val color = resources.textColorPrimary
        val titleRes = if (type == ReorderType.APPS) {

            R.string.reorder_apps_title
        } else {

            R.string.reorder_contacts_title
        }

        value = ToolbarState(
            title = buildToolbarTitle(resources.getString(titleRes), color),
            backIcon = buildBackIcon(color)
        )
    }

    val doneAction: StateFlow<ActionState> = combineState(
        flow1 = resources,
        initialValue = ActionState.empty()
    ) { resources ->

        val color = resources.colorOnPrimary
        val backgroundColor = resources.colorPrimary

        value = buildActionState(
            text = resources.getString(R.string.done),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }

    val items: StateFlow<List<ReorderItem>> = mutableStateFlow(emptyList()) {

        value = if (type == ReorderType.APPS) loadAppItems() else loadContactItems()
    }

    private fun loadAppItems(): List<ReorderItem> {

        val allApps = appRepository.getInstalledApps()
        val selectedApps = initialIds.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
        return selectedApps.map { app -> toAppReorderItem(app) }
    }

    private fun toAppReorderItem(app: com.simple.launcher.retirement.domain.model.AppEntity): ReorderItem = ReorderItem(
        id = app.packageName,
        label = app.label
            .withStyleBodyLarge()
            .with(BigForegroundColor(resources.value.colorOnSurface))
            .build(),
        icon = BigImage(app.icon)
    )

    private fun loadContactItems(): List<ReorderItem> {

        val allContacts = contactRepository.getAllContacts()
        val orderedContacts = initialIds.mapNotNull { id -> allContacts.find { it.id == id } }
        return orderedContacts.map { toContactReorderItem(it) }
    }

    private fun toContactReorderItem(contact: ContactEntity): ReorderItem {

        val photo = if (contact.photoUri != null) BigImage(contact.photoUri)
        else BigImage(R.drawable.ic_home_contact_24dp)

        return ReorderItem(
            id = contact.id,
            label = contact.name
                .withStyleBodyLarge()
                .with(BigForegroundColor(resources.value.colorOnSurface))
                .build(),
            icon = photo,
            data = contact
        )
    }

    fun moveItem(from: Int, to: Int) {

        val list = items.value.toMutableList()
        val item = list.removeAt(from)
        list.add(to, item)
        items.currentValue = list
    }

    fun getFinalIds(): List<String> = items.value.map { it.id }

    fun getFinalContacts(): List<ContactEntity> = items.value.mapNotNull { it.data as? ContactEntity }
}

data class ReorderItem(
    val id: String,
    val label: BigText,
    val icon: BigImage,
    val data: Any? = null
) : ViewItem {

    override fun areItemsTheSame(): List<Any> = listOf(id)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        label to "label",
        icon to "icon"
    )
}

class ReorderViewModelFactory(
    private val type: ReorderType,
    private val initialIds: List<String>,
    private val appRepository: AppRepository,
    private val contactRepository: ContactRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return ReorderViewModel(type, initialIds, appRepository, contactRepository) as T
    }
}
