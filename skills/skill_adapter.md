# Skill: Sử dụng thư viện Adapter

## Mục đích

Thư viện **Adapter** (`com.github.hoanganhtuan95ptit.Adapter`) giúp quản lý nhiều loại item trong `RecyclerView` mà không cần viết boilerplate. Sử dụng **KSP** để tự động đăng ký adapter lúc compile.

## Khi nào dùng

- Khi cần hiển thị danh sách có **nhiều loại item khác nhau** trong một `RecyclerView`.
- Khi muốn mỗi module tự đăng ký adapter mà **không cần khai báo tập trung**.
- Khi cần **partial bind** (chỉ cập nhật phần thay đổi thay vì vẽ lại toàn bộ item).

## Cài đặt

### settings.gradle

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### build.gradle (module)

```groovy
plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    implementation 'com.github.hoanganhtuan95ptit.Adapter:adapter:x.y.z'
    ksp         'com.github.hoanganhtuan95ptit:Adapter:adapter-processor:x.y.z'
}
```

## Kiến trúc tổng quan

```
@Adapter annotation trên class adapter
       ↓
KSP sinh ra {TênModule}ViewItemAdapterProvider lúc compile
       ↓
AutoRegisterManager tự phát hiện provider khi app khởi động
       ↓
MultiAdapter nạp các adapter class theo tên thông qua reflection
       ↓
MultiRecyclerView hiển thị đúng adapter cho từng ViewItem
```

### Các class cốt lõi

| Class | Package | Vai trò |
|---|---|---|
| `ViewItem` | `com.simple.adapter` | Interface cho model dữ liệu, hỗ trợ DiffUtil |
| `ViewItemAdapter<VI, VB>` | `com.simple.adapter` | Base class cho adapter, kế thừa khi tạo adapter mới |
| `@Adapter` | `com.simple.adapter` | Annotation đánh dấu adapter để KSP tự đăng ký |
| `MultiRecyclerView` | `com.simple.adapter` | RecyclerView tự gắn `MultiAdapter` sẵn |
| `MultiAdapter` | `com.simple.adapter` | ListAdapter quản lý nhiều loại ViewItem |
| `ViewItemAdapterProvider` | `com.simple.adapter` | Abstract class cung cấp danh sách tên adapter class |
| `submitListAndAwait` | `com.simple.adapter.utils` | Extension function suspend, submit list và chờ DiffUtil + animation xong |
| `attachAdapter` | `com.simple.adapter.utils` | Extension function trên Flow, tự combine với danh sách adapter đã đăng ký |

## Convention kiến trúc trong project này

### Quy tắc 1 — ViewItem cùng file với Adapter

ViewItem và Adapter tương ứng phải nằm **trong cùng một file `.kt`**. ViewItem khai báo trước, Adapter ngay sau.

```kotlin
// AppAdapter.kt
data class AppHomeItem(...) : HomeItem { ... }

@Adapter
class AppAdapter : ViewItemAdapter<AppHomeItem, ItemAppBinding>() { ... }
```

### Quy tắc 2 — ViewItem chỉ chứa dữ liệu cơ bản

ViewItem **chỉ được chứa**: `RichText`, `RichImage`, `Boolean`, `Int`, `String`, v.v. — dữ liệu đã sẵn sàng để set vào view.

**Entity (domain object) chỉ được giữ trong ViewItem nếu cần cho onclick**, không được dùng để adapter tự extract dữ liệu từ đó.

```kotlin
// ✅ Đúng
data class AppHomeItem(
    val label: RichText,   // đã xử lý sẵn, adapter chỉ setText
    val icon: RichImage,   // đã xử lý sẵn, adapter chỉ setImage
    val entity: AppEntity  // chỉ dùng trong onClick để mở app
) : HomeItem

// ❌ Sai — adapter phải tự extract và xử lý
data class AppHomeItem(val entity: AppEntity) : HomeItem
```

### Quy tắc 3 — Adapter không xử lý dữ liệu

Adapter **chỉ được gọi** `setText(item.xxx)`, `setImage(item.xxx)`, `item.xxx.isChecked` — không được có logic transform, toRich(), ImageDrawable(), điều kiện if/else trên dữ liệu.

