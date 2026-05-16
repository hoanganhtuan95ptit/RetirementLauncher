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
To make the UI reactive to theme changes, observe the `colorMapFlow` (exposed as `themes` in `BaseViewModel`). Use the `getColor` extension for `Map` to access colors via resource IDs safely.

```kotlin
class HomeViewModel : BaseViewModel() {
    val items = combine(strings, themes) { stringMap, themeMap ->
        val textColor = themeMap.getColor(android.R.attr.textColorPrimary)
        // Build your items here
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
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
*   **`val colorMapFlow: StateFlow<Map<String, Int>>`**: A flow that emits the entire color map whenever `load()` is called.

### Extensions
*   **`Map<String, Int>.getColor(@AttrRes attrId: Int, @ColorInt defaultColor: Int = Color.BLACK): Int`**: **(Recommended)** Resolves color from the map using attribute ID. It handles both system (`android.R.attr`) and app attributes. Returns `defaultColor` if attribute is not found.
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
*   **Null Safety**: `getColor()` has a default value (default is `Color.BLACK`). You can override it by passing a second argument: `themeMap.getColor(R.attr.myAttr, Color.RED)`.
