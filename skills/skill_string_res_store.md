# Skill: Dùng StringResStore trong project này

## Mục tiêu

Truy cập string trong ViewModel mà không cần `Context`, đúng với pattern đang dùng ở `Home` và `Settings`.

## API hiện có

```kotlin
object StringResStore {
    val stringMapFlow: StateFlow<Map<String, String>>
    fun load(context: Context)
}

fun Map<String, String>.getString(resId: Int): String
fun Map<String, Any>.getString(resId: Int): String
fun Int.asStringRes(): String
fun String.asStringRes(): String
fun String.asFormattedStringRes(vararg args: Any): String
fun String.observeStringRes(): Flow<String>
```

## Pattern đang dùng thật

Trong ViewModel của project, ưu tiên lấy string từ `resources`:

```kotlin
val title = resources.getString(R.string.settings_title)
val header = resources.getString(R.string.home_header_apps)
```

Lý do:

- `resources` đã gộp sẵn size, theme, string.
- Dễ dùng chung trong `combineState(resources, ...)`.
- Không phải chạm trực tiếp vào `StringResStore` ở mọi nơi.

## Khi nào dùng `strings`

Nếu ViewModel chỉ cần string map thuần:

```kotlin
val label = strings.value.getString(R.string.app_name)
```

Nhưng với code mới trong repo này, `resources` là lựa chọn mặc định.

## Lưu ý

- `StringResStore.load(context)` phải được gọi sớm khi app khởi động.
- `getString(resId)` trả về chuỗi rỗng nếu chưa load hoặc không tìm thấy key.
- Không truyền `Context` vào ViewModel chỉ để gọi `getString(...)`.
