# Skill: Common UI state đang dùng trong repo

## Mục tiêu

Skill này áp dụng cho các màn dùng `ToolbarState`, `ActionState`, `SearchState`, `Background`, `BottomSheetState`.

## 1. ToolbarState

Pattern thật trong `SettingsViewModel`:

```kotlin
val toolbar: StateFlow<ToolbarState> = combineState(
    resources,
    ToolbarState.empty()
) { resources ->
    val color = resources.textColorPrimary
    value = ToolbarState(
        title = buildToolbarTitle(resources.getString(R.string.settings_title), color),
        backIcon = buildBackIcon(color)
    )
}
```

Fragment chỉ bind:

```kotlin
binding.toolbar.tvTitle.setText(state.title)
binding.toolbar.ivLeft.setImage(state.backIcon)
```

## 2. Background toàn màn

`BaseViewModel` đã expose sẵn:

```kotlin
val background: StateFlow<Background>
```

Màn nào có nền thường nên observe và apply:

```kotlin
viewModel.background.observe(this) { background ->
    binding.root.setBackground(background)
}
```

`SettingsFragment` đang làm đúng pattern này.

## 3. ActionState

Action button vẫn dùng helper `buildActionState(...)`, nhưng skill này không nên giả định mọi màn đều có action. Chỉ dùng khi layout thực sự có `layout_action.xml`.

## 4. SearchState

Tương tự, chỉ dùng khi màn có `layout_search.xml`. Những màn như `Home` và `Settings` hiện tại không dùng `SearchState`.

## 5. BottomSheetState

`BaseBottomSheetDialogFragment` tự observe `viewModel.bottomSheet`, nên bottom sheet con không cần tự xử lý anchor/background nữa nếu dùng behavior mặc định.

## 6. Space item ở cuối list

Với `Settings`, khoảng trống cuối màn không làm bằng padding cứng ở Fragment. Nó đang được thêm như một `SpaceViewItem` trong `SettingsViewModel`, dựa trên `navigationBarHeight`.
