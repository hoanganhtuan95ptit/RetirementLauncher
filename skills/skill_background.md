# Skill: Sử dụng Background để tạo nền linh hoạt bằng code

## Mục đích

`Background` là một utility giúp tạo `GradientDrawable` (hình chữ nhật bo góc, có viền) một cách khai báo (declarative) từ code Kotlin, thay vì phải tạo file XML drawable. Kết hợp với `BackgroundBuilder`, nó cung cấp fluent API giúp code gọn gàng và dễ đọc.

## Vị trí

| File | Package |
|---|---|
| `Background.kt` | `com.simple.launcher.retirement.utils.background` |
| `BackgroundBuilder.kt` | `com.simple.launcher.retirement.utils.background` |

## Kiến trúc tổng quan

### Các class và hàm chính

| Thành phần | Mô tả |
|---|---|
| `Background` | Data class chứa các thuộc tính (màu nền, bo góc, viền) và tự tạo `GradientDrawable`. |
| `BackgroundBuilder` | Builder tạo `Background` bằng fluent API (chaining). |
| `Background.Builder()` | Companion function để khởi tạo builder. |
| `View.setBackground(Background?)` | Extension function gán `Background` vào `View`. |

### Thuộc tính của Background

| Thuộc tính | Kiểu | Mặc định | Mô tả |
|---|---|---|---|
| `backgroundColor` | `Int` | `Color.TRANSPARENT` | Màu nền. |
| `cornerRadius_TL` | `Int` | `0` | Bo góc trên-trái (px). |
| `cornerRadius_TR` | `Int` | `0` | Bo góc trên-phải (px). |
| `cornerRadius_BL` | `Int` | `0` | Bo góc dưới-trái (px). |
| `cornerRadius_BR` | `Int` | `0` | Bo góc dưới-phải (px). |
| `strokeWidth` | `Int` | `0` | Độ dày viền (px). |
| `strokeColor` | `Int` | `Color.TRANSPARENT` | Màu viền. |
| `strokeDashWidth` | `Int` | `0` | Độ dài nét đứt (px). |
| `strokeDashGap` | `Int` | `0` | Khoảng cách giữa các nét đứt (px). |

## Hướng dẫn sử dụng

Có **2 cách** tạo `Background`: dùng **constructor trực tiếp** hoặc dùng **Background.Builder()**.

---

### Cách 1: Constructor trực tiếp (phù hợp khi cần truyền nhiều tham số cụ thể)

```kotlin
val bg = Background(
    backgroundColor = Color.WHITE,
    cornerRadius_TL = DP.DP_24,
    cornerRadius_TR = DP.DP_24,
    cornerRadius_BL = 0,
    cornerRadius_BR = 0
)

binding.root.setBackground(bg)
```

---

### Cách 2: Background.Builder() (khuyến khích — gọn gàng, dễ đọc hơn)

Sử dụng `Background.Builder()` để tạo builder, rồi gọi chaining các phương thức, cuối cùng gọi `build()`.

#### API của BackgroundBuilder

| Method | Mô tả |
|---|---|
| `backgroundColor(color)` | Đặt màu nền. |
| `cornerRadius(radius)` | Bo tròn **tất cả 4 góc** cùng giá trị. |
| `cornerRadiusTop(radius)` | Bo tròn **2 góc trên** (TL + TR). |
| `cornerRadiusBottom(radius)` | Bo tròn **2 góc dưới** (BL + BR). |
| `cornerRadius(tl, tr, bl, br)` | Bo tròn **từng góc riêng lẻ**. |
| `stroke(width, color, dashWidth, dashGap)` | Đặt viền (solid hoặc dash). |
| `build()` | Tạo ra `Background` cuối cùng. |

#### 2.1. Background đơn giản — chỉ màu nền

```kotlin
val bg = Background.Builder()
    .backgroundColor(Color.WHITE)
    .build()
```

#### 2.2. Background với bo góc đều

```kotlin
val bg = Background.Builder()
    .backgroundColor(Color.WHITE)
    .cornerRadius(DP.DP_12)
    .build()
```

