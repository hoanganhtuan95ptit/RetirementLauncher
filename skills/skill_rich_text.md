# Skill: Sử dụng RichText để định dạng văn bản linh hoạt

## Mục đích

`RichText` là một công cụ giúp tạo và quản lý các chuỗi văn bản có định dạng (Spannable) một cách dễ dàng và khai báo (declarative). Nó thay thế việc sử dụng `SpannableString` và `setSpan` thủ công bằng một cú pháp trôi chảy (fluent API), giúp code sạch hơn và dễ bảo trì hơn.

## Vị trí

| File | Package |
|---|---|
| `RichText.kt` | `com.simple.launcher.retirement.utils.text` |
| `RichTextBuilder.kt` | `com.simple.launcher.retirement.utils.text` |
| `RichSpan.kt` | `com.simple.launcher.retirement.utils.text` |
| `Exts.kt` | `com.simple.launcher.retirement.utils.text` |

## Kiến trúc tổng quan

Hệ thống dựa trên việc định nghĩa các `RichSpan` (đại diện cho kiểu style) và các `RichSpanConvert` để chuyển đổi chúng thành các class `CharacterStyle` của Android.

### Các class và hàm chính

| Thành phần | Mô tả |
|---|---|
| `RichText` | Lớp chứa nội dung văn bản và danh sách các style đã áp dụng. |
| `RichTextBuilder` | Builder tạo `RichText` bằng fluent API (chaining). |
| `RichSpan` | Lớp cha cho các loại định dạng (Bold, ForegroundColor, RelativeSize, v.v.). |
| `String.withFirst()` | Áp dụng style cho lần xuất hiện **đầu tiên** của một từ khóa. |
| `String.withAll()` | Áp dụng style cho **tất cả** các lần xuất hiện của một từ khóa. |
| `TextView.setText(RichText)` | Hàm mở rộng để hiển thị `RichText` lên UI. |

### Các RichSpan có sẵn

| RichSpan | Mô tả |
|---|---|
| `Bold` | In đậm (Typeface.BOLD). |
| `ForegroundColor(color: Int)` | Đổi màu chữ. |
| `RelativeSize(proportion: Float)` | Thay đổi kích thước chữ tương đối (1.5f = 150%). |
| `TextSize(sizeDip: Int)` | Kích thước chữ tuyệt đối theo dp. |
| `CustomFont(typeface: Typeface)` | Áp dụng custom Typeface. |
| `RoundedOutline(...)` | Vẽ viền bo tròn quanh đoạn text. |

## Hướng dẫn sử dụng

Có **2 cách** tạo `RichText`: dùng **extension functions** hoặc dùng **RichText.Builder**.

---

### Cách 1: Extension functions (phù hợp cho trường hợp đơn giản)

#### 1.1. Áp dụng style cho từ khóa đầu tiên

```kotlin
val message = "Chào mừng bạn đến với Retirement Launcher"
    .withFirst("Retirement Launcher", Bold, ForegroundColor(Color.BLUE))

textView.setText(message)
```

#### 1.2. Áp dụng style cho tất cả các từ khóa trùng khớp

```kotlin
val description = "Android là hệ điều hành, Android rất phổ biến."
    .withAll("Android", Bold, RelativeSize(1.2f))

textView.setText(description)
```

#### 1.3. Kết hợp nhiều style (Chaining)

```kotlin
val complexText = "Giá: 100.000đ (Đã bao gồm thuế)"
    .withFirst("100.000đ", Bold, ForegroundColor(Color.RED), RelativeSize(1.5f))
    .withFirst("(Đã bao gồm thuế)", RelativeSize(0.8f))

textView.setText(complexText)
```

---

### Cách 2: RichText.Builder (khuyến khích — rõ ràng, dễ đọc hơn)

Sử dụng `RichText.Builder(text)` để tạo builder, rồi gọi chaining các phương thức `with()`, `withFirst()`, `withAll()`, cuối cùng gọi `build()`.

#### API của RichTextBuilder

