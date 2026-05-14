# Skill: Sử dụng RichText để định dạng văn bản linh hoạt

## Mục đích

`RichText` là một công cụ giúp tạo và quản lý các chuỗi văn bản có định dạng (Spannable) một cách dễ dàng và khai báo (declarative). Nó thay thế việc sử dụng `SpannableString` và `setSpan` thủ công bằng một cú pháp trôi chảy (fluent API), giúp code sạch hơn và dễ bảo trì hơn.

## Kiến trúc tổng quan

Hệ thống dựa trên việc định nghĩa các `RichSpan` (đại diện cho kiểu style) và các `RichSpanConvert` để chuyển đổi chúng thành các class `CharacterStyle` của Android.

### Các class và hàm chính

| Thành phần | Mô tả |
|---|---|
| `RichText` | Lớp chứa nội dung văn bản và danh sách các style đã áp dụng. |
| `RichSpan` | Lớp cha cho các loại định dạng (Bold, ForegroundColor, RelativeSize, v.v.). |
| `String.withFirst()` | Áp dụng style cho lần xuất hiện **đầu tiên** của một từ khóa. |
| `String.withAll()` | Áp dụng style cho **tất cả** các lần xuất hiện của một từ khóa. |
| `TextView.setText(RichText)` | Hàm mở rộng để hiển thị `RichText` lên UI. |

## Hướng dẫn sử dụng

### 1. Áp dụng style cho từ khóa đầu tiên

Sử dụng `withFirst` khi bạn chỉ muốn nhấn mạnh một cụm từ duy nhất.

```kotlin
val message = "Chào mừng bạn đến với Retirement Launcher"
    .withFirst("Retirement Launcher", Bold, ForegroundColor(Color.BLUE))

textView.setText(message)
```

### 2. Áp dụng style cho tất cả các từ khóa trùng khớp

Sử dụng `withAll` để định dạng hàng loạt các cụm từ giống nhau trong văn bản.

```kotlin
val description = "Android là hệ điều hành, Android rất phổ biến."
    .withAll("Android", Bold, RelativeSize(1.2f))

textView.setText(description)
```

### 3. Kết hợp nhiều style (Chaining)

Bạn có thể gọi liên tiếp các hàm để định dạng nhiều phần khác nhau trong chuỗi.

```kotlin
val complexText = "Giá: 100.000đ (Đã bao gồm thuế)"
    .withFirst("100.000đ", Bold, ForegroundColor(Color.RED), RelativeSize(1.5f))
    .withFirst("(Đã bao gồm thuế)", RelativeSize(0.8f))

textView.setText(complexText)
```

### 4. Hiển thị trên TextView

Sử dụng hàm mở rộng `setText` để gán dữ liệu từ `RichText` vào `TextView`.

```kotlin
val richText: RichText = ...
binding.tvHeader.setText(richText)
```

## Tại sao nên dùng RichText?

1.  **Cú pháp khai báo**: Giúp người đọc hiểu ngay phần văn bản nào đang được định dạng như thế nào mà không cần đọc logic `index` phức tạp.
2.  **Khả năng mở rộng**: Bạn có thể dễ dàng thêm các loại `RichSpan` mới bằng cách kế thừa `RichSpan` và tạo một `RichSpanConvert` tương ứng với annotation `@AutoService`.
3.  **Tự động hóa**: Nhờ sử dụng `ServiceLoader` và `AutoService`, các trình chuyển đổi style mới sẽ tự động được hệ thống nhận diện.
4.  **Tối ưu**: Các hàm `withFirst` và `withAll` đã được tối ưu để tránh tạo đối tượng thừa và thực hiện kiểm tra lỗi biên an toàn.

## Ví dụ trong RecyclerView

Rất hữu ích khi cần hiển thị văn bản có highlight từ khóa tìm kiếm hoặc định dạng giá tiền trong item.

### Ví dụ 1: Định dạng tiền tệ
```kotlin
val formattedAmount = item.amount.withFirst("$", Bold, RelativeSize(0.7f))
binding.tvAmount.setText(formattedAmount)
```

### Ví dụ 2: Settings Item với màu từ Theme
```kotlin
data class SettingItem(
    val title: RichText,
    ...
)

// Trong ViewModel:
val textColor = themeMap.getColor(android.R.attr.textColorPrimary) ?: Color.BLACK
val item = SettingItem(
    title = stringMap.getString(R.string.setting_pin).toRich().with(ForegroundColor(textColor)),
    ...
)
```
