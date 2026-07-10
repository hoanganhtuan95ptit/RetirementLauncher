# Skill: Dùng kích thước dp trong project này

## Mục tiêu

Repo đang có 2 cách dùng dp, và cả hai đều xuất hiện trong code `Home`/`Settings`.

## 1. `Int.dp()` / `Float.dp()`

Đây là cách được dùng nhiều nhất trong Home precomputed nodes:

```kotlin
8.dp().toInt()
24.dp()
18.sp()
```

File:

- `utils/exts/Exts.kt`

Phù hợp khi đang build `LayoutNode`, `DrawSpec`, `TextNode`.

## 2. `DP.DP_xx`

Đây là cách đang dùng nhiều ở settings/background helpers:

```kotlin
cornerRadius(DP.DP_24)
Padding(DP.DP_16, DP.DP_24, DP.DP_16, DP.DP_8)
```

File:

- `utils/size/DP.kt`

Phù hợp khi API cần giá trị ổn định, dùng lặp lại nhiều.

## 3. Quy ước nên theo

- Với precomputed UI của Home: ưu tiên `8.dp().toInt()`, `18.sp()`.
- Với helper/background/padding reusable: ưu tiên `DP.DP_xx`.
- Không hardcode px.
