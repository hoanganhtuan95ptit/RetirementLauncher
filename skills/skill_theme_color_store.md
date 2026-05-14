# Skill: Theme Color Management with ThemeColorStore

## 1. Overview
`ThemeColorStore` is a utility designed to dynamically retrieve and observe theme colors from Android's `R.attr`. It allows components (ViewModels, Adapters, Views) to access theme-defined colors easily and reactively without needing to manually resolve attributes from `Context` or `Resources` every time.

---

## 2. How it Works
1.  **Reflection Discovery**: It uses reflection to scan the app's `R.attr` class and identify all available theme attributes.
2.  **Attribute Resolution**: It resolves the actual color values for these attributes using the provided `Context` theme.
3.  **Centralized Storage**: Colors are stored in an internal map and exposed via a `StateFlow` for reactive updates.

---

## 3. Implementation Steps

### Step 1: Initialization
`ThemeColorStore` must be initialized once, typically in `Application.onCreate()` or the entry `Activity.onCreate()`, to load the theme colors.

```kotlin
// In your Application or MainActivity
ThemeColorStore.load(context)
```

### Step 2: Static Color Access
You can retrieve colors directly using the attribute name or the resource ID.

*   **Using Attribute ID (Type-safe):**
    ```kotlin
    val color = ThemeColorStore.getColor(R.attr.colorPrimary)
    ```

*   **Using Attribute Name (String):**
    ```kotlin
    val color = ThemeColorStore.getColor("colorAccent")
    ```

### Step 3: Reactive Usage in ViewModel
To make the UI reactive to theme changes, observe the `colorMapFlow`.

```kotlin
class HomeViewModel : ViewModel() {
    val primaryColor = ThemeColorStore.colorMapFlow
        .map { map -> map["colorPrimary"] ?: Color.BLACK }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Color.BLACK
        )
}
```

### Step 4: Direct Context Extension
If you have a `Context` and just need a one-off color resolution:
```kotlin
val color = context.getThemeColor(R.attr.colorSurface)
```

---

## 4. API Reference

### `ThemeColorStore` Object
*   **`fun load(context: Context)`**: Scans `R.attr` and populates the store with current theme colors.
*   **`fun getColor(attrName: String): Int?`**: Returns the color integer for a given attribute name (e.g., "colorPrimary").
*   **`fun getColor(@AttrRes attrId: Int): Int?`**: Returns the color integer for a given attribute resource ID.
*   **`val colorMapFlow: StateFlow<Map<String, Int>>`**: A flow that emits the entire color map whenever `load()` is called.

### Extensions
*   **`Context.getThemeColor(@AttrRes attrId: Int): Int`**: Resolves a theme attribute to a color integer directly from the context.

---

## 5. Important Notes & Edge Cases
*   **Call `load()` after Theme Changes**: If the app supports dynamic theme switching (e.g., Light/Dark mode or custom themes), you **must** call `ThemeColorStore.load(context)` again after the theme changes to refresh the stored values.
*   **ProGuard/R8**: If using `getColor(String)`, ensure that the `R.attr` class is not obfuscated or stripped by ProGuard, as it relies on reflection:
    ```proguard
    -keepclassmembers class **.R$attr {
        public static <fields>;
    }
    ```
*   **Null Safety**: `getColor()` returns an optional `Int?`. Always provide a fallback color (e.g., `Color.TRANSPARENT` or a hardcoded default) when using these values in UI components.
