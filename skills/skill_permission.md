# Skill: Sử dụng PermissionManager

## Mục đích

`PermissionManager` là object tập trung toàn bộ logic kiểm tra và yêu cầu quyền trong app. Thay vì xử lý permission trực tiếp trong Fragment/Activity, luôn dùng `PermissionManager` để đảm bảo nhất quán và tái sử dụng.

## Vị trí

| File | Package |
|---|---|
| `PermissionManager.kt` | `com.simple.launcher.retirement.utils.permission` |

---

## Các hàm kiểm tra (đồng bộ)

Dùng để **check trạng thái hiện tại** — không hiển thị UI, không suspend.

```kotlin
PermissionManager.hasFilePermission()         // Quyền đọc/ghi file
PermissionManager.hasUsageStatsPermission()   // Quyền xem thống kê app
PermissionManager.hasOverlayPermission()      // Quyền vẽ đè lên app khác
PermissionManager.hasCallBlockPermissions()   // Quyền chặn cuộc gọi
PermissionManager.hasContactPermission()      // Quyền đọc danh bạ
PermissionManager.isDefaultLauncher()         // Đã là launcher mặc định chưa
PermissionManager.hasPinPermission()          // Đã thiết lập PIN chưa
```

---

## Các hàm yêu cầu quyền (suspend)

Dùng khi cần **đảm bảo có quyền trước khi thực hiện action**. Mỗi hàm:
1. Nếu đã có quyền → trả về `true` ngay.
2. Nếu chưa → mở BottomSheet xin quyền qua deeplink, **chờ** kết quả từ `AppEventBus`.
3. Trả về `true` nếu được cấp, `false` nếu user huỷ.

```kotlin
suspend fun requireFilePermission(): Boolean
suspend fun requireUsageStatsPermission(): Boolean
suspend fun requireOverlayPermission(): Boolean
suspend fun requireCallBlockPermissions(): Boolean
suspend fun requireDefaultLauncher(): Boolean
suspend fun requirePinPermissions(): Boolean   // Verify nếu đã có PIN, Setup nếu chưa có
```

---

## Cách sử dụng

### Trong ViewModel (coroutine scope)

```kotlin
fun onUserRequestFeature() {
    viewModelScope.launch {
        if (!PermissionManager.requireFilePermission()) return@launch
        // Tiếp tục logic khi đã có quyền
        doSomethingWithFiles()
    }
}
```

### Trong ComponentService (với launchCollect hoặc coroutine)

```kotlin
AppEventBus.events.filterIsInstance<AppEvent.SettingClicked>().launchCollect(fragment) { event ->
    if (!PermissionManager.requireCallBlockPermissions()) return@launchCollect
    // Thực hiện action sau khi có quyền
}
```

### Kết hợp nhiều quyền

```kotlin
viewModelScope.launch {
    if (!PermissionManager.requireUsageStatsPermission()) return@launch
    if (!PermissionManager.requireOverlayPermission()) return@launch
    // Chỉ chạy khi có đủ cả hai quyền
    startMonitoring()
}
```

---

## Luồng hoạt động bên trong

```
requireXxxPermission()
    │
    ├─ hasXxxPermission() == true  →  return true (ngay lập tức)
    │
    └─ hasXxxPermission() == false
           │
           ├─ sendDeeplink(DeepLinks.PERMISSION_XXX)
           │       └─ mở BottomSheet tương ứng
           │
           └─ AppEventBus.events.filterIsInstance<AppEvent.PermissionResult>().first()
                   ├─ PermissionAccept  →  return true
                   └─ PermissionCancel →  return false
```

---

## Deeplink và BottomSheet tương ứng

| Hàm `require*` | Deeplink | BottomSheet |
|---|---|---|
| `requireFilePermission()` | `DeepLinks.PERMISSION_FILE` | `FilePermissionBottomSheet` |
| `requireUsageStatsPermission()` | `DeepLinks.PERMISSION_USAGE_STATS` | `UsageStatsPermissionBottomSheet` |
| `requireOverlayPermission()` | `DeepLinks.PERMISSION_OVERLAY` | `OverlayPermissionBottomSheet` |
| `requireCallBlockPermissions()` | `DeepLinks.PERMISSION_CALL_BLOCK` | `CallBlockPermissionBottomSheet` |
| `requireDefaultLauncher()` | `DeepLinks.PERMISSION_DEFAULT_LAUNCHER` | `DefaultLauncherBottomSheet` |
| `requirePinPermissions()` | `DeepLinks.PIN_SETUP` / `DeepLinks.PIN_VERIFY` | PIN BottomSheet |

---

## Thêm một loại permission mới

Khi cần thêm permission mới, làm theo 4 bước:

**Bước 1** — Thêm hàm check vào `PermissionManager`:
```kotlin
fun hasXxxPermission(): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.XXX) == PackageManager.PERMISSION_GRANTED
}
```

**Bước 2** — Thêm hàm require (suspend):
```kotlin
suspend fun requireXxxPermission(): Boolean {
    if (hasXxxPermission()) return true
    sendDeeplink(DeepLinks.PERMISSION_XXX)
    val result = AppEventBus.events.filterIsInstance<AppEvent.PermissionResult>().first()
    return result is AppEvent.PermissionAccept
}
```

**Bước 3** — Thêm deeplink constant vào `DeepLinks.kt`:
```kotlin
const val PERMISSION_XXX = "app://XxxPermission"
```

**Bước 4** — Tạo BottomSheet + DeeplinkHandler:
```kotlin
// BottomSheet post AppEvent khi user tương tác
override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    if (!permissionGranted) AppEventBus.post(AppEvent.PermissionCancel)
}

// DeeplinkHandler hiển thị BottomSheet
@Deeplink
class XxxPermissionDeeplinkHandler : DeeplinkHandler {
    override val deeplink = DeepLinks.PERMISSION_XXX
    override suspend fun navigate(...): Boolean {
        XxxPermissionBottomSheet().show(fragmentActivity.supportFragmentManager, TAG)
        return true
    }
}
```

---

## AppEvent liên quan

```kotlin
// Permission
AppEvent.PermissionAccept   // User đồng ý cấp quyền
AppEvent.PermissionCancel   // User từ chối / đóng BottomSheet

// PIN
AppEvent.PinSetupSuccess    // Thiết lập PIN thành công
AppEvent.PinVerifySuccess   // Xác thực PIN thành công
AppEvent.PinCancel          // User huỷ PIN
```

---

## Import cần thiết

```kotlin
import com.simple.launcher.retirement.utils.permission.PermissionManager
import com.simple.launcher.retirement.utils.AppEvent
import com.simple.launcher.retirement.utils.AppEventBus
import com.simple.launcher.retirement.presentation.DeepLinks
```

---

## Lưu ý quan trọng

- **Hàm `require*` là suspend** — chỉ gọi trong coroutine hoặc `launchCollect`.
- **Chỉ một `require*` chạy tại một thời điểm** — các hàm dùng `AppEventBus.events.first()` nên nếu gọi song song, event đầu tiên sẽ bị lấy bởi cả hai. Luôn gọi tuần tự.
- **BottomSheet phải post đúng event** (`PermissionAccept` hoặc `PermissionCancel`) khi dismiss để không làm treo coroutine đang chờ.
- **`requirePinPermissions()`** tự động phân biệt Setup vs Verify dựa trên `hasPinPermission()` — không cần xử lý thủ công.