Toàn bộ xử lý (`.toRich()`, `ImageDrawable()`, `ImagePath()`, conditional image selection...) phải nằm trong **ViewModel**.

```kotlin
// ✅ Đúng — ViewModel xử lý
// HomeViewModel.kt
AppHomeItem(
    label = entity.label.toRich(),
    icon  = ImageDrawable(entity.icon),
    entity = entity
)

// ✅ Đúng — Adapter chỉ set
override fun onBindViewHolder(...) {
    binding.tvLabel.setText(item.label)
    binding.ivIcon.setImage(item.icon)
}

// ❌ Sai — Adapter tự xử lý
override fun onBindViewHolder(...) {
    binding.tvLabel.setText(item.entity.label.toRich())
    binding.ivIcon.setImage(ImageDrawable(item.entity.icon))
}
```

### Quy tắc 4 — Adapter gửi entity qua EventBus khi click, ViewModel xử lý toggle/logic

Adapter **chỉ gửi entity gốc** (không được tự toggle hay tạo state mới). ViewModel nhận entity và xử lý logic.

```kotlin
// ✅ Đúng — Adapter chỉ gửi entity
viewHolder.itemView.setOnClickListener {
    val item = ... as? SelectableAppItem ?: return@setOnClickListener
    AppListEventBus.post(item.entity)  // gửi entity gốc
}

// ✅ Đúng — ViewModel xử lý toggle
fun updateItem(entity: SelectableAppEntity) {
    val index = currentList.indexOfFirst { it.app.packageName == entity.app.packageName }
    currentList[index] = currentList[index].copy(isSelected = !currentList[index].isSelected)
    _apps.value = currentList
}

// ❌ Sai — Adapter tự toggle
AppListEventBus.post(item.copy(isSelected = !item.isSelected))
```

### Quy tắc 5 — ViewModel giữ domain entities nội bộ, expose ViewItems ra ngoài

Dùng `MutableStateFlow` (không phải `MutableLiveData`) cho state nội bộ, expose bằng `StateFlow`.

```kotlin
class AppListViewModel : BaseViewModel() {

    // Nội bộ: domain entities (để saveSelection, updateItem dễ xử lý)
    private val _apps = MutableStateFlow<List<SelectableAppEntity>>(emptyList())
    private val _query = MutableStateFlow("")

    // Expose: ViewItems đã map sẵn — Fragment/Adapter chỉ nhận ViewItems
    val items: StateFlow<List<SelectableAppItem>> = combine(_apps, _query) { apps, query ->
        apps.filter { query.isBlank() || it.app.label.contains(query, ignoreCase = true) }
            .map { entity ->
                SelectableAppItem(
                    label      = entity.app.label.toRich(),
                    icon       = ImageDrawable(entity.app.icon),
                    isSelected = entity.isSelected,
                    entity     = entity
                )
            }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
```

## Hướng dẫn từng bước

### Bước 1 — Tạo ViewItem

Mỗi loại item trong danh sách cần một `data class` implement `ViewItem`.

**Bắt buộc override:**
- `areItemsTheSame()`: Trả về danh sách các trường định danh duy nhất (giống primary key). DiffUtil dùng để biết hai item có phải cùng một entity không.
- `getContentsCompare()`: Trả về danh sách `Pair<Any, String>` — giá trị cần theo dõi kèm tag. Tag sẽ xuất hiện trong `payloads` khi bind lại, giúp chỉ cập nhật phần thay đổi.

```kotlin
data class TestViewItem(
    val id: String = "",
    val text: String = ""
) : com.simple.adapter.ViewItem {

    override fun areItemsTheSame(): List<Any> = listOf(id)

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        text to "text"
    )
}
```

**Quy tắc:**
- `areItemsTheSame()` — nên chứa ID hoặc tổ hợp trường duy nhất.
- `getContentsCompare()` — **mỗi field hiển thị** đều phải có entry. Tag đặt trùng tên property. Nếu field là `RichText`, so sánh toàn bộ object (`title to "title"`), không chỉ `.text` — để detect cả thay đổi span (ForegroundColor, Bold...).

### Bước 2 — Tạo ViewItemAdapter

Tạo class kế thừa `ViewItemAdapter<VI, VB>` và đánh dấu `@Adapter`. KSP sẽ tự sinh code đăng ký.

