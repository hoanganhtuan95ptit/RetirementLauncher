# Skill: Sử dụng BigImage để hiển thị hình ảnh linh hoạt

## Mục đích

`BigImage` là một data class chuẩn hóa để đại diện cho nhiều nguồn hình ảnh khác nhau (Resource ID, File Path, Drawable, v.v.). Kết hợp với extension function `setImage()`, nó giúp việc hiển thị hình ảnh trên `ImageView` trở nên nhất quán và dễ dàng hơn bằng cách sử dụng Glide.

## Kiến trúc tổng quan

`BigImage` được thiết kế để chứa nguồn ảnh và các transformation cần thiết.

### Các class chính

 Class | Mô tả |
---|---|
 `BigImage` | Data class chứa source, error, placeholder và danh sách transforms. |
 `BigImageTransform` | Interface cho các loại transform (CircleCrop, RoundedCorners, ColorFilter, ...). |
 `ImageView.setImage(image: BigImage)` | Extension function để load `BigImage` vào `ImageView` sử dụng Glide. |

## Hướng dẫn sử dụng

### 1. Hiển thị ảnh từ Resource

```kotlin
val image = BigImage(R.drawable.ic_launcher_foreground)
imageView.setImage(image)
```

Hoặc dùng helper:
```kotlin
val image = R.drawable.ic_launcher_foreground.toBigImage()
```

### 2. Hiển thị ảnh kèm Color Filter

```kotlin
val image = R.drawable.ic_back.toBigImage(colorFilter = textColor)
```

### 3. Hiển thị ảnh từ đường dẫn File hoặc URL

```kotlin
val image = BigImage("/storage/emulated/0/Download/image.jpg")
// Hoặc
val image = "https://example.com/image.jpg".toBigImage()
```

### 4. Hiển thị ảnh từ Drawable

```kotlin
val image = BigImage(myDrawable)
```

### 5. Sử dụng với Transformations

Sử dụng `toBuilder()` để thêm các transformation.

```kotlin
val image = R.drawable.avatar.toBuilder()
    .addTransform(CircleCrop)
    .build()
```

## Các Transformations có sẵn (`com.simple.ui.precompute.image`)

 Transform | Mô tả |
---|---|
 `CircleCrop` | Cắt ảnh hình tròn. |
 `CropSquare` | Cắt ảnh hình vuông. |
 `RoundedCorners(radiusPx, marginPx, cornerType)` | Bo góc ảnh. |
 `ColorFilter(color)` | Áp bộ lọc màu (PorterDuff.Mode.SRC_ATOP). |
 `Blur(radius, sampling)` | Làm mờ ảnh. |
 `Grayscale` | Chuyển ảnh sang trắng đen. |

## Tại sao nên dùng BigImage?

1.  **Tính trừu tượng**: UI layer không cần biết ảnh đến từ đâu, chỉ cần nhận vào một object `BigImage`.
2.  **Đa dạng nguồn dữ liệu**: Hỗ trợ linh hoạt từ Resource ID, String Path cho đến trực tiếp Drawable.
3.  **Nhất quán**: Toàn bộ ứng dụng sử dụng chung một cơ chế load ảnh qua Glide.
4.  **Tương thích MultiAdapter**: Rất phù hợp để làm một trường trong `ViewItem` của RecyclerView.

## Ví dụ trong RecyclerView

```kotlin
data class AppHomeItem(
    val name: String,
    val icon: BigImage
) : ViewItem {
    // Implement ViewItem methods...
}

// Trong Adapter:
binding.ivIcon.setImage(item.icon)
```

## Import cần thiết

```kotlin
import com.simple.ui.precompute.image.BigImage
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.image.toBigImage
import com.simple.ui.precompute.image.toBuilder
import com.simple.ui.precompute.image.ColorFilter
import com.simple.ui.precompute.image.CircleCrop
import com.simple.ui.precompute.image.RoundedCorners
```
