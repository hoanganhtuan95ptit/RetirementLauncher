# Skill: Tạo màn hình mới theo pattern chuẩn (Home pattern)

## Mục đích

Chuẩn hóa cách tạo một màn hình mới trong project, bao gồm đầy đủ:
Fragment + ViewModel + ViewModelFactory + Item interface + Adapters + DeeplinkHandler.

Pattern này dựa trên màn **Home** (`presentation/home/`) — đây là màn hình phức tạp nhất và đầy đủ nhất trong project.

---

## Quy tắc chung — Bắt buộc áp dụng cho mọi file

### Không thụt đầu dòng quá 5 bậc

Code trong project **không được thụt đầu dòng quá 5 bậc** (tương đương 5 lần indent, mỗi bậc = 4 spaces hoặc 1 tab).

Khi logic lồng quá sâu, hãy tách ra hàm riêng hoặc dùng early return:

```kotlin
// ❌ Sai — thụt 6 bậc
class Foo {                                    // bậc 0
    fun bar() {                                // bậc 1
        items.forEach { item ->                // bậc 2
            if (item.isValid) {                // bậc 3
                item.children.forEach { child ->  // bậc 4
                    if (child.active) {            // bậc 5
                        process(child.data)        // bậc 6 ❌
                    }
                }
            }
        }
    }
}

// ✅ Đúng — tách hàm để giữ độ sâu ≤ 5
class Foo {                                    // bậc 0
    fun bar() {                                // bậc 1
        items.forEach { processItem(it) }      // bậc 2
    }

    private fun processItem(item: Item) {      // bậc 0 (hàm mới)
        if (!item.isValid) return              // bậc 1 — early return
        item.children.forEach { child ->       // bậc 1
            if (child.active) process(child.data)  // bậc 2
        }
    }
}
```

**Các kỹ thuật giảm indent:**
- **Early return** — kiểm tra điều kiện sớm, return ngay nếu không hợp lệ.
- **Tách hàm private** — khi một block lambda hoặc if-else phình to, tách thành hàm riêng.
- **`let`, `also`, `run`** — dùng scope function để tránh if-null lồng nhau.

---

## Kiến trúc tổng quan của một màn hình

```
presentation/
└── {screen}/
    ├── {Screen}Fragment.kt          ← UI: setup RecyclerView, observe data
    ├── {Screen}ViewModel.kt         ← State: combineState, expose items
    ├── {Screen}ViewModelFactory.kt  ← Factory: tạo ViewModel với dependencies
    └── adapter/
        ├── {Screen}Item.kt          ← Marker interface: spanSize, TOTAL_COLUMNS
        ├── FooAdapter.kt            ← data class FooScreenItem + @Adapter FooAdapter
        ├── BarAdapter.kt            ← data class BarScreenItem + @Adapter BarAdapter
        └── ...
```

> `DeeplinkHandler` nằm **cùng file với Fragment** (cuối file), không tách riêng.

---

## Bước 1 — Tạo `{Screen}Item` interface

File: `adapter/{Screen}Item.kt`

Mỗi màn hình định nghĩa một marker interface riêng kế thừa `ViewItem`. Interface này kiểm soát `spanSize` và `TOTAL_COLUMNS` của `GridLayoutManager`.

```kotlin
package com.simple.launcher.retirement.presentation.{screen}.adapter

import com.simple.adapter.ViewItem

interface {Screen}Item : ViewItem {

    val spanSize: Int get() = TOTAL_COLUMNS / 3  // default: 1/3 tổng cột

    companion object {
        const val TOTAL_COLUMNS = 6
    }
}
```

**Quy tắc `spanSize`:**

| Trường hợp | Giá trị |
|---|---|
| Full width | `TOTAL_COLUMNS` (= 6) |
| Half width | `TOTAL_COLUMNS / 2` (= 3) |
| 1/3 width | `TOTAL_COLUMNS / 3` (= 2) — default |
| Custom | Override trong từng `data class` |

---

## Bước 2 — Tạo từng Adapter (ViewItem + @Adapter cùng file)

File: `adapter/FooAdapter.kt`

### Template cơ bản (không có onclick)

