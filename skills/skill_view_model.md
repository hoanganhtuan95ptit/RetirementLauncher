# Skill: ViewModel pattern trong RetirementLauncher

## Mục tiêu

Viết ViewModel theo đúng pattern hiện tại của project:

- `BaseViewModel` cung cấp `sizes`, `themes`, `strings`, `resources`, `background`, `bottomSheet`.
- `ViewItemViewModel` dùng để ghép nhiều nhóm `ViewItem` theo `order`.
- `Home` và `Settings` không tự build toàn bộ list trong một ViewModel lớn. Mỗi nhóm có ViewModel riêng, sau đó đẩy `GroupViewItem` vào ViewModel cha qua service.

## 1. Base class cần dùng

### `BaseViewModel`

Kế thừa khi màn chỉ cần expose state riêng:

```kotlin
open class BaseViewModel : ViewModel() {
    val sizes = sizeMapFlow
    val themes = ThemeColorStore.colorMapFlow
    val strings = StringResStore.stringMapFlow
    val resources: StateFlow<Map<String, Any>>
    val background: StateFlow<Background>
    open val bottomSheet: StateFlow<BottomSheetState>
}
```

`resources` là map đã gộp `sizes + themes + strings`. Trong code hiện tại, các ViewModel của `Home` service và `Settings` service đều ưu tiên dùng `resources`.

### `ViewItemViewModel`

Dùng khi màn hình được ghép từ nhiều nhóm item độc lập:

```kotlin
abstract class ViewItemViewModel : BaseViewModel() {
    val viewItemList: StateFlow<List<ViewItem>>
    fun updateItem(order: Double, list: List<ViewItem>?)
    fun updateItem(groupViewItem: GroupViewItem?)
}
```

Pattern này đang được dùng ở:

- `HomeViewModel`
- `SettingsViewModel`

## 2. `combineState` là API mặc định

Trong repo này, `combineState(...)` là extension của `ViewModel`. Lambda nhận `MutableStateFlow<R>` làm receiver, nên phải gán qua `value = ...`.

```kotlin
val toolbar: StateFlow<ToolbarState> = combineState(
    resources,
    ToolbarState.empty()
) { resources ->
    val color = resources.textColorPrimary
    value = ToolbarState(
        title = buildToolbarTitle(resources.getString(R.string.settings_title), color),
        backIcon = buildBackIcon(color)
    )
}
```

Không dùng ví dụ kiểu `return` trực tiếp trong lambda của `combineState`.

## 3. Pattern thật của Home

`HomeViewModel` hiện tại rất mỏng:

```kotlin
class HomeViewModel : ViewItemViewModel()
```

Mỗi section của Home có ViewModel riêng:

- `ClockViewModel`
- `AppViewModel`
- `ContactViewModel`

Mỗi ViewModel con build một `GroupViewItem`:

```kotlin
val appViewItemList: StateFlow<GroupViewItem?> = combineState(
    flow1 = resources,
    flow2 = GetHomeAppsUseCase2.instance.invoke(),
    initialValue = null
) { resources, apps ->
    value = buildAppGroup(resources, apps)
}
```

Sau đó `HomeService` tương ứng sẽ collect và đẩy vào `HomeViewModel.updateItem(...)`.

## 4. Pattern thật của Settings

`SettingsViewModel` cũng là `ViewItemViewModel`, nhưng ngoài list còn có `toolbar` và `bottomViewItem`.

```kotlin
class SettingsViewModel : ViewItemViewModel() {
    val toolbar: StateFlow<ToolbarState> = ...
    val bottomViewItem: StateFlow<List<ViewItem>> = ...
}
```

`bottomViewItem` đang được dùng để chèn `SpaceViewItem` theo `navigationBarHeight`, rồi `init { ... updateItem(Double.MAX_VALUE, it) }`.

Các group settings được tách thành ViewModel riêng, ví dụ:

- `CommonSettingViewModel`
- `ProtectSettingViewModel`
- `AppMonitoringSettingViewModel`

Mỗi ViewModel con trả về `GroupViewItem?`, không tự sửa `SettingsViewModel` trực tiếp.

## 5. Khi nào dùng `GroupViewItem`

Dùng khi một feature sở hữu nguyên một nhóm item trong list tổng:

```kotlin
GroupViewItem(
    order = 1.1,
    list = listOf(settingItem(...))
)
```

Quy ước hiện tại:

- `order` quyết định vị trí nhóm trong list tổng.
- Một service chỉ nên quản một slot hoặc một vài slot cố định.
- Muốn xóa cả nhóm thì truyền `null` hoặc list rỗng vào `updateItem`.

## 6. Cách lấy resource trong ViewModel

Trong code hiện tại, ưu tiên dùng extension trên `Map<String, Any>`:

```kotlin
val title = resources.getString(R.string.home_header_apps)
val color = resources.textColorPrimary
val inset = resources.navigationBarHeight
```

Không cần giữ `Context` trong ViewModel chỉ để resolve string hoặc color.

## 7. Những điểm cần giữ đúng

- `Home` và `Settings` hiện đang theo hướng composition bằng service, không quay lại kiểu một ViewModel build tất cả section.
- Với `combineState`, luôn gán `value = ...` trong lambda.
- Nếu state là list hiển thị tổng hợp, ưu tiên `ViewItemViewModel` + `GroupViewItem`.
- Nếu state chỉ là một nhóm con, dùng `BaseViewModel` là đủ.
