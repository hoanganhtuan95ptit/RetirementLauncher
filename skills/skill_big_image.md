# Skill: Dùng BigImage theo code hiện tại

## Mục tiêu

`BigImage` trong repo này được dùng theo 2 kiểu:

- Item thường của Settings: truyền `BigImage` đã build sẵn vào adapter.
- Item precomputed của Home: truyền `BigImage` hoặc `BigImageBuilder` vào `ImageNode`.

## Pattern trong Settings

```kotlin
icon = icon.toBuilder()
    .addTransform(ColorFilter(resources.colorOnPrimaryContainer))
    .build()
```

Adapter chỉ:

```kotlin
binding.ivSettingIcon.setImage(item.icon)
```

## Pattern trong Home

Ví dụ `ContactHomeItem`:

```kotlin
ImageNode(
    source = BigImageBuilder(entity.photoUri ?: R.drawable.ic_home_contact_24dp)
        .addTransform(CircleCrop)
        .build(),
    ...
)
```

Ví dụ `AppHomeItem`:

```kotlin
ImageNode(source = BigImage(entity.icon), ...)
```

## Lưu ý

- Nếu UI là `ImageView`, build `BigImage` ở ViewModel hoặc builder helper.
- Nếu UI là precomputed node, có thể build ngay trong `buildDrawSpec(resources)`.
- Color tint đang được áp chủ yếu qua `ColorFilter(...)`.
