# Skill: ComponentService pattern của Home và Settings

## Mục tiêu

Đây là kiến trúc rất quan trọng của repo hiện tại. `Home` và `Settings` đều được ghép từ nhiều service nhỏ thay vì dồn hết logic vào Fragment hoặc một ViewModel lớn.

## 1. Pattern thật của Home

`HomeService` là base class:

```kotlin
abstract class HomeService : FragmentViewCreatedService {
    protected lateinit var homeViewModel: HomeViewModel
    abstract fun setup(homeFragment: HomeFragment)
}
```

Service con:

- lấy ViewModel nhóm bằng `homeFragment.viewModels<...>().value`
- collect flow nhóm
- gọi `homeViewModel.updateItem(groupViewItem)`

Ví dụ:

```kotlin
@AutoRegister([HomeFragment::class])
class AppHomeService : HomeService() {
    override fun setup(homeFragment: HomeFragment) {
        val viewModel = homeFragment.viewModels<AppViewModel>().value
        viewModel.appViewItemList.filterNotNull().launchCollect(homeFragment.viewLifecycleOwner) {
            homeViewModel.updateItem(it)
        }
    }
}
```

## 2. Pattern thật của Settings

`SettingService` là base class:

```kotlin
abstract class SettingService : FragmentViewCreatedService {
    protected lateinit var settingsViewModel: SettingsViewModel
    abstract fun setup(settingsFragment: SettingsFragment)
}
```

Service con hiện đang active:

- `CommonSettingService`
- `ProtectSettingService`
- `AppMonitoringSettingService`

Pattern:

- ViewModel nhóm build `GroupViewItem`
- service collect
- service đẩy vào `settingsViewModel.updateItem(...)`

## 3. Helper builder cho Settings

`SettingService.kt` hiện còn chứa helper dùng chung:

- `settingItem(...)`
- `settingHeader(...)`

Khi thêm setting mới cho group hiện tại, ưu tiên tái dùng các helper này để giữ style đồng nhất.

## 4. Lifecycle nên dùng khi collect

Code hiện tại dùng cả hai:

- `launchCollect(settingsFragment.viewLifecycleOwner)`
- `launchCollect(settingsFragment)`

Ưu tiên `viewLifecycleOwner` khi flow chỉ liên quan tới view/list render.

## 5. Legacy services

Một số service cũ đã bị tắt `@AutoRegister`, ví dụ:

- `CallBlockSettingService`
- `FileCleanupSettingService`
- `PocketModeSettingService`
- `OptimizationSettingService`

Khi cập nhật tính năng, kiểm tra xem service đó đang active hay chỉ còn làm tài liệu tham khảo.

## 6. Rule cần giữ

- Service chịu trách nhiệm ghép section vào màn cha.
- ViewModel nhóm không tự gọi sang ViewModel cha.
- Fragment không nên tự manual compose từng section nếu đã có service pattern.
