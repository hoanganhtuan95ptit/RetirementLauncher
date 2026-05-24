# Skill: Sử dụng Object DP (Đơn vị kích thước)

## Mục đích

Cung cấp các hằng số kích thước **dp đã chuyển đổi sang pixel** (px) thông qua object `DP`, giúp sử dụng kích thước nhất quán trong code Kotlin (đặt margin, padding, radius bằng code thay vì XML).

## Vị trí

| File | Package |
|---|---|
| `DP.kt` | `com.simple.launcher.retirement.utils.size` |
| `toPx.kt` | `com.simple.launcher.retirement.utils.size` |

## Cách hoạt động

`DP` là một `object` chứa các property `lazy` — mỗi giá trị được khởi tạo lần đầu khi được truy cập và chuyển đổi từ dp sang px thông qua extension function `toPx()`.

```kotlin
// toPx.kt
fun Int.toPx(): Int = toFloat().toPx().toInt()

fun Float.toPx(): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)
```

## Danh sách hằng số có sẵn

| Hằng số | Giá trị dp |
|---|---|
| `DP.DP_05` | 0.5 dp |
| `DP.DP_1` | 1 dp |
| `DP.DP_2` | 2 dp |
| `DP.DP_4` | 4 dp |
| `DP.DP_6` | 6 dp |
| `DP.DP_8` | 8 dp |
| `DP.DP_10` | 10 dp |
| `DP.DP_12` | 12 dp |
| `DP.DP_14` | 14 dp |
| `DP.DP_16` | 16 dp |
| `DP.DP_18` | 18 dp |
| `DP.DP_20` | 20 dp |
| `DP.DP_22` | 22 dp |
| `DP.DP_24` | 24 dp |
| `DP.DP_26` | 26 dp |
| `DP.DP_28` | 28 dp |
| `DP.DP_30` | 30 dp |
| `DP.DP_32` | 32 dp |
| `DP.DP_34` | 34 dp |
| `DP.DP_36` | 36 dp |
| `DP.DP_38` | 38 dp |
| `DP.DP_40` | 40 dp |
| `DP.DP_42` | 42 dp |
| `DP.DP_44` | 44 dp |
| `DP.DP_46` | 46 dp |
| `DP.DP_48` | 48 dp |
| `DP.DP_50` | 50 dp |
| `DP.DP_52` | 52 dp |
| `DP.DP_54` | 54 dp |
| `DP.DP_56` | 56 dp |
| `DP.DP_58` | 58 dp |
| `DP.DP_60` | 60 dp |
| `DP.DP_62` | 62 dp |
| `DP.DP_64` | 64 dp |
| `DP.DP_66` | 66 dp |
| `DP.DP_68` | 68 dp |
| `DP.DP_70` | 70 dp |
| `DP.DP_72` | 72 dp |
| `DP.DP_74` | 74 dp |
| `DP.DP_76` | 76 dp |
| `DP.DP_78` | 78 dp |
| `DP.DP_80` | 80 dp |
| `DP.DP_82` | 82 dp |
| `DP.DP_84` | 84 dp |
| `DP.DP_86` | 86 dp |
| `DP.DP_88` | 88 dp |
| `DP.DP_90` | 90 dp |
| `DP.DP_92` | 92 dp |
| `DP.DP_94` | 94 dp |
| `DP.DP_96` | 96 dp |
| `DP.DP_98` | 98 dp |
| `DP.DP_100` | 100 dp |
| `DP.DP_102` | 102 dp |
| `DP.DP_104` | 104 dp |
| `DP.DP_106` | 106 dp |
| `DP.DP_108` | 108 dp |
| `DP.DP_350` | 350 dp |

## Hướng dẫn sử dụng

### Import

```kotlin
import com.simple.launcher.retirement.utils.size.DP
```

### Ví dụ — Đặt kích thước View bằng code

```kotlin
// Đặt corner radius
view.background = GradientDrawable().apply {
    cornerRadius = DP.DP_12.toFloat()
}

// Đặt margin hoặc padding
val params = view.layoutParams as ViewGroup.MarginLayoutParams
params.setMargins(DP.DP_16, DP.DP_8, DP.DP_16, DP.DP_8)
view.layoutParams = params
```

### Ví dụ — Dùng trong custom View (Canvas)

```kotlin
canvas.drawCircle(cx, cy, DP.DP_24.toFloat(), paint)
```

### Ví dụ — Dùng `toPx()` trực tiếp khi không có sẵn hằng số

Nếu cần một giá trị dp không có trong `DP`, dùng extension function:

```kotlin
import com.simple.launcher.retirement.utils.size.toPx

val customPx = 45.toPx()       // Int → px (Int)
val customPxF = 45f.toPx()     // Float → px (Float)
```

## Lưu ý quan trọng

- **Không hardcode giá trị px** — luôn dùng `DP.DP_xx` hoặc `Int/Float.toPx()` để đảm bảo scale đúng trên mọi màn hình.
- Các property trong `DP` là `lazy`, nghĩa là chúng **chỉ được tính toán một lần** khi lần đầu truy cập — rất hiệu quả khi dùng lặp lại.
- `DP.DP_xx` trả về kiểu `Int` (trừ `DP_05` trả về `Float`). Khi API yêu cầu `Float`, dùng `.toFloat()`.
