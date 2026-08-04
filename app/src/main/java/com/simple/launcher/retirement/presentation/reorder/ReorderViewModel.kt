package com.simple.launcher.retirement.presentation.reorder

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.first

enum class ReorderType {

    APPS,
    CONTACTS
}

/**
 * Nhận tham số qua [SavedStateHandle] để `by viewModels()` mặc định
 * (SavedStateViewModelFactory) tự dựng được — không cần Factory riêng.
 *
 * Fragment.arguments được framework tự đổ vào SavedStateHandle theo key,
 * nên ReorderFragment chỉ cần put args vào Bundle như bình thường.
 * Repository là singleton, đọc thẳng từ `.instance`.
 */
class ReorderViewModel(
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    // ── 1. Fields ─────────────────────────────────────────────────────────

    val type: ReorderType = resolveType(savedStateHandle)
    private val initialIds: List<String> = savedStateHandle.get<ArrayList<String>?>(ARG_IDS) ?: emptyList()
    private val appRepository: AppRepository = AppRepository.instance
    private val contactRepository: ContactRepository = ContactRepository.instance

    // ── 2. Flows ──────────────────────────────────────────────────────────

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

    // ── 3. Public API ─────────────────────────────────────────────────────

    fun moveItem(from: Int, to: Int) {

        val list = items.value.toMutableList()
        val item = list.removeAt(from)
        list.add(to, item)
        items.currentValue = list
    }

    fun getFinalIds(): List<String> = items.value.map { it.id }

    fun getFinalContacts(): List<ContactEntity> = items.value.mapNotNull { it.data as? ContactEntity }

    // ── 4. Private helpers ────────────────────────────────────────────────

    private fun resolveType(handle: SavedStateHandle): ReorderType {

        // SavedStateHandle giữ nguyên object gốc (ReorderType enum) khi arguments
        // được set bằng putSerializable, nhưng cũng phải phòng trường hợp bị serialize
        // thành String (ví dụ khi restore từ process death).
        return when (val raw = handle.get<Any?>(ARG_TYPE)) {

            is ReorderType -> raw
            is String -> runCatching { ReorderType.valueOf(raw) }.getOrDefault(ReorderType.APPS)
            else -> ReorderType.APPS
        }
    }

    private suspend fun loadAppItems(): List<ReorderItem> {

        val allApps = appRepository.getAllAppFlow().first()
        val selectedApps = initialIds.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }
        return selectedApps.map { app -> toAppReorderItem(app) }
    }

    private fun toAppReorderItem(app: AppEntity): ReorderItem = ReorderItem(
        id = app.packageName,
        label = app.label
            .withStyleBodyLarge()
            .with(BigForegroundColor(resources.value.colorOnSurface))
            .build(),
        icon = BigImage(app.icon)
    )

    private suspend fun loadContactItems(): List<ReorderItem> {

        val allContacts = contactRepository.getAllContactsFlow().first()
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

    // ── 6. Companion object ───────────────────────────────────────────────

    companion object {

        // Key argument dùng chung giữa Fragment (put) và ViewModel (get qua SavedStateHandle).
        const val ARG_TYPE = "type"
        const val ARG_IDS = "ids"
    }
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
