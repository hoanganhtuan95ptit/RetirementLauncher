# Skill: Deeplink pattern trong RetirementLauncher

## Mục tiêu

Điều hướng bằng `@Deeplink` đúng với cách `Home` và `Settings` đang làm.

## 1. Nơi khai báo deeplink

Project đang tập trung toàn bộ deeplink string ở:

`app/src/main/java/com/simple/launcher/retirement/presentation/DeepLinks.kt`

Khi thêm route mới:

1. thêm constant vào `DeepLinks`
2. nếu cần extras dùng chung, thêm vào `DeepLinks.Extras`
3. tạo `@Deeplink` handler

## 2. Pattern handler hiện tại

### Handler thay fragment

Ví dụ `SettingsDeeplinkHandler`:

```kotlin
@Deeplink
class SettingsDeeplinkHandler : DeeplinkHandler {
    override val deeplink: String = DeepLinks.SETTINGS

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {
        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SettingsFragment())

        if (extras?.get("addToBackStack") == true) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
        return true
    }
}
```

### Handler dọn back stack

`HomeDeeplinkHandler` hiện không replace fragment mới. Nó pop toàn bộ back stack và remove fragment hiện tại để quay về màn Home gốc của `activity_main.xml`.

## 3. Cách gọi deeplink

Pattern hiện tại:

```kotlin
sendDeeplink(DeepLinks.APP, mapOf("entity" to item.entity))
sendDeeplinkWithBackStack(DeepLinks.APP_LIST)
```

`sendDeeplinkWithBackStack(...)` là helper của project, không phải của library gốc.

## 4. Rule thực tế

- Nếu route là màn cấp 2 từ Settings, thường gọi `sendDeeplinkWithBackStack(...)`.
- Nếu click item Home dẫn đến màn hành động cụ thể, adapter/precomputed adapter có thể gọi `sendDeeplink(...)` trực tiếp.
- Đừng hardcode string `"app://..."` rải rác ngoài `DeepLinks.kt`.
