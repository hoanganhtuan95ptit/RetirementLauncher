package com.simple.launcher.retirement.presentation.clean_memory

import android.graphics.Color
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.RamInfo
import com.simple.launcher.retirement.domain.repository.MemoryRepository
import com.simple.launcher.retirement.presentation.base.ActionState
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.presentation.base.ToolbarState
import com.simple.launcher.retirement.presentation.base.buildActionState
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.string.asStringRes
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.RichText
import com.simple.launcher.retirement.utils.text.toRich
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class BoostState { IDLE, BOOSTING, DONE }

/** Bọc RamInfo kèm flag animate để Fragment quyết định animation gauge. */
data class RamUpdate(val info: RamInfo, val animate: Boolean)

class CleanMemoryViewModel : BaseViewModel() {

    private val repository = MemoryRepository.instance

    // ── Toolbar ──────────────────────────────────────────────────────────────

    val toolbar: StateFlow<ToolbarState> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = ToolbarState.empty()
    ) { stringMap, themeMap ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        ToolbarState(
            title = buildToolbarTitle(stringMap.getString(R.string.clean_memory_title), color),
            backIcon = buildBackIcon(color)
        )
    }

    // ── RAM state ─────────────────────────────────────────────────────────────

    private val _ramUpdate = MutableStateFlow<RamUpdate?>(null)
    val ramUpdate: StateFlow<RamUpdate?> = _ramUpdate

    // ── Boost button state ────────────────────────────────────────────────────

    private val _boostState = MutableStateFlow(BoostState.IDLE)
    val boostState: StateFlow<BoostState> = _boostState

    private val _actionRes = MutableStateFlow(R.string.clean_memory_start)

    val action: StateFlow<ActionState> = combineState(
        flow1 = strings,
        flow2 = themes,
        flow3 = _actionRes,
        initialValue = ActionState.empty()
    ) { stringMap, themeMap, actionRes ->
        val color = themeMap.getColor(android.R.attr.textColorPrimary)
        val bgColor = themeMap.getColor(android.R.attr.colorControlHighlight, Color.LTGRAY)
        buildActionState(
            text = stringMap.getString(actionRes),
            textColor = color,
            backgroundColor = bgColor
        )
    }

    // ── Result title ──────────────────────────────────────────────────────────

    private val _freedMB = MutableStateFlow<Long?>(null)

    val resultTitle: StateFlow<RichText?> = combineState(
        flow1 = strings,
        flow2 = _freedMB,
        initialValue = null
    ) { stringMap, mb ->
        if (mb == null) null
        else if (mb > 0) {
            String.format(stringMap.getString(R.string.clean_memory_toast), mb.toInt()).toRich()
        } else {
            stringMap.getString(R.string.clean_memory_optimal).toRich()
        }
    }

    // ── Toast event ───────────────────────────────────────────────────────────

    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastEvent: SharedFlow<String> = _toastEvent

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        // Load RAM info ngay khi ViewModel khởi tạo, không cần Fragment gọi thủ công
        viewModelScope.launch(Dispatchers.IO) {
            val info = repository.getRamInfo()
            _ramUpdate.value = RamUpdate(info, animate = false)
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    fun startBoost() {
        if (_boostState.value == BoostState.BOOSTING) return

        _freedMB.value = null  // ẩn result card cũ
        _boostState.value = BoostState.BOOSTING
        _actionRes.value = R.string.clean_memory_running

        viewModelScope.launch {
            val freedBytes = withContext(Dispatchers.IO) {
                val result = repository.cleanMemory()
                delay(2000)
                result
            }

            // Refresh RAM info sau boost, animate gauge
            val updatedInfo = withContext(Dispatchers.IO) { repository.getRamInfo() }
            _ramUpdate.value = RamUpdate(updatedInfo, animate = true)

            val freedMB = freedBytes / (1024 * 1024)
            _freedMB.value = freedMB
            if (freedMB > 0) {
                val template = R.string.clean_memory_toast.asStringRes()
                if (template.isNotEmpty()) {
                    _toastEvent.emit(String.format(template, freedMB.toInt()))
                }
            }

            _boostState.value = BoostState.DONE
            _actionRes.value = R.string.clean_memory_retry
        }
    }
}
