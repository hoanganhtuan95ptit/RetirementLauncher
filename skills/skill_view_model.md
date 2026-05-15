# Skill: Sử dụng ViewModel và Flow

## Mục đích

Đảm bảo code trong ViewModel ngắn gọn, dễ đọc và tuân thủ pattern quản lý state thống nhất bằng cách sử dụng các extension functions có sẵn.

## Quy tắc 1 — Luôn dùng `combineState` để kết hợp các Flow

Khi cần kết hợp nhiều `Flow` (như dữ liệu từ repository, stringMap, themeMap) thành một `StateFlow` để expose ra View, **không được** dùng `combine(...).stateIn(...)` thủ công.

**Lý do:**
- Giảm boilerplate code.
- Đảm bảo `SharingStarted.Eagerly` được sử dụng thống nhất.
- Tự động quản lý `viewModelScope`.

### Các phiên bản hỗ trợ
Hiện tại `ViewModelExt.kt` hỗ trợ kết hợp từ 2 đến 5 flows.

```kotlin
// ✅ Đúng — dùng combineState
val items: StateFlow<List<ViewItem>> = combineState(
    flow1 = flowA,
    flow2 = flowB,
    initialValue = emptyList()
) { a, b ->
    // transform logic
}

// ❌ Sai — viết thủ công
val items: StateFlow<List<ViewItem>> = combine(flowA, flowB) { a, b ->
    // transform logic
}.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
```

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
            label = it.entity.label.toRich(), // ✅ Transform ngay tại đây
            icon = ImageDrawable(it.entity.icon),
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
- `buildToolbarTitle(...)`, `buildBackIcon(...)`, `buildActionState(...)`: Các hàm helper để tạo `RichText`/`RichImage` chuẩn UI.

```kotlin
class HomeViewModel(...) : BaseViewModel() {
    // strings và themes đã có sẵn từ BaseViewModel
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

## Import cần thiết

```kotlin
import com.simple.launcher.retirement.utils.combineState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
```
