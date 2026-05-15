# Skill: Sử dụng Base UI Classes (Fragment, Dialog, BottomSheet)

## Mục đích

Cung cấp các class cơ bản (`BaseFragment`, `BaseDialogFragment`, `BaseBottomSheetDialogFragment`) để chuẩn hóa cách khởi tạo ViewBinding, quản lý lifecycle và giảm thiểu code lặp lại (boilerplate) trong layer Presentation.

## Kiến trúc tổng quan

Dự án sử dụng cơ chế Generics kết hợp với ViewBinding để tự động quản lý binding object.

### Các class cốt lõi

| Class | Package | Vai trò |
|---|---|---|
| `BaseFragment<VB>` | `com.simple.launcher.retirement.presentation.base` | Base class cho tất cả các Fragment thông thường |
| `BaseDialogFragment<VB>` | `com.simple.launcher.retirement.presentation.base` | Base class cho các DialogFragment |
| `BaseBottomSheetDialogFragment<VB, VM>` | `com.simple.launcher.retirement.presentation.base` | Base class cho các BottomSheet (Material Design) — yêu cầu 2 type params: ViewBinding và ViewModel |

## Hướng dẫn sử dụng

### Bước 1 — Kế thừa Base Class

Kế thừa class tương ứng và truyền vào class ViewBinding của layout.

```kotlin
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    // ...
}
```

### Bước 2 — Implement `inflateBinding`

Đây là hàm abstract bắt buộc phải implement để cung cấp instance của ViewBinding.

```kotlin
override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
    return FragmentHomeBinding.inflate(inflater, container, false)
}
```

### Bước 3 — Sử dụng các hàm Lifecycle chuẩn

Thay vì override `onViewCreated`, hãy sử dụng các hàm đã được template sẵn:

- `setupViews(view, savedInstanceState)`: Khởi tạo View, Adapter, Click Listener.
- `observeData()`: Đăng ký quan sát LiveData hoặc Flow.

```kotlin
override fun setupViews(view: View, savedInstanceState: Bundle?) {
    super.setupViews(view, savedInstanceState)
    binding.btnAction.setOnClickListener { /* ... */ }
}

override fun observeData() {
    super.observeData()
    // Dùng extension function Flow<T>.observe(Fragment) từ utils/lifecycle/LifecycleExt.kt
    // KHÔNG dùng viewLifecycleOwner.lifecycleScope.launch { ... } thủ công
    viewModel.data.observe(this) { /* ... */ }
}
```

## Các tính năng tự động

1.  **Quản lý Binding**: Class base tự động khởi tạo `_binding` trong `onCreateView` và gán về `null` trong `onDestroyView` để tránh memory leak.
2.  **Truy cập Binding**: Sử dụng property `binding` (non-null) để truy cập các view trong layout.
3.  **Lifecycle Hook**: Đảm bảo `observeData()` luôn được gọi sau `setupViews()`.

## Ví dụ đầy đủ (BottomSheet)

`BaseBottomSheetDialogFragment` có **2 type params**: `<VB : ViewBinding, VM : BaseViewModel>`. Subclass **bắt buộc** khai báo `override val viewModel: VM` — đây là property abstract mà base class dùng để tự observe `bottomSheet` state (background, anchor).

```kotlin
class MyBottomSheet : BaseBottomSheetDialogFragment<BottomSheetMyBinding, MyViewModel>() {

    // BẮT BUỘC: base class dùng viewModel để observe bottomSheet state
    override val viewModel: MyViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        BottomSheetMyBinding.inflate(inflater, container, false)

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)
        binding.btnConfirm.root.setOnSafeClickListener { /* ... */ }
    }

    override fun observeData() {
        super.observeData()
        // Dùng extension Flow<T>.observe(Fragment) — import từ utils.lifecycle.observe
        viewModel.action.observe(this) { state ->
            binding.btnConfirm.tvAction.setText(state.text)
            binding.btnConfirm.tvAction.setBackground(state.background)
        }
    }
}
```

**Lý do phải có `viewModel`**: `BaseBottomSheetDialogFragment.onViewCreated()` tự gọi `observeBottomSheetState()` — hàm này collect `viewModel.bottomSheet` để set background, bo góc, anchor color cho BottomSheet. Nếu thiếu property này sẽ lỗi compile.

## Ví dụ đầy đủ (Fragment)

```kotlin
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels { HomeViewModelFactory(...) }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)
        binding.btnAction.setOnSafeClickListener { /* ... */ }
    }

    override fun observeData() {
        super.observeData()
        viewModel.items.observe(this) { /* bind to RecyclerView */ }
    }
}
```

## Lưu ý quan trọng

- **Không override `onCreateView` hoặc `onViewCreated`** trừ khi thực sự cần thiết. Nếu override, phải gọi `super`.
- **Luôn sử dụng `binding`** thay vì `_binding` để tránh null check không cần thiết.
- **`BaseBottomSheetDialogFragment` yêu cầu 2 type params** `<VB, VM>` và property `abstract val viewModel: VM`. Thiếu một trong hai sẽ lỗi compile.
- **`BaseActivity`** cũng có sẵn trong gói `base` nhưng có signature hơi khác: `inflateBinding(inflater: LayoutInflater)` — không có `container`.
- **Để observe Flow trong Fragment/BottomSheet**, dùng extension function `Flow<T>.observe(this) { }` từ `com.simple.launcher.retirement.utils.lifecycle` — KHÔNG dùng `lifecycleScope.launch { collectLatest { } }` thủ công.

## Import cần thiết

```kotlin
import com.simple.launcher.retirement.utils.lifecycle.observe  // Flow<T>.observe(Fragment)
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener
import com.simple.launcher.retirement.utils.text.setText
import com.simple.launcher.retirement.utils.image.setImage
import com.simple.launcher.retirement.utils.background.setBackground
```
