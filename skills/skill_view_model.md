# Skill: Sử dụng ViewModel và Flow

## Mục đích

Đảm bảo code trong ViewModel ngắn gọn, dễ đọc và tuân thủ pattern quản lý state thống nhất bằng cách sử dụng các extension functions có sẵn.

## Quy tắc 1 — Ưu tiên dùng `combineState` để kết hợp các Flow thành StateFlow

Khi cần kết hợp `Flow` (dữ liệu từ repository, stringMap, themeMap...) thành một `StateFlow` để expose ra View, **ưu tiên dùng `combineState`** thay vì `combine(...).stateIn(...)` thủ công.

**Lý do:**
- Giảm boilerplate code.
- Đảm bảo `SharingStarted.Eagerly` được sử dụng thống nhất.
- Tự động quản lý `viewModelScope`.

### Các phiên bản hỗ trợ
`ViewModelExt.kt` hỗ trợ kết hợp từ 2 đến 5 flows.

```kotlin
// ✅ Khuyến khích — dùng combineState
val items: StateFlow<List<ViewItem>> = combineState(
    flow1 = flowA,
    flow2 = flowB,
    initialValue = emptyList()
) { a, b ->
    // transform logic
}

// ⚠️ Chấp nhận được khi cần linh hoạt hơn (e.g., combine flow không liên quan đến strings/themes)
val items: StateFlow<List<SelectableAppItem>> = combine(_apps, _query) { apps, query ->
    // transform logic
}.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
```

> **Lưu ý thực tế**: Một số ViewModel trong dự án (như `AppListViewModel`) vẫn dùng `combine().stateIn()` trực tiếp — đây là code cũ. Khi viết ViewModel mới hoặc refactor, hãy dùng `combineState`.

## Quy tắc 2 — Xử lý dữ liệu (Transform) hoàn toàn trong ViewModel

Tuân thủ Quy tắc 3 của [Skill Adapter](skill_adapter.md): Toàn bộ xử lý giao diện (`.toRich()`, `ImageDrawable()`, conditional logic...) phải nằm trong ViewModel.

```kotlin
val appHomeItems: StateFlow<Pair<Double, List<ViewItem>>> = combineState(
    flow1 = strings,
    flow2 = getHomeAppsUseCase.asFlow(),
    initialValue = 3.0 to emptyList()
) { strings, entities ->
    val list = entities.map { 
        AppHomeItem(
            label = it.entity.label.toBig(), // ✅ Transform ngay tại đây
            icon = BigImage(it.entity.icon),
            entity = it.entity
        ) 
    }
    3.0 to list
}
```

## Quy tắc 3 — Sử dụng BaseViewModel cho UI common state

Tất cả ViewModels nên kế thừa `BaseViewModel` để tận dụng:
- `strings`: Flow chứa toàn bộ string resources.
- `themes`: Flow chứa toàn bộ theme colors.
- `background`: `StateFlow<Background>` — màu nền toàn màn hình từ theme. Fragment observe và gọi `binding.root.setBackground(background)`.
- `bottomSheet`: `StateFlow<BottomSheetState>` — `BaseBottomSheetDialogFragment` tự observe; ViewModel con có thể override.
- `buildToolbarTitle(...)`, `buildBackIcon(...)`, `buildActionState(...)`, `buildSearchState(...)`: Các hàm helper để tạo `BigText`/`BigImage`/`ActionState` chuẩn UI.

```kotlin
class HomeViewModel(...) : BaseViewModel() {
    // strings, themes, background, bottomSheet đã có sẵn từ BaseViewModel
}
```

## Quy tắc 4 — Quản lý Item động bằng `_itemMap` và `updateItem`

Đối với các màn hình có danh sách item phức tạp hoặc có các thành phần có thể cập nhật độc lập, sử dụng `Map<Double, List<ViewItem>>` để quản lý theo trọng số (`order`).

```kotlin
private val _itemMap = MutableStateFlow<Map<Double, List<ViewItem>>>(
    mapOf(0.0 to listOf(ClockHomeItem)) // Khởi tạo với item mặc định
)

// Hàm update linh hoạt
fun updateItem(order: Double, list: List<ViewItem>) {
    _itemMap.value = _itemMap.value.toMutableMap().apply {
        put(order, list)
    }
}

// Combine cùng các flow khác
val items = combineState(..., flow5 = _itemMap, ...) { ..., extraMap ->
    (extraMap.toList() + listOf(...))
        .sortedBy { it.first }
        .flatMap { it.second }
}
```

## Quy tắc 5 — Dùng `mutableSharedFlow` / `combineSources` / `listenerSources` cho logic phức tạp

`ViewModel.kt` cung cấp thêm các extension function cho các trường hợp cần control flow thủ công hơn (ví dụ: chờ event từ EventBus, cancel job cũ khi source thay đổi).

| Hàm | Khi nào dùng |
|---|---|
| `mutableSharedFlow<T> { ... }` | Tạo `MutableSharedFlow<T>` chạy một coroutine nền khi khởi tạo. |
| `launchState` | Tạo `StateFlow<R>` bằng cách cho phép `emit` hoặc logic phức tạp trong lambda (dùng `MutableStateFlow` receiver). |
| `combineSources(vararg flows) { ... }` | Re-execute khi **bất kỳ** source flow nào emit — cancel job cũ. |
| `listenerSources(vararg flows) { ... }` | Tương tự `combineSources` nhưng dùng `merge` (một flow emit là chạy). Thường dùng với EventBus. |
| `combineSourcesWithDiff` / `listenerSourcesWithDiff` | Giống trên nhưng chỉ emit khi giá trị **thực sự thay đổi** (`distinctUntilChanged`). |

```kotlin
// Ví dụ: launchState để handle logic phức tạp
val state: StateFlow<MyState> = launchState(flow1, initialValue) { value ->
    // Perform complex logic, side effects, or multiple emits
    emit(process(value))
}

// Ví dụ: listenerSources để phản ứng với EventBus
val result: MutableSharedFlow<String> = listenerSources(AppListEventBus.events) {
    val entity = AppListEventBus.events.first()
    emit(processEntity(entity))
}

// Ví dụ: combineSources để re-compute khi nhiều nguồn thay đổi
val processed: MutableSharedFlow<List<ViewItem>> = combineSources(strings, themes, _dataFlow) {
    val stringMap = strings.first()
    val themeMap = themes.first()
    val data = _dataFlow.first()
    emit(data.map { buildViewItem(it, stringMap, themeMap) })
}
```

> **Lưu ý**: `mutableSharedFlow` có `replay = 1` — subscriber mới sẽ nhận ngay giá trị cuối cùng (khác với `EventBus` không có replay).

## Import cần thiết

```kotlin
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.launchState
import com.simple.launcher.retirement.utils.mutableSharedFlow
import com.simple.launcher.retirement.utils.combineSources
import com.simple.launcher.retirement.utils.listenerSources
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
```
