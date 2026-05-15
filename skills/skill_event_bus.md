# Skill: Sử dụng EventBus

## Mục đích

`EventBus<T>` là utility dùng chung để giao tiếp **một chiều** giữa các lớp không có reference trực tiếp đến nhau (thường là Adapter → Fragment/ViewModel). Thay vì mỗi feature tự viết boilerplate `MutableSharedFlow`, chỉ cần kế thừa `EventBus<T>`.

## Vị trí file

```
app/src/main/java/com/simple/launcher/retirement/utils/EventBus.kt
```

## Khi nào dùng

- Adapter muốn thông báo sự kiện click/toggle cho Fragment hoặc ViewModel mà không giữ reference.
- Cần giao tiếp giữa các component trong cùng màn hình (Adapter → Fragment, Fragment → Fragment) mà không qua ViewModel.
- Khi muốn tạo một event bus mới cho một màn hình/feature mới.

## Cách tạo EventBus mới

Tạo file `XxxEvent.kt` trong package của feature, chỉ cần 1 dòng:

```kotlin
package com.simple.launcher.retirement.presentation.xxx

import com.simple.launcher.retirement.utils.EventBus

object XxxEventBus : EventBus<XxxType>()
```

Không cần khai báo `MutableSharedFlow`, `asSharedFlow()`, hay hàm `post()` — tất cả đã có trong `EventBus<T>`.

## Cách dùng

### Gửi event (từ Adapter hoặc bất kỳ nơi nào)

```kotlin
XxxEventBus.post(item)
```

### Nhận event (từ Fragment) — dùng extension `observe`

```kotlin
// Trong observeData() của Fragment — import com.simple.launcher.retirement.utils.lifecycle.observe
XxxEventBus.events.observe(this) { item ->
    viewModel.updateItem(item)
}
```

**KHÔNG** dùng `viewLifecycleOwner.lifecycleScope.launch { collectLatest { } }` thủ công — hãy dùng extension `observe(fragment)`.

### Nhận event trong ViewModel (dùng `listenerSources`)

`listenerSources` trả về `MutableSharedFlow<T>` — trigger lại block mỗi khi một trong các source emit. Phù hợp khi ViewModel cần phản ứng với EventBus mà không cần Fragment trung gian.

```kotlin
// Ví dụ: ViewModel tự lắng nghe EventBus và cập nhật state
val updateResult: MutableSharedFlow<List<SelectableAppEntity>> = listenerSources(XxxEventBus.events) {
    val entity = XxxEventBus.events.first()  // lấy event mới nhất
    val updated = _apps.value.toMutableList().apply {
        val index = indexOfFirst { it.app.packageName == entity.app.packageName }
        if (index != -1) this[index] = this[index].copy(isSelected = !this[index].isSelected)
    }
    emit(updated)  // emit kết quả vào MutableSharedFlow
}
```

> **Quan trọng**: `listenerSources` nhận `vararg sources: Flow<*>` — các flow chỉ dùng làm trigger, không phải input. Luôn phải gọi `.first()` hoặc `.value` trên flow gốc để lấy giá trị thực sự bên trong block.

## Ví dụ thực tế trong project

| EventBus | Type | Nơi gửi | Nơi nhận |
|---|---|---|---|
| `HomeEventBus` | `HomeItem` | `AppAdapter`, `ContactAdapter`, `UtilityAdapter` | `HomeFragment` |
| `SettingsEventBus` | `SettingItem` | `SettingsAdapter` | `SettingsFragment` |
| `AppListEventBus` | `SelectableAppEntity` | `AppListAdapter` | `AppListFragment` / `AppListViewModel` |
| `ContactListEventBus` | `SelectableContactEntity` | `ContactListAdapter` | `ContactListFragment` / `ContactListViewModel` |

## API của EventBus<T>

```kotlin
open class EventBus<T> {
    val events: SharedFlow<T>   // subscribe để nhận event
    fun post(item: T)           // gửi event (thread-safe, non-suspend)
}
```

- `post()` dùng `tryEmit` — không suspend, an toàn gọi từ mọi thread.
- `events` là `SharedFlow` với `extraBufferCapacity = 1` — không drop event nếu consumer chậm một chút.

## Checklist khi thêm EventBus mới

1. [ ] Tạo `object XxxEventBus : EventBus<XxxType>()` trong file `XxxEvent.kt` của feature.
2. [ ] Gọi `XxxEventBus.post(item)` ở nơi phát sinh sự kiện (thường trong `createViewHolder` của Adapter).
3. [ ] Subscribe `XxxEventBus.events.collectLatest { }` ở Fragment hoặc ViewModel.
4. [ ] **Không** tự tạo `MutableSharedFlow` mới — luôn dùng `EventBus<T>`.

## Lưu ý

- Mỗi `object` EventBus là **singleton** — an toàn dùng toàn app.
- `SharedFlow` không có replay mặc định → subscriber chỉ nhận event **sau** khi subscribe. Đây là hành vi mong muốn cho event bus (khác với state).
- Nếu cần giữ lại giá trị cuối (state), hãy dùng `MutableSharedFlow(replay = 1)` trong `ViewModel.mutableSharedFlow(...)` thay vì `EventBus`.
