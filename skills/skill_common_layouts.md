# Skill: Sử dụng Layout Common (Toolbar và Action Button)

## Mục đích
Chuẩn hóa cách sử dụng các layout dùng chung như `layout_toolbar` và `layout_action` để đảm bảo tính đồng nhất về giao diện (Theme, Localization) và giảm thiểu code lặp lại.

---

## 1. Layout Toolbar (`layout_toolbar.xml`)

Sử dụng cho thanh tiêu đề ở đầu mỗi màn hình.

### Cách dùng trong Layout (XML)
```xml
<include
    android:id="@+id/toolbar"
    layout="@layout/layout_toolbar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

### Cách dùng trong ViewModel
Sử dụng `ToolbarState` (kết hợp với `strings` và `themes`) để quản lý tiêu đề và icon back.
```kotlin
val toolbar: StateFlow<ToolbarState> = combine(strings, themes) { stringMap, themeMap ->
    val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: Color.BLACK
    ToolbarState(
        title = stringMap.getString(R.string.your_title).toRich().with(ForegroundColor(color), Bold),
        backIcon = RichImage(R.drawable.ic_back, color)
    )
}.stateIn(viewModelScope, SharingStarted.Eagerly, ToolbarState())
```

### Cách dùng trong Fragment/BottomSheet
```kotlin
override fun setupViews(view: View, savedInstanceState: Bundle?) {
    binding.toolbar.ivLeft.setOnSafeClickListener {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}

override fun observeData() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewModel.toolbar.collectLatest { state ->
            binding.toolbar.tvTitle.setText(state.title)
            state.backIcon?.let {
                binding.toolbar.ivLeft.visibility = View.VISIBLE
                binding.toolbar.ivLeft.setImage(it)
            }
        }
    }
}
```

---

## 2. Layout Action Button (`layout_action.xml`)

Sử dụng cho các nút bấm chính (nút lưu, nút xác nhận, nút dọn dẹp).

### Cách dùng trong Layout (XML)
```xml
<include
    android:id="@+id/btnSubmit"
    layout="@layout/layout_action"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

### Cách dùng trong ViewModel
Sử dụng hàm `buildActionState` từ `BaseViewModel`.
```kotlin
val action: StateFlow<ActionState> = combine(strings, themes) { stringMap, themeMap ->
    val color = themeMap.getColor(android.R.attr.textColorPrimary) ?: Color.BLACK
    val backgroundColor = themeMap.getColor(android.R.attr.colorControlHighlight) ?: Color.LTGRAY

    buildActionState(
        text = stringMap.getString(R.string.save),
        textColor = color,
        backgroundColor = backgroundColor,
        cornerRadius = DP.DP_12.toInt(),
        strokeWidth = 2, // Tùy chọn
        strokeColor = color // Tùy chọn
    )
}.stateIn(viewModelScope, SharingStarted.Eagerly, ActionState.empty())
```

### Cách dùng trong Fragment/BottomSheet
```kotlin
override fun setupViews(view: View, savedInstanceState: Bundle?) {
    binding.btnSubmit.root.setOnSafeClickListener {
        // Xử lý sự kiện click
    }
}

override fun observeData() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewModel.action.collectLatest { state ->
            binding.btnSubmit.tvAction.setText(state.text)
            binding.btnSubmit.tvAction.setBackground(state.background)
        }
    }
}
```

## Lưu ý quan trọng
- **Sự kiện Click**: Luôn gán vào `binding.btnXxx.root` (là container của nút).
- **Hiển thị**: Luôn dùng `setText(state.text)` và `setBackground(state.background)` để đảm bảo hỗ trợ đầy đủ RichText và Background (cornerRadius, stroke).
- **Phân tách logic**: ViewModel chịu trách nhiệm tính toán màu sắc và nội dung, Fragment chỉ chịu trách nhiệm hiển thị.
