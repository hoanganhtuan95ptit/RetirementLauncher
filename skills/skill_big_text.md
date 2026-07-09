# Skill: Sử dụng BigText để định dạng văn bản linh hoạt

## Mục đích

`BigText` là công cụ giúp tạo và quản lý các chuỗi văn bản có định dạng (Spannable) một cách dễ dàng và khai báo (declarative). Dự án sử dụng thư viện `BigText` từ module `:node-engine`.

## Thư viện sử dụng

| Thành phần | Package |
|---|---|
| `BigText` | `com.simple.ui.precompute.text` |
| Spans (`BigBold`, `BigForegroundColor`, ...) | `com.simple.ui.precompute.text.span` |

## Các class và hàm chính

 Thành phần | Mô tả |
---|---|
 `BigText` | Lớp chứa nội dung văn bản và danh sách các style đã áp dụng. |
 `BigTextBuilder` | Builder tạo `BigText` dùng trong nội bộ. |
 `String.with(...)` | Bắt đầu builder bằng cách áp dụng style cho toàn bộ String. |
 `String.withFirst(...)` | Áp dụng style cho lần xuất hiện đầu tiên của một từ khóa. |
 `String.withAll(...)` | Áp dụng style cho tất cả lần xuất hiện của một từ khóa. |
 `TextView.setText(BigText)` | Hàm mở rộng để hiển thị `BigText` lên UI. |

## Các BigSpan có sẵn (`com.simple.ui.precompute.text.span`)

 BigSpan | Mô tả |
---|---|
 `BigBold` | In đậm. |
 `BigForegroundColor(color: Int)` | Đổi màu chữ. |
 `BigRelativeSize(proportion: Float)` | Thay đổi kích thước chữ tương đối. |
 `BigTextSize(sizePx: Int)` | Kích thước chữ tuyệt đối theo pixel (thường dùng `.toPx()`). |
 `BigCustomFont(typeface: Typeface)` | Áp dụng custom Typeface. |
 `BigRoundedOutline(...)` | Vẽ viền bo tròn quanh đoạn text. |

## Hướng dẫn sử dụng

Luôn sử dụng các **extension functions** trực tiếp trên `String` để tạo `BigText`. Nhớ import `com.simple.ui.precompute.text.*` và các span cần thiết.

---

### 1. Áp dụng style cho toàn bộ chuỗi

```kotlin
import com.simple.ui.precompute.text.*
import com.simple.ui.precompute.text.span.*

val title = "Cài đặt"
    .with(BigForegroundColor(textColor), BigBold)
    .build()

textView.setText(title)
```

---

### 2. Áp dụng style cho từ khóa đầu tiên

```kotlin
import com.simple.ui.precompute.text.*
import com.simple.ui.precompute.text.span.*

val message = "Chào mừng bạn đến với Retirement Launcher"
    .withFirst("Retirement Launcher", BigBold, BigForegroundColor(Color.BLUE))
    .build()
```

---

### 3. Helpers trong `utils/text/Exts.kt`

Dự án cung cấp một số helper để tạo nhanh các style chuẩn Material Design:

```kotlin
import com.simple.launcher.retirement.utils.text.*

val title = "Tiêu đề"
    .withStyleTitleLarge() // Trả về BigTextBuilder với size 22dp
    .with(BigBold)
    .build()
```

## Lưu ý quan trọng

- **Luôn import** `com.simple.ui.precompute.text.*` để gọi được `.with()`, `.withFirst()`, `.withAll()`.
- **Luôn gọi `.build()`** ở cuối cùng để tạo đối tượng `BigText`.
- Đối với `BigTextSize`, đơn vị là **pixel**. Sử dụng extension `.toPx()` từ `utils.size` nếu truyền vào giá trị dp.
