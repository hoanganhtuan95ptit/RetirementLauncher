# Skill: ThemeColorStore và `resources` color access

## Mục tiêu

Lấy màu theme đúng với code hiện tại, đặc biệt cho `SettingsViewModel` và các setting builders.

## API gốc

```kotlin
object ThemeColorStore {
    val colorMapFlow: StateFlow<Map<String, Int>>
    fun load(context: Context)
}

fun Map<String, Int>.getColor(@AttrRes attrId: Int, defaultColor: Int = Color.BLACK): Int
fun Context.getThemeColor(@AttrRes attrId: Int): Int
```

## Pattern đang dùng thật trong repo

Project hiện không hay gọi `themeMap.getColor(...)` trực tiếp ở tầng feature nữa. Thay vào đó, `BaseViewModel.resources` được gắn thêm các extension property:

```kotlin
val color = resources.textColorPrimary
val bg = resources.colorBackground
val stroke = resources.colorPrimary
val iconTint = resources.colorOnPrimaryContainer
```

Các extension này nằm ở `utils/exts/Color.kt`.

## Khi nào dùng gì

- Trong feature ViewModel: ưu tiên `resources.textColorPrimary`, `resources.colorBackground`, ...
- Trong code framework hoặc util cấp thấp: có thể dùng `ThemeColorStore.colorMapFlow` hoặc `Context.getThemeColor(...)`.

## Lưu ý

- `ThemeColorStore.load(context)` phải được gọi lại nếu app đổi theme runtime.
- `BaseViewModel.background` và `bottomSheet` đang build từ `themes`, nên màn hình thường không cần tự resolve lại các màu nền cơ bản.
