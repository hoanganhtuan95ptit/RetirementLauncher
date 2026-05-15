# Skill: Sử dụng Layout Common (Toolbar, Action, Search và BottomSheet)

## Mục đích
Chuẩn hóa cách sử dụng các layout và state dùng chung để đảm bảo tính đồng nhất về giao diện (Theme, Localization) và tách biệt logic hiển thị giữa ViewModel và Fragment.

---

## 1. ToolbarState (`layout_toolbar.xml`)

Sử dụng cho thanh tiêu đề ở đầu mỗi màn hình.

### Cách dùng trong ViewModel
Sử dụng `buildToolbarTitle` và `buildBackIcon` để tạo state.
```kotlin
val toolbar: StateFlow<ToolbarState> = combine(strings, themes) { stringMap, themeMap ->
    val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: Color.BLACK
    val title = buildToolbarTitle(stringMap.getString(R.string.title_res), color)
    ToolbarState(title = title, backIcon = buildBackIcon(color))
}.stateIn(viewModelScope, SharingStarted.Eagerly, ToolbarState.empty())
```

### Cách dùng trong Fragment/BottomSheet
```kotlin
viewModel.toolbar.observe(this) { state ->
    binding.toolbar.tvTitle.setText(state.title)
    state.backIcon?.let {
        binding.toolbar.ivLeft.visibility = View.VISIBLE
        binding.toolbar.ivLeft.setImage(it)
    } ?: run {
        binding.toolbar.ivLeft.visibility = View.GONE
    }
}
```

---

## 2. ActionState (`layout_action.xml`)

Sử dụng cho các nút bấm chính (Lưu, Xác nhận, Dọn dẹp).

### Cách dùng trong ViewModel
Sử dụng hàm `buildActionState`.
```kotlin
val action: StateFlow<ActionState> = combine(strings, themes) { stringMap, themeMap ->
    val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: Color.BLACK
    val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight) ?: Color.LTGRAY

    buildActionState(
        text = stringMap.getString(R.string.action_res),
        textColor = color,
        backgroundColor = backgroundColor
    )
}.stateIn(viewModelScope, SharingStarted.Eagerly, ActionState.empty())
```

### Cách dùng trong Fragment/BottomSheet
```kotlin
viewModel.action.observe(this) { state ->
    binding.btnSubmit.tvAction.setText(state.text)
    binding.btnSubmit.tvAction.setBackground(state.background)
}
```

---

## 3. SearchState (`layout_search.xml`)

Sử dụng cho ô tìm kiếm.

### Cách dùng trong ViewModel
Sử dụng hàm `buildSearchState`.
```kotlin
val searchState: StateFlow<SearchState> = combine(strings, themes) { stringMap, themeMap ->
    val textColor = themeMap.getColor(android.R.attr.textColorPrimary) ?: Color.BLACK
    val hintColor = themeMap.getColor(android.R.attr.textColorSecondary) ?: Color.GRAY
    val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight) ?: Color.LTGRAY

    buildSearchState(
        hint = stringMap.getString(R.string.search),
        textColor = textColor,
        hintColor = hintColor,
        backgroundColor = backgroundColor
    )
}.stateIn(viewModelScope, SharingStarted.Eagerly, SearchState.empty())
```

### Cách dùng trong Fragment/BottomSheet
```kotlin
viewModel.searchState.observe(this) { state ->
    binding.layoutSearch.root.setBackground(state.background)
    binding.layoutSearch.etSearch.hint = state.hint
    binding.layoutSearch.etSearch.setHintTextColor(state.hintColor)
    binding.layoutSearch.etSearch.setTextColor(state.textColor)
    state.clearIcon?.let { binding.layoutSearch.ivClear.setImage(it) }
}
```

---

## 4. BottomSheetState

Sử dụng để quản lý background và thanh kéo (anchor) của BottomSheet.

### Cách dùng trong ViewModel
Thường được khai báo trong `BaseViewModel` hoặc override nếu cần tùy chỉnh.
```kotlin
override val bottomSheet: StateFlow<BottomSheetState> = themes.map { themeMap ->
    val backgroundColor = themeMap.getColor(android.R.attr.colorBackground) ?: Color.WHITE
    val anchorColor = themeMap.getColor(android.R.attr.textColorSecondary) ?: Color.LTGRAY

    buildBottomSheetState(
        backgroundColor = backgroundColor,
        anchorColor = anchorColor,
        showAnchor = true
    )
}.stateIn(viewModelScope, SharingStarted.Eagerly, BottomSheetState.empty())
```

### Cách dùng trong BottomSheet
`BaseBottomSheetDialogFragment` đã tự động xử lý việc observe và apply `bottomSheet` state vào UI. Các class con chỉ cần kế thừa và sử dụng.

---

---

## 5. Background (màu nền toàn màn hình)

`BaseViewModel` expose sẵn `background: StateFlow<Background>` lấy màu từ `android.R.attr.colorBackground`. **Mọi Fragment đều phải observe và apply** vào root view.

### Cách dùng trong Fragment
```kotlin
// KHÔNG cần khai báo gì trong ViewModel — đã có sẵn từ BaseViewModel
viewModel.background.observe(this) { background ->
    binding.root.setBackground(background)
}
```

ViewModel con **không cần override** `background` trừ khi màn hình cần màu nền khác.

---

## Lưu ý quan trọng
- **Tách biệt logic**: ViewModel tính toán màu sắc (từ `themes`) và nội dung (từ `strings`), Fragment chỉ nhận State và bind vào View.
- **Tiện ích mở rộng**: Luôn sử dụng các extension functions như `.setText(richText)`, `.setImage(richImage)`, `.setBackground(background)` để đảm bảo hỗ trợ đầy đủ các thuộc tính custom.
- **Sự kiện Click**: Đối với `layout_action`, luôn gán sự kiện click vào `binding.btnXxx.root`.
- **`.observe(this)` là extension function tùy chỉnh**: Không phải LiveData's `.observe()`. Đây là `Flow<T>.observe(Fragment)` từ `com.simple.launcher.retirement.utils.lifecycle` — nội bộ dùng `viewLifecycleOwner.lifecycleScope.launch { collectLatest { } }`. Import: `import com.simple.launcher.retirement.utils.lifecycle.observe`.
