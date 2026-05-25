# Skill: Sử dụng RichText để định dạng văn bản linh hoạt

## Mục đích

`RichText` là một công cụ giúp tạo và quản lý các chuỗi văn bản có định dạng (Spannable) một cách dễ dàng và khai báo (declarative). Nó thay thế việc sử dụng `SpannableString` và `setSpan` thủ công bằng một cú pháp mở rộng (extension functions) trôi chảy, giúp code sạch hơn và dễ bảo trì hơn.

## Vị trí

| File / Folder | Package / Path |
|---|---|
| `RichText.kt` | `com.simple.launcher.retirement.utils.text` |
| `RichTextBuilder.kt` | `com.simple.launcher.retirement.utils.text` |
| `RichSpan.kt` | `com.simple.launcher.retirement.utils.text` (Chứa lớp cơ sở `RichSpan` & `RichSpanConvert`) |
| `Exts.kt` | `com.simple.launcher.retirement.utils.text` |
| `span/` | Thư mục `utils/text/span/` (Chứa các custom spans cụ thể) |

## Kiến trúc tổng quan

Hệ thống dựa trên việc định nghĩa các `RichSpan` (đại diện cho kiểu style) và các `RichSpanConvert` để chuyển đổi chúng thành các class `CharacterStyle` của Android. Các span cụ thể được đặt trong thư mục `utils/text/span/` để giữ mã nguồn sạch sẽ.

### Các class và hàm chính

| Thành phần | Mô tả |
|---|---|
| `RichText` | Lớp chứa nội dung văn bản và danh sách các style đã áp dụng. Có thể khởi tạo đơn giản bằng `RichText("nội dung")` khi không cần style. |
| `RichTextBuilder` | Builder tạo `RichText` dùng trong nội bộ. |
| `String.toBuilder()` | Chuyển đổi String thành `RichTextBuilder`. |
| `String.with(...)` | Bắt đầu builder bằng cách áp dụng style cho toàn bộ String. |
| `String.withFirst(...)` | Bắt đầu builder bằng cách áp dụng style cho lần xuất hiện đầu tiên của một từ khóa. |
| `String.withAll(...)` | Bắt đầu builder bằng cách áp dụng style cho tất cả lần xuất hiện của một từ khóa. |
| `TextView.setText(RichText)` | Hàm mở rộng để hiển thị `RichText` lên UI. |

### Các RichSpan có sẵn (nằm tại thư mục `utils/text/span/`)

| RichSpan | Mô tả |
|---|---|
| `Bold` | In đậm (Typeface.BOLD). |
| `ForegroundColor(color: Int)` | Đổi màu chữ. |
| `RelativeSize(proportion: Float)` | Thay đổi kích thước chữ tương đối (1.5f = 150%). |
| `TextSize(sizeDip: Int)` | Kích thước chữ tuyệt đối theo dp. |
| `CustomFont(typeface: Typeface)` | Áp dụng custom Typeface. |
| `RoundedOutline(...)` | Vẽ viền bo tròn quanh đoạn text. |

## Hướng dẫn sử dụng

Luôn sử dụng các **extension functions** trực tiếp trên `String` để tạo `RichText` một cách tự nhiên và sạch đẹp nhất. Nhớ import `com.simple.launcher.retirement.utils.text.*`.

---

### 1. Áp dụng style cho toàn bộ chuỗi

```kotlin
import com.simple.launcher.retirement.utils.text.*

val title = "Cài đặt"
    .with(ForegroundColor(textColor), TextSize(20), Bold)
    .build()

textView.setText(title)
```

---

### 2. Áp dụng style cho từ khóa đầu tiên

```kotlin
import com.simple.launcher.retirement.utils.text.*

val message = "Chào mừng bạn đến với Retirement Launcher"
    .withFirst("Retirement Launcher", Bold, ForegroundColor(Color.BLUE))
    .build()

textView.setText(message)
```

---

### 3. Áp dụng style cho tất cả các từ khóa trùng khớp

```kotlin
import com.simple.launcher.retirement.utils.text.*

val description = "Android là hệ điều hành, Android rất phổ biến."
    .withAll("Android", Bold, RelativeSize(1.2f))
    .build()

textView.setText(description)
```

---

### 4. Kết hợp nhiều style (Chaining)

```kotlin
import com.simple.launcher.retirement.utils.text.*

val complexText = "Giá: 100.000đ (Đã bao gồm thuế)"
    .withFirst("100.000đ", Bold, ForegroundColor(Color.RED), RelativeSize(1.5f))
    .withFirst("(Đã bao gồm thuế)", RelativeSize(0.8f))
    .build()

textView.setText(complexText)
```

---

### 5. Tạo RichText đơn giản không có style

```kotlin
import com.simple.launcher.retirement.utils.text.RichText

val simpleText = RichText("Chỉ là text bình thường")
textView.setText(simpleText)
```

---

### Hiển thị trên TextView

```kotlin
val richText: RichText = ...
binding.tvHeader.setText(richText)
```

## Tại sao nên dùng RichText?

1. **Cú pháp khai báo**: Giúp người đọc hiểu ngay phần văn bản nào đang được định dạng như thế nào mà không cần đọc logic `index` phức tạp.
2. **Khả năng mở rộng**: Bạn có thể dễ dàng thêm các loại `RichSpan` mới bằng cách kế thừa `RichSpan` và tạo một `RichSpanConvert` tương ứng với annotation `@AutoService`.
3. **Tự động hóa**: Nhờ sử dụng `ServiceLoader` và `AutoService`, các trình chuyển đổi style mới sẽ tự động được hệ thống nhận diện.
4. **Không Boilerplate**: Không cần gọi `RichText.Builder(...)` thủ công, chỉ cần gọi extension trực tiếp trên String.

## Ví dụ thực tế trong dự án

### Ví dụ 1: ActionState cho nút bấm

```kotlin
import com.simple.launcher.retirement.utils.text.*

fun buildActionState(
    text: String,
    textColor: Int,
    textSize: Int = 18
): ActionState = ActionState(
    text = text.with(ForegroundColor(textColor), TextSize(textSize), Bold).build()
)
```

### Ví dụ 2: Settings Item

```kotlin
import com.simple.launcher.retirement.utils.text.*

val textColor = themeMap.getColor(android.R.attr.textColorPrimary)
val item = SettingItem(
    title = stringMap.getString(R.string.setting_pin).with(ForegroundColor(textColor)).build(),
)
```

### Ví dụ 3: Permission screen với highlight

```kotlin
import com.simple.launcher.retirement.utils.text.*

val description = stringMap.getString(R.string.overlay_permission_desc)
    .with(ForegroundColor(descColor))
    .withFirst(stringMap.getString(R.string.overlay_permission_highlight), Bold, ForegroundColor(highlightColor))
    .build()
```

### Ví dụ 4: Header với custom font

```kotlin
import com.simple.launcher.retirement.utils.text.*

val header = strings.getString(R.string.home_header_apps)
    .with(
        ForegroundColor(Color.WHITE),
        TextSize(20),
        CustomFont(Typeface.create("sans-serif-medium", Typeface.NORMAL))
    ).build()
```

## Lưu ý quan trọng

- **Luôn import** `com.simple.launcher.retirement.utils.text.*` (hoặc các extension riêng lẻ) để gọi được `.with()`, `.withFirst()`, `.withAll()`.
- **Luôn gọi `.build()`** ở cuối cùng của chuỗi phương thức để tạo đối tượng `RichText` thực sự.
- `withFirst` và `withAll` trả về chính builder nếu substring không tìm thấy (không crash).