```kotlin
package com.simple.launcher.retirement.presentation.{screen}.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.simple.adapter.Adapter
import com.simple.adapter.ViewItemAdapter
import com.simple.launcher.retirement.databinding.ItemFooBinding
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.text.BigText
import com.simple.launcher.retirement.utils.text.setText

data class FooScreenItem(
    val title: BigText,
    val background: Background
) : {Screen}Item {

    override val spanSize: Int = {Screen}Item.TOTAL_COLUMNS  // full width

    override fun areItemsTheSame(): List<Any> = listOf("Foo")  // dùng ID duy nhất

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        title to "title",
        background to "background"
    )
}

@Adapter
class FooAdapter : ViewItemAdapter<FooScreenItem, ItemFooBinding>() {

    override val viewItemClass: Class<FooScreenItem> by lazy {
        FooScreenItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemFooBinding {
        return ItemFooBinding.inflate(layoutInflater, parent, false)
    }

    override fun onBindViewHolder(binding: ItemFooBinding, viewType: Int, position: Int, item: FooScreenItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        if (payloads.isEmpty() || payloads.contains("title")) {
            binding.tvTitle.setText(item.title)
        }
        if (payloads.isEmpty() || payloads.contains("background")) {
            binding.root.setBackground(item.background)
        }
    }
}
```

### Template có onclick (cần entity + click)

```kotlin
data class BarScreenItem(
    val entity: BarEntity,      // chỉ dùng trong onClick

    val label: BigText,
    val icon: BigImage,
    val background: Background
) : {Screen}Item {

    override fun areItemsTheSame(): List<Any> = listOf(entity.id)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        label to "label",
        icon to "icon",
        background to "background"
    )
}

@Adapter
class BarAdapter : ViewItemAdapter<BarScreenItem, ItemBarBinding>() {

    override val viewItemClass: Class<BarScreenItem> by lazy {
        BarScreenItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ItemBarBinding {
        return ItemBarBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<ItemBarBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)
        viewHolder.itemView.setOnSafeWithPerformHapticFeedbackClickListener {
            val item = viewHolder.getItem<BarScreenItem>() ?: return@setOnSafeWithPerformHapticFeedbackClickListener
            // Gửi entity qua EventBus hoặc xử lý trực tiếp (deeplink, intent...)
            sendDeeplinkWithBackStack(DeepLinks.BAR)
        }
        return viewHolder
    }

    override fun onBindViewHolder(binding: ItemBarBinding, viewType: Int, position: Int, item: BarScreenItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)
        if (payloads.isEmpty() || payloads.contains("label")) {
            binding.tvLabel.setText(item.label)
        }
        if (payloads.isEmpty() || payloads.contains("icon")) {
            binding.ivIcon.setImage(item.icon)
        }
        if (payloads.isEmpty() || payloads.contains("background")) {
            binding.root.setBackground(item.background)
        }
    }
}
```

**Quy tắc bắt buộc cho Adapter:**
- ViewItem và Adapter **cùng một file `.kt`**, ViewItem khai báo trước.
- `@Adapter` chỉ đặt trên class **concrete** (không phải abstract/sealed).
- `onBindViewHolder`: mỗi field có tag riêng dùng `if (payloads.isEmpty() || payloads.contains("tag"))`.
- Adapter **không xử lý dữ liệu** — chỉ `setText`, `setImage`, `setBackground`.
- Luôn gọi `super.onBindViewHolder(...)` ở đầu.

---

## Bước 3 — Tạo `{Screen}ViewModel`

File: `{Screen}ViewModel.kt`

```kotlin
package com.simple.launcher.retirement.presentation.{screen}

import com.simple.adapter.ViewItem
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.combineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class {Screen}ViewModel : BaseViewModel() {

    // --- Các StateFlow con (theo từng nhóm dữ liệu) ---

    val fooViewItems: StateFlow<Pair<Double, List<ViewItem>>> = combineState(
        flow1 = strings,
        flow2 = themes,
        initialValue = 1.0 to emptyList()
    ) { strings, themeMap ->

        1.0 to listOf(
            FooScreenItem(
                title = strings.getString(R.string.foo_title)
                    .withStyleTitleLarge()
                    .with(ForegroundColor(themeMap.getColor(android.R.attr.textColorPrimary)))
                    .build(),
                background = Background.Builder()
                    .backgroundColor(themeMap.getColor(R.attr.colorSurface))
                    .cornerRadius(DP.DP_16)
                    .build()
            )
        )
    }

    // --- viewItemMap: quản lý các item động theo order (Double) ---
    // Key là "order" (thứ tự hiển thị), value là danh sách ViewItem tương ứng.
    // Giá trị khởi tạo chứa các item mặc định (nếu có).
    val viewItemMap = MutableStateFlow<Map<Double, List<ViewItem>>>(emptyMap())

    // --- items: combine tất cả lại theo thứ tự ---
    val items: StateFlow<List<ViewItem>> = combineState(
        flow1 = fooViewItems,
        flow2 = viewItemMap,
        initialValue = emptyList()
    ) { foo, extraMap ->

        (listOf(foo) + extraMap.toList())
            .sortedBy { it.first }
            .flatMap { it.second }
    }

    // --- updateItem: dùng khi Fragment hoặc Adapter cần inject item theo order ---
    fun updateItem(order: Double, list: List<ViewItem>) {
        viewItemMap.value = viewItemMap.value.toMutableMap().apply {
            put(order, list)
        }
    }
}
```

