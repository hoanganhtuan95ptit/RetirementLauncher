package com.simple.launcher.retirement.presentation.contact_list

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.domain.model.SelectableContactEntity
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.utils.string.VietnameseStringUtils
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.SearchState
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildSearchState
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.colorSurface
import com.simple.launcher.retirement.utils.image.ImagePath
import com.simple.launcher.retirement.utils.image.ImageRes
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
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
        flow1 = resources,
        initialValue = ToolbarState.empty()
    ) { resources ->
        val color = resources.textColorPrimary
        ToolbarState(
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

        buildSearchState(
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

        buildActionState(
            text = resources.getString(R.string.save),
            textColor = color,
            backgroundColor = backgroundColor
        )
    }

    // Nội bộ: danh sách contact thuần (không chứa trạng thái selected)
    private val _contacts = MutableStateFlow<List<ContactEntity>>(emptyList())
    private val _query = MutableStateFlow("")

    // Tách riêng trạng thái selected — độc lập với search query
    // Khởi tạo từ danh sách đã lưu trong cache
    private val _selectedIds = MutableStateFlow<Set<String>>(
        repository.getSelectedContacts().map { it.id }.toSet()
    )

    // Expose ra ngoài: ViewItems đã được xử lý sẵn, adapter chỉ set data
    val items: StateFlow<List<SelectableContactItem>> = combineState(
        flow1 = _contacts,
        flow2 = _query,
        flow3 = _selectedIds,
        initialValue = emptyList()
    ) { contacts, query, selectedIds ->
        val filtered = if (query.isBlank()) {
            // Không có query → trả về toàn bộ, giữ thứ tự gốc (A-Z)
            contacts.map { it to 0 }
        } else {
            // Lọc + gán priority: 0 = khớp chính xác tên, 1 = tên bắt đầu bằng query,
            // 2 = tên chứa query, 3 = SĐT chứa query
            contacts.mapNotNull { contact ->
                val name = contact.name
                val phone = contact.phoneNumber

                val priority = when {
                    // Ưu tiên cao nhất: tên khớp chính xác (có hoặc không dấu)
                    VietnameseStringUtils.equalsIgnoreDiacritics(name, query) -> 0
                    // Tên bắt đầu bằng query
                    VietnameseStringUtils.startsWithIgnoreDiacritics(name, query) -> 1
                    // Tên chứa query (ở giữa/cuối)
                    VietnameseStringUtils.containsIgnoreDiacritics(name, query) -> 2
                    // SĐT chứa query (tìm theo số)
                    phone.contains(query) -> 3
                    else -> return@mapNotNull null
                }
                contact to priority
            }
        }

        filtered
            .sortedWith(compareBy({ it.second }, { it.first.name.lowercase() }))
            .map { (contact, _) ->
                val isSelected = contact.id in selectedIds
                val photo = if (contact.photoUri != null) {
                    ImagePath(contact.photoUri)
                } else {
                    ImageRes(R.drawable.ic_home_contact_24dp)
                }
                SelectableContactItem(
                    name = RichText(contact.name),
                    photo = photo,
                    isSelected = isSelected,
                    entity = SelectableContactEntity(contact, isSelected)
                )
            }
    }

    fun search(text: String) {
        _query.value = text
    }

    fun loadContacts(context: Context) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.getAllContacts(context)
            }
            _contacts.value = result
        }
    }

    // Toggle trạng thái selected — chỉ thao tác trên _selectedIds
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

    /** Trả về tất cả contact ID đang được chọn (không phụ thuộc search query) */
    fun getAllSelectedIds(): Set<String> = _selectedIds.value
}

class ContactListViewModelFactory(private val repository: ContactRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ContactListViewModel(repository) as T
    }
}
