# Skill: PermissionManager và luồng xin quyền hiện tại

## Mục tiêu

Xin quyền theo đúng flow đang chạy ở `Settings` và các bottom sheet permission.

## 1. `PermissionManager` là entry point duy nhất

Các hàm check hiện có:

- `hasFilePermission()`
- `hasUsageStatsPermission()`
- `hasOverlayPermission()`
- `hasCallBlockPermissions()`
- `hasContactPermission()`
- `hasPinPermission()`
- `hasCallPermission()`
- `isDefaultLauncher()`

Các hàm require hiện có:

- `requireFilePermission()`
- `requireUsageStatsPermission()`
- `requireOverlayPermission()`
- `requireCallBlockPermissions()`
- `requireDefaultLauncher()`
- `requirePinPermissions()`

## 2. Flow chung của `require*`

```kotlin
if (hasXxxPermission()) return true
return awaitPermissionResult(DeepLinks.PERMISSION_XXX)
```

Helper `awaitPermissionResult` / `awaitEventAfterDeeplink` subscribe vào `AppEventBus`
TRƯỚC rồi mới gửi deeplink (qua `onSubscription`):

```kotlin
AppEventBus.events
    .onSubscription { sendDeeplink(deeplink) }
    .filterIsInstance<T>()
    .first()
```

Không được gửi deeplink trước rồi mới subscribe — nếu bottom sheet trả kết quả quá nhanh
sẽ lỡ event và coroutine treo vĩnh viễn ở `first()`.

PIN là ngoại lệ:

```kotlin
if (hasPinPermission()) sendDeeplink(DeepLinks.PIN_VERIFY)
else sendDeeplink(DeepLinks.PIN_SETUP)
```

## 3. Pattern thật ở Settings

### Default launcher

`SettingsFragment` gọi:

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    PermissionManager.requireDefaultLauncher()
}
```

### Emergency call

Đây là trường hợp đặc biệt:

- Bật tính năng: xin `CALL_PHONE` bằng `registerForActivityResult(...)`, không đi qua bottom sheet.
- Tắt tính năng: bắt buộc verify PIN qua `PermissionManager.requirePinPermissions()`.

Không gom trường hợp này vào pattern `requireCallBlockPermissions()`.

## 4. Rule quan trọng

- Không chạy song song nhiều `require*` vì chúng cùng chờ event từ `AppEventBus`.
- Bottom sheet permission phải post đúng `PermissionAccept` hoặc `PermissionCancel`.
- Màn PIN phải post đúng `PinSetupSuccess`, `PinVerifySuccess`, hoặc `PinCancel`.
- Nếu action là tắt một tính năng bảo vệ, thường phải verify PIN trước.