**Bắt buộc override:**
- `viewItemClass`: Khai báo class của ViewItem mà adapter này xử lý.
- `createViewBinding(...)`: Inflate ViewBinding.

**Tuỳ chọn override:**
- `createViewHolder(...)`: Gắn click listener hoặc setup ban đầu cho ViewHolder.
- `onBindViewHolder(...)`: Bind dữ liệu vào view. Dùng `payloads` để partial bind.

```kotlin
@Adapter
class TestAdapter : com.simple.adapter.ViewItemAdapter<TestViewItem, AdapterItemNoneBinding>() {

    override val viewItemClass: Class<TestViewItem> by lazy {
        TestViewItem::class.java
    }

    override fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): AdapterItemNoneBinding {
        return AdapterItemNoneBinding.inflate(layoutInflater, parent, false)
    }

    override fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<AdapterItemNoneBinding> {
        val viewHolder = super.createViewHolder(parent, viewType)

        viewHolder.itemView.setOnSafeClickListener {
            // Dùng extension getItem<T>() từ com.simple.launcher.retirement.utils.getItem
            val item = viewHolder.getItem<TestViewItem>() ?: return@setOnSafeClickListener
            TestEventBus.post(item.entity)  // gửi entity, không tự xử lý
        }

        return viewHolder
    }

    override fun onBindViewHolder(binding: AdapterItemNoneBinding, viewType: Int, position: Int, item: TestViewItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)

        if (payloads.isEmpty() || payloads.contains("text")) {
            binding.tvText.setText(item.text)
        }
    }
}
```

**Quy tắc quan trọng về `payloads`:**
- `payloads` là danh sách các tag từ `getContentsCompare()` của những trường **thực sự thay đổi**.
- Khi `payloads.isEmpty()` → bind lần đầu hoặc full rebind → cập nhật tất cả.
- Khi `payloads.contains("tag")` → chỉ trường đó thay đổi → cập nhật riêng trường đó.
- **Pattern chuẩn cho mọi field có tag**: `if (payloads.isEmpty() || payloads.contains("tag")) { ... }`.
- **Field không có trong `getContentsCompare()`** (không bao giờ thay đổi): chỉ set khi `payloads.isEmpty()`.
- **Không được gộp nhiều field vào cùng một `if (payloads.isEmpty())`** nếu chúng có tag riêng — sẽ miss update khi chỉ một trong số đó thay đổi.

```kotlin
// ✅ Đúng — mỗi field một guard riêng theo tag của nó
if (payloads.isEmpty() || payloads.contains("label")) { binding.tvLabel.setText(item.label) }
if (payloads.isEmpty() || payloads.contains("icon"))  { binding.ivIcon.setImage(item.icon) }
if (payloads.isEmpty() || payloads.contains("isSelected")) { binding.cbSelected.isChecked = item.isSelected }

// ❌ Sai — label và icon có tag nhưng bị gộp vào payloads.isEmpty() → miss update
if (payloads.isEmpty()) {
    binding.tvLabel.setText(item.label)
    binding.ivIcon.setImage(item.icon)
}
```

### Bước 3 — Tạo EventBus

Mỗi màn hình có một EventBus riêng. Dùng base class `EventBus<T>` trong `utils/EventBus.kt`.

```kotlin
// AppListEvent.kt (cùng package với adapter/fragment)
object AppListEventBus : EventBus<SelectableAppEntity>()
```

**Quy tắc:**
- EventBus emit **entity** (domain object), không emit ViewItem.
- Adapter gọi `XxxEventBus.post(item.entity)` trong onClick.
- Fragment/ViewModel nhận event qua `XxxEventBus.events.collectLatest { entity -> viewModel.updateItem(entity) }`.
- ViewModel xử lý toàn bộ logic (toggle, update list...).

```kotlin
// Trong Fragment
viewLifecycleOwner.lifecycleScope.launch {
    AppListEventBus.events.collectLatest { entity ->
        viewModel.updateItem(entity)
    }
}
```

### Bước 4 — Hiển thị bằng MultiRecyclerView

Có 2 cách submit list vào `MultiRecyclerView`:

#### Cách 1 — Dùng trực tiếp với AutoRegisterManager

