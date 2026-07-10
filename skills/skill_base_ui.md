# Skill: Base UI classes trong project này

## 1. `BaseFragment`

`BaseFragment<VB>` của repo này có `binding` nullable:

```kotlin
abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    var binding: VB? = null
}
```

Vì vậy trong `setupViews()` và `observeData()`, pattern an toàn là:

```kotlin
val binding = binding ?: return
```

Không viết skill theo kiểu `binding` non-null mặc định.

## 2. Lifecycle hooks chuẩn

Base class đang gọi:

1. `setupViews(view, savedInstanceState)`
2. `observeData()`

trong `onViewCreated()`.

Pattern đúng:

```kotlin
override fun setupViews(view: View, savedInstanceState: Bundle?) { ... }
override fun observeData() { ... }
```

## 3. Pattern thật của HomeFragment

`HomeFragment`:

- dùng `activityViewModels<HomeViewModel>()`
- set `GridLayoutManager(HomeItem.TOTAL_COLUMNS)`
- span size lookup đọc từ `MultiAdapter.currentList`
- observe `viewItemList.attachAdapter()`

Nó không tự xử lý toolbar, background hay event bus.

## 4. Pattern thật của SettingsFragment

`SettingsFragment`:

- dùng `viewModels<SettingsViewModel>()`
- tự bind `toolbar`
- tự bind `background`
- tự nghe `AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>()`
- tự quyết định deeplink và permission

Đây là màn tiêu biểu cho fragment điều phối, còn state/render list được bơm từ service + ViewModel.

## 5. `BaseBottomSheetDialogFragment`

Base bottom sheet của repo này:

- yêu cầu `protected abstract val viewModel: VM`
- tự observe `viewModel.bottomSheet`
- tự tạo anchor view
- tự setup edge-to-edge và insets

Vì vậy khi viết bottom sheet mới:

- chỉ cần bind content
- nếu cần đổi nền/anchor thì override `bottomSheet` trong ViewModel

## 6. Rule cần nhớ

- Ưu tiên `setupViews()` và `observeData()`, không dồn logic vào `onViewCreated()`.
- Trong fragment, luôn null-check `binding`.
- Dùng extension `observe(...)` của project thay vì tự collect thủ công nếu không có lý do rõ ràng.
