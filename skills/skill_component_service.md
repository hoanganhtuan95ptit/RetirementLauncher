# Skill: Sử dụng ComponentService

## Mục đích

`ComponentService` là hệ thống **dependency injection theo lifecycle** — cho phép inject logic vào một Activity hoặc Fragment cụ thể (hoặc toàn bộ) mà **không cần sửa code của Activity/Fragment đó**. Mỗi service là một class độc lập, được đăng ký tự động qua `@AutoRegister` và được `ServiceInitializer` gọi đúng thời điểm lifecycle.

## Vị trí file

| File | Package |
|---|---|
| `ComponentService.kt` | `com.simple.launcher.retirement.utils.services` |
| `ServiceInitializer.kt` | `com.simple.launcher.retirement.utils.services` |

## Kiến trúc tổng quan

```
@AutoRegister(apis = [XxxService::class])
class MyService : XxxService {
    override fun setup(target) { /* logic */ }
}
```

`ServiceInitializer` (chạy qua `androidx.startup`) lắng nghe lifecycle của Application, Activity và Fragment. Mỗi khi lifecycle tương ứng được trigger, nó tự động collect tất cả service đã đăng ký qua `AutoRegisterManager` và gọi `setup()` theo thứ tự `priority()`.

## Danh sách interface có sẵn

| Interface | Lifecycle callback tương ứng | Tham số `setup()` |
|---|---|---|
| `ApplicationService` | Application khởi động | `Application` |
| `ActivityCreatedService` | `onActivityCreated` | `FragmentActivity` |
| `ActivityStartedService` | `onActivityStarted` | `FragmentActivity` |
| `ActivityResumedService` | `onActivityResumed` | `FragmentActivity` |
| `FragmentAttachedService` | `onFragmentAttached` | `Fragment` |
| `FragmentCreatedService` | `onFragmentCreated` | `Fragment` |
| `FragmentViewCreatedService` | `onFragmentViewCreated` | `Fragment` |
| `FragmentStartedService` | `onFragmentStarted` | `Fragment` |
| `FragmentResumedService` | `onFragmentResumed` | `Fragment` |

## Hai cách đăng ký với `@AutoRegister`

### 1. Đăng ký toàn cục — chạy trên **mọi** Activity/Fragment

Dùng khi muốn service chạy cho tất cả Activity hoặc Fragment thuộc loại đó.

```kotlin
// Chạy mỗi khi BẤT KỲ FragmentActivity nào được tạo
@AutoRegister(apis = [ActivityCreatedService::class])
class TrackingMainService : ActivityCreatedService {
    override fun setup(fragmentActivity: FragmentActivity) {
        // logic chạy trên mọi activity
    }
}
```

### 2. Đăng ký theo target cụ thể — chỉ chạy trên **một** Activity/Fragment nhất định

Dùng khi muốn service chỉ chạy cho một màn hình cụ thể.

```kotlin
// Chỉ chạy khi MainActivity được tạo
@AutoRegister(apis = [MainActivity::class])
class PocketModeMainService : ActivityCreatedService {
    override fun setup(fragmentActivity: FragmentActivity) {
        // logic chỉ dành cho MainActivity
    }
}

// Chỉ chạy khi SettingsFragment được tạo
@AutoRegister(apis = [SettingsFragment::class])
class CallBlockSettingService : FragmentCreatedService {
    override fun setup(fragment: Fragment) {
        // logic chỉ dành cho SettingsFragment
    }
}
```

> **Lưu ý:** Khi `apis` là một Activity/Fragment class (không phải interface), `ServiceInitializer` dùng `AutoRegisterManager.subscribe(componentCallbacks.javaClass.name, api)` để match đúng target.

## Ví dụ đầy đủ — Service inject vào Fragment

```kotlin
@AutoRegister(apis = [SettingsFragment::class])
class CallBlockSettingService : FragmentCreatedService {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var callBlockViewModel: CallBlockSettingViewModel

    override fun setup(fragment: Fragment) {
        settingsViewModel = fragment.viewModels<SettingsViewModel>().value
        callBlockViewModel = fragment.viewModels<CallBlockSettingViewModel>().value

        // Dùng launchCollect để observe Flow theo lifecycle của fragment
        callBlockViewModel.items.launchCollect(fragment) { items ->
            settingsViewModel.updateItem(SettingItem.ORDER_TOGGLE_CALL_BLOCK, items)
        }
    }
}
```

## Ví dụ đầy đủ — Service inject vào Activity

```kotlin
@AutoRegister(apis = [MainActivity::class])
class PocketModeMainService : ActivityCreatedService {

    override fun setup(fragmentActivity: FragmentActivity) {
        val sensorManager = fragmentActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Gắn lifecycle observer để register/unregister sensor đúng lúc
        fragmentActivity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) { /* register */ }
            override fun onPause(owner: LifecycleOwner)  { /* unregister */ }
        })
    }
}
```

## Priority — kiểm soát thứ tự thực thi

Nếu nhiều service cùng target, override `priority()` để chạy theo thứ tự mong muốn (số nhỏ hơn chạy trước).

```kotlin
@AutoRegister(apis = [ActivityCreatedService::class])
class EarlyInitService : ActivityCreatedService {
    override fun priority() = -10  // chạy trước
    override fun setup(fragmentActivity: FragmentActivity) { /* ... */ }
}

@AutoRegister(apis = [ActivityCreatedService::class])
class LateInitService : ActivityCreatedService {
    override fun priority() = 10   // chạy sau
    override fun setup(fragmentActivity: FragmentActivity) { /* ... */ }
}
```

## launchCollect — collect Flow theo lifecycle

`launchCollect` là extension function nội bộ trong package `services`, dùng để collect Flow gắn với lifecycle của target:

```kotlin
import com.simple.launcher.retirement.utils.services.launchCollect

someFlow.launchCollect(fragment) { value ->
    // tự động hủy khi fragment destroy
}
```

## Import cần thiết

```kotlin
import com.simple.auto.register.AutoRegister
import com.simple.launcher.retirement.utils.services.ApplicationService
import com.simple.launcher.retirement.utils.services.ActivityCreatedService
import com.simple.launcher.retirement.utils.services.ActivityStartedService
import com.simple.launcher.retirement.utils.services.ActivityResumedService
import com.simple.launcher.retirement.utils.services.FragmentAttachedService
import com.simple.launcher.retirement.utils.services.FragmentCreatedService
import com.simple.launcher.retirement.utils.services.FragmentViewCreatedService
import com.simple.launcher.retirement.utils.services.FragmentStartedService
import com.simple.launcher.retirement.utils.services.FragmentResumedService
import com.simple.launcher.retirement.utils.services.launchCollect
```

## Lưu ý quan trọng

- **Không inject logic trực tiếp vào Activity/Fragment** khi có thể tách ra thành service — giúp code dễ test và mở rộng.
- **Service phải stateless hoặc quản lý state cẩn thận**: `setup()` có thể được gọi nhiều lần (mỗi lần lifecycle trigger).
- **Dùng `fragment.viewModels<>()` để lấy ViewModel** trong Fragment service — ViewModel sẽ được share đúng scope.
- **Không lưu reference dài hạn** tới `FragmentActivity` hoặc `Fragment` trong service để tránh memory leak. Dùng `launchCollect` hoặc `lifecycle.addObserver` để tự động dọn dẹp.