#### 2.3. Background với bo góc trên (BottomSheet)

```kotlin
val bg = Background.Builder()
    .backgroundColor(backgroundColor)
    .cornerRadiusTop(DP.DP_24)
    .build()
```

#### 2.4. Background với viền solid

```kotlin
val bg = Background.Builder()
    .backgroundColor(Color.TRANSPARENT)
    .cornerRadius(DP.DP_8)
    .stroke(width = DP.DP_1, color = Color.GRAY)
    .build()
```

#### 2.5. Background với viền nét đứt (dashed)

```kotlin
val bg = Background.Builder()
    .backgroundColor(Color.TRANSPARENT)
    .cornerRadius(DP.DP_8)
    .stroke(
        width = DP.DP_1,
        color = Color.GRAY,
        dashWidth = DP.DP_4,
        dashGap = DP.DP_2
    )
    .build()
```

#### 2.6. Background với bo góc không đều

```kotlin
val bg = Background.Builder()
    .backgroundColor(Color.WHITE)
    .cornerRadius(
        topLeft = DP.DP_16,
        topRight = DP.DP_16,
        bottomLeft = 0,
        bottomRight = 0
    )
    .build()
```

---

### Gán Background vào View

```kotlin
binding.root.setBackground(bg)
binding.btnSave.tvAction.setBackground(actionState.background)
```

## Ví dụ thực tế trong dự án

### Ví dụ 1: ActionState cho nút bấm

```kotlin
fun buildActionState(
    text: String,
    textColor: Int,
    backgroundColor: Int,
    cornerRadius: Int = DP.DP_12,
    strokeWidth: Int = 0,
    strokeColor: Int = Color.TRANSPARENT
): ActionState = ActionState(
    text = RichText.Builder(text)
        .with(ForegroundColor(textColor), TextSize(18), Bold)
        .build(),
    background = Background.Builder()
        .backgroundColor(backgroundColor)
        .cornerRadius(cornerRadius)
        .stroke(width = strokeWidth, color = strokeColor)
        .build()
)
```

### Ví dụ 2: BottomSheet với bo góc trên

```kotlin
fun buildBottomSheetState(
    backgroundColor: Int,
    anchorColor: Int,
    cornerRadius: Int = DP.DP_24
): BottomSheetState = BottomSheetState(
    background = Background.Builder()
        .backgroundColor(backgroundColor)
        .cornerRadiusTop(cornerRadius)
        .build(),
    anchorBackground = Background.Builder()
        .backgroundColor(anchorColor)
        .cornerRadius(100.toPx())
        .build()
)
```

### Ví dụ 3: SearchState với ô tìm kiếm bo góc

```kotlin
fun buildSearchState(
    hint: String,
    textColor: Int,
    hintColor: Int,
    backgroundColor: Int,
    cornerRadius: Int = DP.DP_12.toInt()
): SearchState = SearchState(
    hint = hint,
    textColor = textColor,
    hintColor = hintColor,
    background = Background.Builder()
        .backgroundColor(backgroundColor)
        .cornerRadius(cornerRadius)
        .build()
)
```

### Ví dụ 4: Background chỉ màu nền (ViewModel)

```kotlin
val background: StateFlow<Background> = themes.map { themeMap ->
    Background.Builder()
        .backgroundColor(themeMap.getColor(android.R.attr.windowBackground))
        .build()
}.stateIn(viewModelScope, SharingStarted.Eagerly, Background())
```

## Lưu ý quan trọng

- **Ưu tiên dùng `Background.Builder()`** — code rõ ràng hơn constructor khi có nhiều tham số mặc định.
- **Dùng constructor trực tiếp** khi cần truyền tất cả tham số một lúc hoặc khi dùng `copy()` trên data class.
- Các giá trị corner radius và stroke width là **pixel** — luôn dùng `DP.DP_xx` hoặc `Int.toPx()` để chuyển từ dp sang px.
- `Background` tự gọi `refresh()` trong `init` — drawable được tạo ngay khi khởi tạo.
- Luôn gọi `build()` ở cuối khi dùng Builder.
