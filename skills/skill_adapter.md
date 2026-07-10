# Skill: Adapter pattern hiện tại của Home và Settings

## Mục tiêu

Project này đang có 2 pattern adapter khác nhau:

- `Settings`: `ViewItemAdapter` thường.
- `Home`: `PrecomputedAdapter` + `PrecomputedViewItem`.

Khi thêm item mới, phải chọn đúng pattern theo màn.

## 1. Settings dùng `ViewItemAdapter`

Ví dụ thật: `SettingsAdapter`, `SettingHeaderAdapter`.

### Quy tắc

- `ViewItem` chứa dữ liệu đã sẵn sàng để bind.
- Adapter chỉ `setText`, `setImage`, `setBackground`, `isChecked`.
- Dùng `payloads` đúng theo tag từ `getContentsCompare()`.

Ví dụ:

```kotlin
data class SettingItem(
    val id: Int,
    val title: BigText,
    val icon: BigImage,
    val iconBackground: Background,
    val isSwitch: Boolean,
    var isChecked: Boolean,
    val background: Background
) : ViewItem, SpanSizeLookupViewItem
```

```kotlin
if (payloads.isEmpty() || payloads.contains("title")) {
    binding.tvSettingTitle.setText(item.title)
}
```

### Click handling của Settings

`SettingsAdapter` không điều hướng trực tiếp. Nó post event chung:

```kotlin
AppEventBus.post(AppEvent.SettingClicked(item))
```

`SettingsFragment` mới là nơi xử lý điều hướng và permission.

### Switch trong Settings

Switch đang bị khóa thao tác trực tiếp:

```kotlin
binding.swSetting.disableUserChange()
```

User tap vào `root`, sau đó Fragment hoặc service quyết định có đổi trạng thái hay không.

## 2. Home dùng `PrecomputedAdapter`

Ví dụ thật:

- `ClockAdapter`
- `HeaderAdapter`
- `AppAdapter`
- `ContactAdapter`

### Cấu trúc

```kotlin
abstract class PrecomputedViewItem : ViewItem {
    lateinit var spec: DrawSpec
    abstract fun buildDrawSpec(resources: Map<String, Any>)
}
```

```kotlin
abstract class PrecomputedAdapter<T : PrecomputedViewItem> : ViewItemAdapter<T, ItemNodeBinding>()
```

### Quy tắc

- Item phải tự build `DrawSpec` trong `buildDrawSpec(resources)`.
- `onBindViewHolder(...)` chỉ gán `binding.nodeView.spec = item.spec`.
- Click được xử lý qua `onItemCLick(item)`.

Ví dụ:

```kotlin
override fun onItemCLick(item: AppHomeItem) {
    sendDeeplink(DeepLinks.APP, mapOf("entity" to item.entity))
}
```

Với Home, pattern hiện tại là adapter có thể điều hướng trực tiếp nếu hành vi click gắn chặt với item đó.

## 3. Span size

- `Home` dùng `HomeItem.spanSize` với `TOTAL_COLUMNS = 6`.
- `Settings` dùng `SpanSizeLookupViewItem.getSpanSize()` với grid 2 cột.

Không trộn 2 cách này.

## 4. Quy tắc dữ liệu

- Với adapter thường: item nên chứa `BigText`, `BigImage`, `Background`, boolean, v.v.
- Với precomputed item: có thể giữ entity và build node từ entity ngay trong item.
- Không chuyển logic style sang Fragment.

## 5. Checklist trước khi thêm adapter mới

- Màn đang dùng `RecyclerView` thường hay `ItemNodeBinding` precomputed.
- `getContentsCompare()` đã khai báo đủ field hiển thị chưa.
- Nếu là Settings-style item, click có đi qua `AppEventBus` chưa.
- Nếu là Home-style item, đã gọi `buildDrawSpec(resources)` trước khi đưa vào list chưa.