**Quy tắc bắt buộc cho ViewModel:**
- Kế thừa `BaseViewModel` — tận dụng `strings`, `themes`, `background`.
- Dùng `combineState` để kết hợp flows (xem `skill_view_model.md`).
- Expose `StateFlow<List<ViewItem>>` ra ngoài, **không** expose entity trực tiếp.
- Mỗi nhóm dữ liệu tạo một `StateFlow<Pair<Double, List<ViewItem>>>` với key `Double` là thứ tự.
- Nếu màn hình đơn giản (không cần `viewItemMap`), bỏ đi phần đó.
- Toàn bộ logic biến đổi (`.toBig()`, `BigImage()`, color, background…) nằm trong ViewModel.

---

## Bước 4 — Tạo `{Screen}ViewModelFactory`

File: `{Screen}ViewModelFactory.kt`

```kotlin
package com.simple.launcher.retirement.presentation.{screen}

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class {Screen}ViewModelFactory(
    // Thêm dependencies nếu cần (UseCase, Repository...)
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom({Screen}ViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return {Screen}ViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

> Nếu ViewModel không có dependency nào, có thể bỏ Factory và dùng `by viewModels()` trực tiếp.

---

## Bước 5 — Tạo `{Screen}Fragment` + `DeeplinkHandler`

File: `{Screen}Fragment.kt`

```kotlin
package com.simple.launcher.retirement.presentation.{screen}

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.simple.adapter.MultiAdapter
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.Fragment{Screen}Binding
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.presentation.{screen}.adapter.{Screen}Item
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.lifecycle.observe
import com.simple.launcher.retirement.utils.size.DP

class {Screen}Fragment : BaseFragment<Fragment{Screen}Binding>() {

    private val viewModel: {Screen}ViewModel by viewModels {
        {Screen}ViewModelFactory(/* dependencies */)
    }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): Fragment{Screen}Binding {
        return Fragment{Screen}Binding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {
        super.setupViews(view, savedInstanceState)

        // Setup GridLayoutManager với spanSizeLookup
        val layoutManager = GridLayoutManager(requireContext(), {Screen}Item.TOTAL_COLUMNS)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return (binding.rvContent.adapter as? MultiAdapter)
                    ?.currentList
                    ?.getOrNull(position) as? {Screen}Item)?.spanSize
                    ?: {Screen}Item.TOTAL_COLUMNS
            }
        }
        binding.rvContent.layoutManager = layoutManager
    }

    override fun observeData() {
        super.observeData()

        // Background toàn màn hình từ theme
        viewModel.background.observe(this) { background ->
            binding.root.setBackground(background)
        }

        // Danh sách item chính
        viewModel.items.attachAdapter().observe(this) { (items, adapters) ->
            binding.rvContent.submitListAndAwait(items, adapters, true)
        }
    }
}

// ─── DeeplinkHandler — cùng file với Fragment ────────────────────────────────

