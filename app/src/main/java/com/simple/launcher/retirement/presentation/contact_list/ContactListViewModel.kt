package com.simple.launcher.retirement.presentation.contact_list

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
import com.simple.launcher.retirement.utils.VietnameseStringUtils
import com.simple.launcher.retirement.utils.exts.colorOnPrimary
import com.simple.launcher.retirement.utils.exts.colorPrimary
import com.simple.launcher.retirement.utils.exts.colorSurface
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.mutableStateFlow
import com.simple.launcher.retirement.utils.exts.textColorPrimary
import com.simple.launcher.retirement.utils.exts.textColorSecondary
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.text.BigText
import com.simple.ui.precompute.text.toBig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * No-arg constructor để dùng được với `by viewModels()` mặc định (không cần Factory).
 * ContactRepository là singleton — đọc thẳng từ `.instance`.
 */
class ContactListViewModel : BaseViewModel() {

    // ── 1. Fields ─────────────────────────────────────────────────────────

    private val repository: ContactRepository = ContactRepository.instance

    // ── 2. Flows ──────────────────────────────────────────────────────────

    private val _contacts = MutableStateFlow<List<ContactEntity>>(emptyList())
    private val _query = MutableStateFlow("")

    // Khởi tạo rỗng rồi nạp từ flow trong background. Toggle sau đó cập nhật trực tiếp
    // _selectedIds — không cần re-collect flow.
    private val _selectedIds: MutableStateFlow<Set<String>> = mutableStateFlow(emptySet()) {

        value = repository.getSelectedContactsFlow().first().map { it.id }.toSet()
    }

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

    private val _preparedContacts: StateFlow<List<PreparedContactEntry>> = combineState(
        flow1 = _contacts,
        initialValue = emptyList()
    ) { contacts ->

        value = contacts.map { contact ->

            val photo = if (contact.photoUri != null) BigImage(contact.photoUri)
            else BigImage(R.drawable.ic_home_contact_24dp)
            PreparedContactEntry(
                contact = contact,
                lowerName = contact.name.lowercase(),
                normalizedName = VietnameseStringUtils.normalizeForSearch(contact.name),
                bigName = contact.name.toBig(),
                bigPhoto = photo
            )
        }
    }

    val items: StateFlow<List<SelectableContactItem>> = combineState(
        flow1 = _preparedContacts,
        flow2 = _query,
        flow3 = _selectedIds,
        initialValue = emptyList()
    ) { prepared, query, selectedIds ->

        val filtered = filterByQuery(prepared, query)
        value = filtered
            .sortedWith(compareBy({ it.second }, { it.first.lowerName }))
            .map { (entry, _) -> toSelectableContactItem(entry, selectedIds) }
    }

    // ── 3. Public API ─────────────────────────────────────────────────────

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

    /**
     * Lưu list contact đã chọn — giữ thứ tự cũ đã save + append contact mới ở cuối.
     * Trước đây `_contacts.value.filter { it.id in selectedIds }` giữ thứ tự
     * theo DISPLAY_NAME của ContactsProvider, làm mất thứ tự user đã sắp trên home.
     */
    suspend fun saveSelection() {

        val orderedIds = buildOrderedSelectedIds()
        val indexByOrder = orderedIds.withIndex().associate { it.value to it.index }
        val selected = _contacts.value
            .filter { indexByOrder.containsKey(it.id) }
            .sortedBy { indexByOrder.getValue(it.id) }
        repository.saveSelectedContacts(selected)
    }

    // ── 4. Private helpers ────────────────────────────────────────────────

    private fun filterByQuery(
        contacts: List<PreparedContactEntry>,
        query: String
    ): List<Pair<PreparedContactEntry, Int>> {

        if (query.isBlank()) return contacts.map { it to 0 }

        // Normalize query 1 lần thay vì mỗi item x 3 lần.
        val normalizedQuery = VietnameseStringUtils.normalizeForSearch(query)
        return contacts.mapNotNull { entry -> matchWithPriority(entry, normalizedQuery, query) }
    }

    private fun matchWithPriority(
        entry: PreparedContactEntry,
        normalizedQuery: String,
        rawQuery: String
    ): Pair<PreparedContactEntry, Int>? {

        val normalizedName = entry.normalizedName
        val phone = entry.contact.phoneNumber
        val priority = when {

            normalizedName == normalizedQuery -> 0
            normalizedName.startsWith(normalizedQuery) -> 1
            normalizedName.contains(normalizedQuery) -> 2
            phone.contains(rawQuery) -> 3
            else -> return null
        }
        return entry to priority
    }

    private fun toSelectableContactItem(
        entry: PreparedContactEntry,
        selectedIds: Set<String>
    ): SelectableContactItem {

        val isSelected = entry.contact.id in selectedIds
        return SelectableContactItem(
            name = entry.bigName,
            photo = entry.bigPhoto,
            isSelected = isSelected,
            entity = SelectableContactEntity(entry.contact, isSelected)
        )
    }

    // ── 5. Nested classes ─────────────────────────────────────────────────

    /**
     * Wrapper cache dữ liệu precomputed cho 1 contact — chỉ dựng lại khi list
     * _contacts thay đổi (thường 1 lần / màn hình), KHÔNG dựng mỗi keystroke như trước.
     */
    private data class PreparedContactEntry(
        val contact: ContactEntity,
        val lowerName: String,
        val normalizedName: String,
        val bigName: BigText,
        val bigPhoto: BigImage
    )
}
