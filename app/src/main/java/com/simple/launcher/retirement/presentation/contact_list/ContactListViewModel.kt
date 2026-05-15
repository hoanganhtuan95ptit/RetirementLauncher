package com.simple.launcher.retirement.presentation.contact_list

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.domain.model.SelectableContactEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.SearchState
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildSearchState
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.image.ImagePath
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactListViewModel(
    private val repository: AppRepository
) : BaseViewModel() {

    // Toolbar state — title với màu, size, font từ theme; backIcon với màu từ theme
    val toolbar: StateFlow<ToolbarState> = combine(strings, themes) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        val title = buildToolbarTitle(stringMap.getString(R.string.contact_list_title), color)
        ToolbarState(title = title, backIcon = buildBackIcon(color))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ToolbarState.empty())

    val searchState: StateFlow<SearchState> = combine(strings, themes) { stringMap, themeMap ->
        val textColor = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        val hintColor = themeMap.getColor(android.R.attr.textColorSecondary) ?: android.graphics.Color.GRAY
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight) ?: android.graphics.Color.LTGRAY

        buildSearchState(
            hint = stringMap.getString(R.string.search),
            textColor = textColor,
            hintColor = hintColor,
            backgroundColor = backgroundColor
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SearchState.empty())

    val saveAction: StateFlow<ActionState> = combine(strings, themes) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: android.graphics.Color.BLACK
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight) ?: android.graphics.Color.LTGRAY

        buildActionState(
            text = stringMap.getString(R.string.save),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ActionState.empty())

    // Nội bộ: domain entities
    private val _contacts = MutableStateFlow<List<SelectableContactEntity>>(emptyList())
    private val _query = MutableStateFlow("")

    // Expose ra ngoài: ViewItems đã được xử lý sẵn, adapter chỉ set data
    val items: StateFlow<List<SelectableContactItem>> = combine(_contacts, _query) { contacts, query ->
        filterContacts(contacts, query)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private fun filterContacts(entities: List<SelectableContactEntity>, query: String): List<SelectableContactItem> {
        return entities.filter {
            query.isBlank() || it.contact.name.contains(query, ignoreCase = true)
        }.map { entity ->
            val photo = if (entity.contact.photoUri != null) {
                ImagePath(entity.contact.photoUri)
            } else {
                ImageRes(R.drawable.ic_home_contact_24dp)
            }
            SelectableContactItem(
                name = entity.contact.name.toRich(),
                photo = photo,
                isSelected = entity.isSelected,
                entity = entity
            )
        }
    }

    fun search(text: String) {
        _query.value = text
    }

    fun loadContacts(context: Context) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val selectedIds = repository.getSelectedContacts().map { it.id }.toSet()
                val contentResolver = context.contentResolver
                val cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )

                val contactsList = mutableListOf<SelectableContactEntity>()
                cursor?.use {
                    val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                    val processedIds = mutableSetOf<String>()

                    while (it.moveToNext()) {
                        val id = it.getString(idIndex)
                        if (processedIds.contains(id)) continue

                        val name = it.getString(nameIndex)
                        val number = it.getString(numberIndex)
                        val photoUri = it.getString(photoIndex)

                        val contact = ContactEntity(id, name, number, photoUri)
                        contactsList.add(SelectableContactEntity(contact, selectedIds.contains(id)))
                        processedIds.add(id)
                    }
                }
                contactsList
            }
            _contacts.value = result
        }
    }

    // Nhận entity từ EventBus (adapter gửi nguyên entity, không toggle), ViewModel xử lý toggle
    fun updateItem(entity: SelectableContactEntity) {
        val currentList = _contacts.value.toMutableList()
        val index = currentList.indexOfFirst { it.contact.id == entity.contact.id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isSelected = !currentList[index].isSelected)
            _contacts.value = currentList
        }
    }

    fun saveSelection() {
        val selected = _contacts.value.filter { it.isSelected }.map { it.contact }
        repository.saveSelectedContacts(selected)
    }
}

class ContactListViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ContactListViewModel(repository) as T
    }
}
