# Skill: EventBus pattern hiện tại

## Mục tiêu

Repo hiện tại không còn ưu tiên nhiều bus riêng cho từng màn. Pattern đang dùng thật là một `AppEventBus` chung với `sealed class AppEvent`.

## 1. Cấu trúc hiện tại

```kotlin
sealed class AppEvent {
    object PermissionAccept : PermissionResult()
    object PermissionCancel : PermissionResult()
    object PinSetupSuccess : PinResult()
    object PinVerifySuccess : PinResult()
    object PinCancel : PinResult()
    data class AppSelected(val entity: SelectableAppEntity) : AppEvent()
    data class ContactSelected(val entity: SelectableContactEntity) : AppEvent()
    data class SettingClicked(val item: SettingItem) : AppEvent()
}

object AppEventBus : EventBus<AppEvent>()
```

## 2. Pattern đang dùng ở Settings

Adapter post event:

```kotlin
AppEventBus.post(AppEvent.SettingClicked(item))
```

Fragment nghe event:

```kotlin
AppEventBus.events
    .filterIsInstance<AppEvent.SettingClicked>()
    .observe(this@SettingsFragment) { event ->
        handleSettingItemClick(event.item)
    }
```

## 3. Pattern đang dùng cho permission và PIN

`PermissionManager` chờ:

- `AppEvent.PermissionResult`
- `AppEvent.PinResult`

Bottom sheet hoặc màn PIN phải post đúng event để coroutine đang chờ được resume.

## 4. Lưu ý quan trọng

- `EventBus` hiện dùng `MutableSharedFlow` với `extraBufferCapacity = Int.MAX_VALUE`.
- Khi feature mới chỉ cần vài event nội bộ nhưng vẫn cùng domain app, ưu tiên mở rộng `AppEvent` trước.
- Chỉ tạo bus riêng khi thật sự có boundary rõ ràng; đừng mặc định mỗi màn một bus mới.
