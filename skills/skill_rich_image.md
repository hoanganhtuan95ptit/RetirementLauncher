# Skill: Sử dụng RichImage để hiển thị hình ảnh linh hoạt

## Mục đích

`RichImage` là một hệ thống các data class được chuẩn hóa để đại diện cho nhiều nguồn hình ảnh khác nhau (Resource ID, File Path, Drawable, v.v.). Kết hợp với extension function `setImage()`, nó giúp việc hiển thị hình ảnh trên `ImageView` trở nên nhất quán và dễ dàng hơn bằng cách sử dụng Glide.

## Kiến trúc tổng quan

`RichImage` được thiết kế dưới dạng `sealed class` để đảm bảo tính an toàn về kiểu (type-safety) và dễ dàng quản lý các nguồn dữ liệu khác nhau thông qua `RichImageData`.

### Các class chính

| Class | Mô tả |
|---|---|
| `RichImage` | Base sealed class cho tất cả các loại ảnh. |
| `RichImageData` | Sealed class trung gian đại diện cho các nguồn ảnh có dữ liệu (`data`). |
| `ImageRes(data: Int, colorFilter: Int)` | Đại diện cho ảnh từ Resource (ví dụ: `R.drawable.ic_home`). |
| `ImagePath(data: String)` | Đại diện cho ảnh từ đường dẫn file cục bộ hoặc URL. |
| `ImageDrawable(data: Drawable)` | Đại diện cho ảnh từ đối tượng `Drawable` có sẵn. |
| `ImageView.setImage(source, ...)` | Extension function để load `RichImage` vào `ImageView` sử dụng Glide. |

## Hướng dẫn sử dụng

### 1. Hiển thị ảnh từ Resource

```kotlin
val image = ImageRes(R.drawable.ic_launcher_foreground)
imageView.setImage(image)
```

### 2. Hiển thị ảnh từ đường dẫn File hoặc URL

```kotlin
val image = ImagePath("/storage/emulated/0/Download/image.jpg")
imageView.setImage(image)
```

### 3. Hiển thị ảnh từ Drawable

```kotlin
// Giả sử bạn đã có một đối tượng Drawable
val image = ImageDrawable(myDrawable)
imageView.setImage(image)
```

### 4. Sử dụng với Transformations (Glide)

Bạn có thể truyền thêm các transformation của Glide như `CircleCrop`, `RoundedCorners`, v.v.

```kotlin
val image = ImageRes(R.drawable.avatar)
imageView.setImage(image, CircleCrop())
```

## Tại sao nên dùng RichImage?

1.  **Tính trừu tượng**: UI layer không cần biết ảnh đến từ đâu, chỉ cần nhận vào một object `RichImage`.
2.  **Đa dạng nguồn dữ liệu**: Hỗ trợ linh hoạt từ Resource ID, String Path cho đến trực tiếp Drawable.
3.  **Nhất quán**: Toàn bộ ứng dụng sử dụng chung một cơ chế load ảnh qua Glide, tự động tối ưu hóa việc transform và hiển thị.
4.  **Tương thích MultiAdapter**: Rất phù hợp để làm một trường trong `ViewItem` của RecyclerView.

## Ví dụ trong RecyclerView

```kotlin
data class AppHomeItem(
    val name: String,
    val icon: RichImage
) : ViewItem {
    // Implement ViewItem methods...
}

// Trong Adapter:
binding.ivIcon.setImage(item.icon)
```
