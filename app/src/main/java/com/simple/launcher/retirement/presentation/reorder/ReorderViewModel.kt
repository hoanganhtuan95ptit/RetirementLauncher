package com.simple.launcher.retirement.presentation.reorder

import android.content.Context
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
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.getColor
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.image.ImageDrawable
import com.simple.launcher.retirement.utils.image.ImagePath
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.image.RichImage
import com.simple.launcher.retirement.utils.text.ForegroundColor
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.build
import com.simple.launcher.retirement.utils.text.with
import com.simple.launcher.retirement.utils.text.withStyleBodyLarge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ReorderType { APPS, CONTACTS }

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
        val color = resources.getColor(android.R.attr.textColorPrimary)
        val titleRes = if (type == ReorderType.APPS) R.string.reorder_apps_title else R.string.reorder_contacts_title
        ToolbarState(
            title = buildToolbarTitle(resources.getString(titleRes), color),
            backIcon = buildBackIcon(color)
        )
    }

    val doneAction: StateFlow<ActionState> = combineState(
        flow1 = resources,
        initialValue = ActionState.empty()
    ) { resources ->

        val color = resources.getColor(com.google.android.material.R.attr.colorOnPrimary)
        val backgroundColor = resources.getColor(android.R.attr.colorPrimary, android.graphics.Color.LTGRAY)

        buildActionState(
            text = resources.getString(R.string.done),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }

    private val _items = MutableStateFlow<List<ReorderItem>>(emptyList())
    val items: StateFlow<List<ReorderItem>> = _items

    fun loadItems(context: Context) {
        if (type == ReorderType.APPS) {
            val allApps = appRepository.getInstalledApps()
            val selectedApps = initialIds.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
            _items.value = selectedApps.map { app ->
                ReorderItem(
                    id = app.packageName,
                    label = app.label
                        .withStyleBodyLarge()
                        .with(ForegroundColor(resources.value.getColor(com.google.android.material.R.attr.colorOnSurface)))
                        .build(),
                    icon = ImageDrawable(app.icon)
                )
            }
        } else {
            val allContacts = contactRepository.getAllContacts(context)
            val orderedContacts = initialIds.mapNotNull { id -> allContacts.find { it.id == id } }
            
            _items.value = orderedContacts.map { contact ->
                val photo = if (contact.photoUri != null) {
                    ImagePath(contact.photoUri)
                } else {
                    ImageRes(R.drawable.ic_home_contact_24dp)
                }
                ReorderItem(
                    id = contact.id,
                    label = contact.name
                        .withStyleBodyLarge()
                        .with(ForegroundColor(resources.value.getColor(com.google.android.material.R.attr.colorOnSurface)))
                        .build(),
                    icon = photo,
                    data = contact
                )
            }
        }
    }

    fun moveItem(from: Int, to: Int) {
        val list = _items.value.toMutableList()
        val item = list.removeAt(from)
        list.add(to, item)
        _items.value = list
    }

    fun getFinalIds(): List<String> = _items.value.map { it.id }

    fun getFinalContacts(): List<ContactEntity> = _items.value.mapNotNull { it.data as? ContactEntity }
}

data class ReorderItem(
    val id: String,
    val label: RichText,
    val icon: RichImage,
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
