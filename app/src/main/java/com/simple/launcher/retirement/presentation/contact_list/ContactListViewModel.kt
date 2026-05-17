package com.simple.launcher.retirement.presentation.contact_list

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.domain.model.SelectableContactEntity
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.SearchState
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildSearchState
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.image.ImagePath
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactListViewModel(
    private val repository: ContactRepository
) : BaseViewModel() {

    // Toolbar state — title với màu, size, font từ theme; backIcon với màu từ theme
    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = ToolbarState.empty()
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        ToolbarState(
            title = buildToolbarTitle(stringMap.getString(R.string.contact_list_title), color),
            backIcon = buildBackIcon(color)
        )
    }

    val searchState: StateFlow<SearchState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = SearchState.empty()
    ) { stringMap, themeMap ->
        val textColor = themeMap.getColor(android.R.attr.textColorPrimary)
        val hintColor = themeMap.getColor(android.R.attr.textColorSecondary, android.graphics.Color.GRAY)
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight, android.graphics.Color.LTGRAY)

        buildSearchState(
            hint = stringMap.getString(R.string.search),
            textColor = textColor,
            hintColor = hintColor,
            backgroundColor = backgroundColor
        )
    }

    val saveAction: StateFlow<ActionState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = ActionState.empty()
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight, android.graphics.Color.LTGRAY)

        buildActionState(
            text = stringMap.getString(R.string.save),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }

    // Nội bộ: domain entities
    private val _contacts = MutableStateFlow<List<SelectableContactEntity>>(emptyList())
    private val _query = MutableStateFlow("")

    // Expose ra ngoài: ViewItems đã được xử lý sẵn, adapter chỉ set data
    val items: StateFlow<List<SelectableContactItem>> = combineState(
        flow1 = _contacts,
        flow2 = _query,
        initialValue = emptyList()
    ) { contacts, query ->
        contacts.filter {
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
                // Chỉ lấy 4 columns cần thiết thay vì null (query toàn bộ columns)
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
                )
                val cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, null, null,
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
        val index = _contacts.value.indexOfFirst { it.contact.id == entity.contact.id }
        if (index == -1) return
        _contacts.value = _contacts.value.mapIndexed { i, item ->
            if (i == index) item.copy(isSelected = !item.isSelected) else item
        }
    }

    fun saveSelection() {
        val selected = _contacts.value.filter { it.isSelected }.map { it.contact }
        repository.saveSelectedContacts(selected)
    }
}

class ContactListViewModelFactory(private val repository: ContactRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ContactListViewModel(repository) as T
    }
}
