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
| `BaseBottomSheetDialogFragment<VB>` | `com.simple.launcher.retirement.presentation.base` | Base class cho các BottomSheet (Material Design) |

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
    viewModel.data.observe(viewLifecycleOwner) { /* ... */ }
}
```

## Các tính năng tự động

1.  **Quản lý Binding**: Class base tự động khởi tạo `_binding` trong `onCreateView` và gán về `null` trong `onDestroyView` để tránh memory leak.
2.  **Truy cập Binding**: Sử dụng property `binding` (non-null) để truy cập các view trong layout.
3.  **Lifecycle Hook**: Đảm bảo `observeData()` luôn được gọi sau `setupViews()`.

## Ví dụ đầy đủ (BottomSheet)

```kotlin
class MyBottomSheet : BaseBottomSheetDialogFragment<BottomSheetMyBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        BottomSheetMyBinding.inflate(inflater, container, false)

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)
        binding.tvTitle.text = "Hello World"
    }

    override fun observeData() {
        super.observeData()
        // Collect flows here
    }
}
```

## Lưu ý quan trọng

- **Không override `onCreateView` hoặc `onViewCreated`** trừ khi thực sự cần thiết. Nếu override, phải gọi `super`.
- **Luôn sử dụng `binding`** thay vì `_binding` để tránh null check không cần thiết.
- **`BaseActivity`** cũng có sẵn trong gói `base` nhưng có signature hơi khác (không có `container` trong `inflateBinding`).
