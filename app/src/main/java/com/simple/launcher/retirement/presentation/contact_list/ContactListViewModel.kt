package com.simple.launcher.retirement.presentation.contact_list

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
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.colorSurface
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.mutableStateFlow
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.launcher.retirement.utils.VietnameseStringUtils
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.text.toBig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ContactListViewModel(
    private val repository: ContactRepository
) : BaseViewModel() {

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = resources,
        initialValue = ToolbarState.empty()
    ) { resources ->

        val color = resources.textColorPrimary
        value = ToolbarState(
            title = buildToolbarTitle(resources.getString(R.string.contact_list_title), color),
            backIcon = buildBackIcon(color)
        )
    }

    val searchState: StateFlow<SearchState> = combineState(
        flow1 = resources,
        initialValue = SearchState.empty()
    ) { resources ->

        val textColor = resources.textColorPrimary
        val hintColor = resources.textColorSecondary
        val backgroundColor = resources.colorSurface

        value = buildSearchState(
            hint = resources.getString(R.string.search),
            textColor = textColor,
            hintColor = hintColor,
            backgroundColor = backgroundColor
        )
    }

    val saveAction: StateFlow<ActionState> = combineState(
        flow1 = resources,
        initialValue = ActionState.empty()
    ) { resources ->

        val color = resources.colorOnPrimary
        val backgroundColor = resources.colorPrimary

        value = buildActionState(
            text = resources.getString(R.string.save),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }

    private val _contacts = MutableStateFlow<List<ContactEntity>>(emptyList())
    private val _query = MutableStateFlow("")

    // Khởi tạo rỗng rồi nạp từ flow trong background. Toggle sau đó cập nhật trực tiếp
    // _selectedIds — không cần re-collect flow.
    private val _selectedIds: MutableStateFlow<Set<String>> = mutableStateFlow(emptySet()) {

        value = repository.getSelectedContactsFlow().first().map { it.id }.toSet()
    }

    val items: StateFlow<List<SelectableContactItem>> = combineState(
        flow1 = _contacts,
        flow2 = _query,
        flow3 = _selectedIds,
        initialValue = emptyList()
    ) { contacts, query, selectedIds ->

        val filtered = filterByQuery(contacts, query)
        value = filtered
            .sortedWith(compareBy({ it.second }, { it.first.name.lowercase() }))
            .map { (contact, _) -> toSelectableContactItem(contact, selectedIds) }
    }

    private fun filterByQuery(
        contacts: List<ContactEntity>,
        query: String
    ): List<Pair<ContactEntity, Int>> {

        if (query.isBlank()) return contacts.map { it to 0 }
        return contacts.mapNotNull { contact -> matchWithPriority(contact, query) }
    }

    private fun matchWithPriority(
        contact: ContactEntity,
        query: String
    ): Pair<ContactEntity, Int>? {

        val name = contact.name
        val phone = contact.phoneNumber
        val priority = when {

            VietnameseStringUtils.equalsIgnoreDiacritics(name, query) -> 0
            VietnameseStringUtils.startsWithIgnoreDiacritics(name, query) -> 1
            VietnameseStringUtils.containsIgnoreDiacritics(name, query) -> 2
            phone.contains(query) -> 3
            else -> return null
        }
        return contact to priority
    }

    private fun toSelectableContactItem(
        contact: ContactEntity,
        selectedIds: Set<String>
    ): SelectableContactItem {

        val isSelected = contact.id in selectedIds
        val photo = if (contact.photoUri != null) BigImage(contact.photoUri)
        else BigImage(R.drawable.ic_home_contact_24dp)
        return SelectableContactItem(
            name = contact.name.toBig(),
            photo = photo,
            isSelected = isSelected,
            entity = SelectableContactEntity(contact, isSelected)
        )
    }

    fun search(text: String) {

        _query.value = text
    }

    fun loadContacts() {

        // Collect chỉ 1 lần: getAllContactsFlow đã tự cập nhật khi ContentObserver bắn thay đổi,
        // nhưng ở màn hình này ta chỉ cần snapshot 1 lần lúc mở.
        viewModelScope.launch {

            _contacts.value = repository.getAllContactsFlow().first()
        }
    }

    fun updateItem(entity: SelectableContactEntity) {

        val id = entity.contact.id
        val current = _selectedIds.value
        _selectedIds.value = if (id in current) {

            current - id
        } else {

            current + id
        }
    }

    fun saveSelection() {

        val selectedIds = _selectedIds.value
        val selected = _contacts.value.filter { it.id in selectedIds }
        repository.saveSelectedContacts(selected)
    }

    fun getAllSelectedIds(): Set<String> = _selectedIds.value

    /**
     * Xây danh sách contact id đã chọn theo đúng thứ tự cũ đã lưu:
     * - Giữ nguyên thứ tự các contact cũ vẫn đang chọn
     * - Contact mới toggle thêm sẽ nằm cuối
     */
    suspend fun buildOrderedSelectedIds(): List<String> {

        val currentSelected = _selectedIds.value
        val savedIds = repository.getSelectedContactsFlow().first().map { it.id }
        val ordered = savedIds.filter { it in currentSelected }.toMutableList()
        ordered.addAll(currentSelected.filter { it !in savedIds })
        return ordered
    }
}

class ContactListViewModelFactory(
    private val repository: ContactRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        return ContactListViewModel(repository) as T
    }
}
