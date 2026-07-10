# Skill: Dùng BigText theo style hiện tại

## Mục tiêu

Trong project này, `BigText` chủ yếu được build ngay ở ViewModel hoặc trong `PrecomputedViewItem.buildDrawSpec(...)`. Adapter và Fragment chỉ bind.

## Hai pattern đang dùng

### 1. Cho màn Settings và các màn form

Build text trước rồi truyền vào `SettingItem`, `ToolbarState`, `ActionState`:

```kotlin
resources.getString(R.string.setting_app_list)
    .withStyleBodyLarge()
    .with(BigForegroundColor(resources.textColorPrimary))
    .build()
```

### 2. Cho Home precomputed items

Build text ngay trong `TextNode`:

```kotlin
entity.label.toBuilder()
    .with(BigBold, BigTextSize(18.sp()), BigForegroundColor(Color.WHITE))
    .build()
```

## Helper đang dùng nhiều

Từ `utils/text/Exts.kt`:

- `withStyleTitleLarge()`
- `withStyleBodyLarge()`
- `withStyleLabelLarge()`

Các helper này chỉ set size. Muốn màu hoặc bold thì chain thêm `.with(...)`.

## Lưu ý

- Nếu text đi qua adapter dạng thường, để `BigText` nằm trong `ViewItem`.
- Nếu item là `PrecomputedViewItem`, có thể build trực tiếp trong `TextNode`.
- Không để adapter tự convert `String -> BigText`.