Tự subscribe `AutoRegisterManager` để lấy danh sách adapter class names, rồi truyền vào `submitListAndAwait`.

```kotlin
suspend fun test(fragment: Fragment) {

    val testList = arrayListOf(
        TestViewItem()
    )

    val recyclerView = MultiRecyclerView(fragment.requireContext())
    recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())

    AutoRegisterManager.subscribe(ViewItemAdapterProvider::class.java).map { it.flatMap { it.provider() } }.collect {

        recyclerView.submitListAndAwait(viewItemList = testList, adapterList = it, isAnimation = true)
    }
}
```

#### Cách 2 — Dùng với LiveData + `attachAdapter()` (khuyến khích)

Phù hợp khi dữ liệu đến từ `LiveData` hoặc `StateFlow` trong ViewModel.

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.items.asFlow().attachAdapter().collectLatest { (items, adapters) ->
        binding.recyclerView.submitListAndAwait(items, adapters, true)
    }
}
```

**Khi nào dùng cách nào:**
- **Cách 1**: Khi danh sách item cố định hoặc muốn kiểm soát thủ công.
- **Cách 2**: Khi dữ liệu reactive từ ViewModel (`LiveData` / `StateFlow`). Đây là cách được khuyến khích.

## API Reference

### `ViewItem` interface

```kotlin
interface ViewItem {
    fun areItemsTheSame(): List<Any>
    fun getContentsCompare(): List<Pair<Any, String>> = listOf()
}
```

### `ViewItemAdapter<VI, VB>` abstract class

```kotlin
abstract class ViewItemAdapter<VI : ViewItem, VB : ViewBinding>() : ViewItemAdapterDelegate {
    abstract val viewItemClass: Class<VI>
    abstract fun createViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): VB
    open fun createViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<VB>
    open fun onBindViewHolder(binding: VB, viewType: Int, position: Int, item: VI, payloads: List<String>)
}
```

### `EventBus<T>` base class (`utils/EventBus.kt`)

```kotlin
open class EventBus<T> {
    private val _events = MutableSharedFlow<T>(extraBufferCapacity = 1)
    val events: SharedFlow<T> = _events.asSharedFlow()
    fun post(item: T) { _events.tryEmit(item) }
}
```

Khai báo EventBus cho một màn hình:

```kotlin
object AppListEventBus : EventBus<SelectableAppEntity>()
```

### `submitListAndAwait` extension function

```kotlin
suspend fun RecyclerView.submitListAndAwait(
    viewItemList: List<ViewItem>,
    adapterList: List<String>,
    isAnimation: Boolean = false,
    ignoreTransitionViewId: List<Int> = emptyList()
)
```

| Tham số | Mô tả |
|---|---|
| `viewItemList` | Danh sách `ViewItem` cần hiển thị |
| `adapterList` | Danh sách tên class adapter (lấy từ `ViewItemAdapterProvider`) |
| `isAnimation` | `true` = dùng `AutoTransition` cho animation mượt mà |
| `ignoreTransitionViewId` | Danh sách view ID loại trừ khỏi animation |

### `attachAdapter` extension function

```kotlin
fun Flow<List<ViewItem>>.attachAdapter(): Flow<Pair<List<ViewItem>, List<String>>>
```

Combine flow dữ liệu `List<ViewItem>` với `adapterStateFlow` (danh sách tên class adapter đã đăng ký qua AutoRegister). Trả về `Flow<Pair<List<ViewItem>, List<String>>>` — destructure thành `(viewItemList, adapterList)` để truyền vào `submitListAndAwait`.

## Code được sinh tự động bởi KSP

Khi build, KSP quét tất cả class có `@Adapter` trong module và sinh ra:

```kotlin
// Generated by AdapterProcessor. DO NOT EDIT.
@AutoRegister(apis = [ViewItemAdapterProvider::class])
public class AdapterViewItemAdapterProvider : ViewItemAdapterProvider() {
    override fun provider(): List<String> = listOf(
        NoneAdapter::class.java.name,
    )
}
```

Tên class sinh ra theo quy tắc: tên Gradle module chuyển sang PascalCase + `ViewItemAdapterProvider`.
Ví dụ: module `feature-payment` → `FeaturePaymentViewItemAdapterProvider`.

## Import cần thiết

```kotlin
import com.simple.adapter.Adapter                              // @Adapter annotation
import com.simple.adapter.ViewItem                             // ViewItem interface
import com.simple.adapter.ViewItemAdapter                      // Base adapter class
import com.simple.adapter.ViewItemAdapterProvider              // Provider cho AutoRegister
import com.simple.adapter.MultiRecyclerView                    // RecyclerView có sẵn MultiAdapter
import com.simple.adapter.base.BaseBindingViewHolder           // ViewHolder base class
import com.simple.adapter.utils.submitListAndAwait             // Extension function submit list
import com.simple.adapter.utils.attachAdapter                  // Extension function combine với adapter
import com.simple.auto.register.AutoRegisterManager            // Subscribe danh sách adapter
import com.simple.launcher.retirement.utils.getItem            // Extension: viewHolder.getItem<T>()
import com.simple.launcher.retirement.utils.lifecycle.observe  // Flow<T>.observe(Fragment)
import com.simple.launcher.retirement.utils.view.setOnSafeClickListener // Click listener an toàn
import kotlinx.coroutines.flow.MutableStateFlow                // State nội bộ ViewModel
import kotlinx.coroutines.flow.StateFlow                       // Expose state ra Fragment
import kotlinx.coroutines.flow.combine                         // Kết hợp flows
import kotlinx.coroutines.flow.stateIn                         // Chuyển Flow thành StateFlow
import com.simple.launcher.retirement.utils.EventBus           // Base EventBus class
```

## Checklist khi tạo mới một item type

1. [ ] Tạo `data class XxxViewItem(...) : ViewItem` — chỉ chứa `RichText`/`RichImage`/primitive. Override `areItemsTheSame()` và `getContentsCompare()` (mỗi field hiển thị một entry, `RichText` so sánh full object không chỉ `.text`).
2. [ ] Nếu cần onclick: giữ thêm `val entity: XxxEntity` trong ViewItem.
3. [ ] Tạo `object XxxEventBus : EventBus<XxxEntity>()` nếu có onclick.
4. [ ] Tạo layout XML cho item → sẽ sinh ra ViewBinding class.
5. [ ] Tạo `@Adapter class XxxAdapter : ViewItemAdapter<XxxViewItem, XxxBinding>()` **trong cùng file với ViewItem** — override `viewItemClass`, `createViewBinding(...)`.
6. [ ] Override `createViewHolder(...)` để gắn click: `XxxEventBus.post(item.entity)`.
7. [ ] Override `onBindViewHolder(...)`: mỗi field dùng `if (payloads.isEmpty() || payloads.contains("tag"))`.
8. [ ] Trong ViewModel: giữ `MutableStateFlow<List<XxxEntity>>` nội bộ, expose `StateFlow<List<XxxViewItem>>` (combine + map có xử lý data).
9. [ ] Build project để KSP sinh code đăng ký tự động.
10. [ ] Thêm `XxxViewItem` vào danh sách truyền cho `submitListAndAwait`.

## Lưu ý quan trọng

- **Không gọi `submitList(...)` trực tiếp** trên `MultiAdapter`. Luôn dùng `submitListAndAwait(...)` với `adapterList`.
- **`@Adapter` không dùng trên abstract/sealed class** — KSP sẽ báo lỗi compile.
- **Mỗi `ViewItem` class chỉ nên có đúng một `ViewItemAdapter`** tương ứng. Nếu có nhiều adapter cho cùng ViewItem class, `MultiAdapter` sẽ dùng adapter đăng ký sau cùng.
- **`viewItemClass` nên dùng `by lazy`** để tránh vấn đề khởi tạo sớm.
- **Luôn gọi `super.onBindViewHolder(...)`** ở đầu hàm `onBindViewHolder` nếu override.
- **`MultiRecyclerView` đã tự gắn `MultiAdapter`** trong `init` block — không cần set adapter thủ công.
- **Ưu tiên dùng `attachAdapter()`** (Cách 2) khi dữ liệu đến từ ViewModel — code ngắn gọn hơn và tự xử lý combine.
- **Entity không implement `ViewItem`** — domain entity và ViewItem là hai tầng riêng biệt.