@Deeplink
class {Screen}DeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.{SCREEN}

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {
        val transaction = fragmentActivity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, {Screen}Fragment())

        if (extras?.get(DeepLinks.Extras.ADD_TO_BACK_STACK) == true) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
        return true
    }
}
```

**Quy tắc bắt buộc cho Fragment:**
- Kế thừa `BaseFragment<VB>` — implement `inflateBinding(...)`.
- Dùng `setupViews(...)` cho setup layout/click, `observeData()` cho observe Flow/LiveData.
- **Luôn observe `viewModel.background`** và gọi `binding.root.setBackground(background)`.
- Setup `GridLayoutManager` với `spanSizeLookup` đọc `spanSize` từ `{Screen}Item`.
- Dùng `viewModel.items.attachAdapter().observe(this) { (items, adapters) -> binding.rv.submitListAndAwait(...) }`.
- `DeeplinkHandler` đặt **cuối file Fragment**, annotate `@Deeplink`.
- Deeplink handler kiểm tra `extras?.get(ADD_TO_BACK_STACK)` để quyết định `addToBackStack`.

---

## Bước 6 — Đăng ký Deeplink constant

File: `presentation/DeepLinks.kt`

Thêm constant vào `object DeepLinks`:

```kotlin
const val {SCREEN} = "app://{screen}"
```

---

## Checklist đầy đủ khi tạo màn hình mới

### Cấu trúc file
- [ ] `adapter/{Screen}Item.kt` — marker interface với `TOTAL_COLUMNS` và default `spanSize`
- [ ] `adapter/FooAdapter.kt` — `data class FooScreenItem` + `@Adapter class FooAdapter` (cùng file)
- [ ] `{Screen}ViewModel.kt` — kế thừa `BaseViewModel`, dùng `combineState`
- [ ] `{Screen}ViewModelFactory.kt` — `ViewModelProvider.Factory` (bỏ nếu không có dependency)
- [ ] `{Screen}Fragment.kt` — kế thừa `BaseFragment<VB>`, setup RecyclerView, observe `background` + `items`
- [ ] `DeeplinkHandler` — cuối file Fragment, annotate `@Deeplink`
- [ ] `DeepLinks.kt` — thêm constant `const val {SCREEN} = "app://{screen}"`

### ViewModel
- [ ] Kế thừa `BaseViewModel`
- [ ] Dùng `combineState` (không dùng `combine().stateIn()` thủ công khi không cần)
- [ ] Expose `StateFlow<List<ViewItem>>` tên `items`
- [ ] Mỗi nhóm dữ liệu là `StateFlow<Pair<Double, List<ViewItem>>>` với Double là order
- [ ] Có `viewItemMap` + `updateItem(order, list)` nếu màn hình có item động
- [ ] Toàn bộ transform (BigText, BigImage, Background) nằm trong ViewModel

### Adapter
- [ ] ViewItem và Adapter cùng file
- [ ] `areItemsTheSame()` dùng ID duy nhất
- [ ] `getContentsCompare()` có entry cho mọi field hiển thị (BigText so sánh full object)
- [ ] `onBindViewHolder`: từng field có `if (payloads.isEmpty() || payloads.contains("tag"))`
- [ ] Gọi `super.onBindViewHolder(...)` đầu tiên
- [ ] Nếu có click: dùng `setOnSafeWithPerformHapticFeedbackClickListener` + `getItem<T>()`
- [ ] Adapter chỉ `setText` / `setImage` / `setBackground` — không tự xử lý dữ liệu

### Fragment
- [ ] `inflateBinding(...)` implement đúng
- [ ] `setupViews(...)`: setup GridLayoutManager + spanSizeLookup
- [ ] `observeData()`: observe `background`, observe `items.attachAdapter()`
- [ ] Không override `onCreateView`/`onViewCreated` trừ khi thực sự cần

### Deeplink
- [ ] `@Deeplink` annotation trên DeeplinkHandler
- [ ] `override val deeplink: String` với đúng constant từ `DeepLinks`
- [ ] Kiểm tra `ADD_TO_BACK_STACK` trong extras trước khi `addToBackStack(null)`

---

## Ví dụ: cấu trúc màn Home (tham chiếu thực tế)

```
presentation/home/
├── HomeFragment.kt
│   └── HomeDeeplinkHandler  ← @Deeplink, cuối file
├── HomeViewModel.kt
│   ├── cleanFilesViewItemList  (order = 1.0)
│   ├── cleanMemoryViewItemList (order = 2.0)
│   ├── appsAndContactsViewItemList (order = 3.0)
│   ├── viewItemMap (order = 0.0 → ClockHomeItem mặc định)
│   └── items = combine(tất cả) → sort by order → flatMap
├── HomeViewModelFactory.kt
└── adapter/
    ├── HomeItem.kt            ← TOTAL_COLUMNS = 6
    ├── ClockAdapter.kt        ← object ClockHomeItem (spanSize = full)
    ├── HeaderAdapter.kt       ← HeaderHomeItem (spanSize = full)
    ├── AppAdapter.kt          ← AppHomeItem (spanSize = TOTAL_COLUMNS/3 = 2)
    ├── ContactAdapter.kt      ← ContactHomeItem (spanSize = TOTAL_COLUMNS/2 = 3)
    ├── CleanFilesAdapter.kt   ← CleanFilesHomeItem (spanSize = TOTAL_COLUMNS/2 = 3)
    ├── CleanMemoryAdapter.kt  ← CleanMemoryHomeItem (spanSize = TOTAL_COLUMNS/2 = 3)
    └── UtilityAdapter.kt      ← abstract base cho CleanFiles và CleanMemory
```

## Import cần thiết (Fragment)

```kotlin
import com.simple.adapter.MultiAdapter
import com.simple.adapter.utils.attachAdapter
import com.simple.adapter.utils.submitListAndAwait
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.background.setBackground
import com.simple.launcher.retirement.utils.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
```

## Import cần thiết (ViewModel)

```kotlin
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.combineState
import com.simple.launcher.retirement.utils.background.Background
import com.simple.launcher.retirement.utils.size.DP
import com.simple.launcher.retirement.utils.string.getString
import com.simple.launcher.retirement.utils.text.*
import com.simple.launcher.retirement.utils.theme.getColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
```