| Method | Mô tả |
|---|---|
| `with(vararg spans)` | Áp dụng style cho **toàn bộ** chuỗi gốc. |
| `withFirst(substring, vararg spans)` | Áp dụng style cho lần xuất hiện **đầu tiên** của `substring`. |
| `withAll(substring, vararg spans)` | Áp dụng style cho **tất cả** lần xuất hiện của `substring`. |
| `build()` | Tạo ra `RichText` cuối cùng. |

#### 2.1. Style toàn bộ chuỗi

```kotlin
val title = RichText.Builder("Cài đặt")
    .with(ForegroundColor(textColor), TextSize(20), Bold)
    .build()
```

#### 2.2. Style một phần chuỗi

```kotlin
val desc = RichText.Builder("Bạn có 5 file rác cần dọn dẹp")
    .withFirst("5", Bold, ForegroundColor(Color.RED))
    .withFirst("rác", ForegroundColor(Color.YELLOW))
    .build()
```

#### 2.3. Kết hợp style toàn bộ + style một phần

```kotlin
val text = RichText.Builder(getString(R.string.overlay_permission_desc))
    .with(ForegroundColor(descColor))
    .withFirst(getString(R.string.overlay_permission_highlight), Bold, ForegroundColor(highlightColor))
    .build()
```

---

### Hiển thị trên TextView

```kotlin
val richText: RichText = ...
binding.tvHeader.setText(richText)
```

## Tại sao nên dùng RichText?

1.  **Cú pháp khai báo**: Giúp người đọc hiểu ngay phần văn bản nào đang được định dạng như thế nào mà không cần đọc logic `index` phức tạp.
2.  **Khả năng mở rộng**: Bạn có thể dễ dàng thêm các loại `RichSpan` mới bằng cách kế thừa `RichSpan` và tạo một `RichSpanConvert` tương ứng với annotation `@AutoService`.
3.  **Tự động hóa**: Nhờ sử dụng `ServiceLoader` và `AutoService`, các trình chuyển đổi style mới sẽ tự động được hệ thống nhận diện.
4.  **Hai cách dùng**: Extension functions cho code ngắn gọn, Builder cho code rõ ràng — tùy ngữ cảnh mà chọn.

## Ví dụ thực tế trong dự án

### Ví dụ 1: ActionState cho nút bấm (Builder)

```kotlin
fun buildActionState(
    text: String,
    textColor: Int,
    textSize: Int = 18
): ActionState = ActionState(
    text = RichText.Builder(text)
        .with(ForegroundColor(textColor), TextSize(textSize), Bold)
        .build()
)
```

### Ví dụ 2: Settings Item (Extension)

```kotlin
val textColor = themeMap.getColor(android.R.attr.textColorPrimary)
val item = SettingItem(
    title = stringMap.getString(R.string.setting_pin).toRich().with(ForegroundColor(textColor)),
)
```

### Ví dụ 3: Permission screen với highlight (Builder)

```kotlin
val description = RichText.Builder(stringMap.getString(R.string.overlay_permission_desc))
    .with(ForegroundColor(descColor))
    .withFirst(stringMap.getString(R.string.overlay_permission_highlight), Bold, ForegroundColor(highlightColor))
    .build()
```

### Ví dụ 4: Header với custom font (Extension)

```kotlin
val header = strings.getString(R.string.home_header_apps).toRich().with(
    ForegroundColor(Color.WHITE),
    TextSize(20),
    CustomFont(Typeface.create("sans-serif-medium", Typeface.NORMAL))
)
```

## Lưu ý quan trọng

- **Ưu tiên dùng `RichText.Builder`** khi cần kết hợp `with()` + `withFirst()` — code rõ ràng hơn extension chaining.
- **Dùng extension functions** (`toRich()`, `.with()`) khi chỉ cần style đơn giản cho toàn bộ chuỗi.
- Luôn gọi `build()` ở cuối khi dùng Builder.
- `withFirst` trả về chính builder nếu substring không tìm thấy (không crash).
