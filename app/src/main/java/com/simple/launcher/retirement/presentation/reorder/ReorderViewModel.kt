package com.simple.launcher.retirement.presentation.reorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.AppEntity
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
import com.simple.launcher.retirement.utils.image.ImageDrawable
import com.simple.launcher.retirement.utils.image.ImagePath
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.image.RichImage
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.theme.getColor
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
        flow1 = strings,
        flow2 = themes,
        initialValue = ToolbarState.empty()
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        val titleRes = if (type == ReorderType.APPS) R.string.reorder_apps_title else R.string.reorder_contacts_title
        ToolbarState(
            title = buildToolbarTitle(stringMap.getString(titleRes), color),
            backIcon = buildBackIcon(color)
        )
    }

    val doneAction: StateFlow<ActionState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = ActionState.empty()
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight, android.graphics.Color.LTGRAY)

        buildActionState(
            text = stringMap.getString(R.string.done),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }

    private val _items = MutableStateFlow<List<ReorderItem>>(emptyList())
    val items: StateFlow<List<ReorderItem>> = _items

    fun loadItems() {
        if (type == ReorderType.APPS) {
            val allApps = appRepository.getInstalledApps()
            val selectedApps = initialIds.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
            _items.value = selectedApps.map { app ->
                ReorderItem(
                    id = app.packageName,
                    label = app.label.toRich(),
                    icon = ImageDrawable(app.icon)
                )
            }
        } else {
            // For contacts, we can't easily get all by ID from the repo yet.
            // But wait, ContactRepositoryImpl has a way to load from DB.
            // For now, let's assume we pass IDs and the Fragment/ViewModel can handle it.
            // Actually, if we are in ContactListFragment, we have the full entities.
            // To simplify, let's assume ReorderFragment is told what to display.
            
            // For now, I'll just use the already selected ones from repo as a fallback,
            // but ideally we pass the current selection.
            val selectedContacts = contactRepository.getSelectedContacts()
            // Map initialIds to entities if possible, or just use repo if they match.
            val orderedContacts = initialIds.mapNotNull { id -> selectedContacts.find { it.id == id } }
            
            _items.value = orderedContacts.map { contact ->
                val photo = if (contact.photoUri != null) {
                    ImagePath(contact.photoUri)
                } else {
                    ImageRes(R.drawable.ic_home_contact_24dp)
                }
                ReorderItem(
                    id = contact.id,
                    label = contact.name.toRich(),
                    icon = photo
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
}

data class ReorderItem(
    val id: String,
    val label: RichText,
    val icon: RichImage
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
