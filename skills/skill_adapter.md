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
- `getContentsCompare()` — mỗi trường cần theo dõi thay đổi sẽ là một cặp `giáTrị to "tag"`. Tag nên đặt trùng tên property cho dễ đọc.

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

        viewHolder.itemView.setOnClickListener {

            val item = (viewHolder.bindingAdapter as ListAdapter<*, *>).currentList.getOrNull(viewHolder.absoluteAdapterPosition) as? TestViewItem ?: return@setOnClickListener

            // todo gửi sự kiện
        }

        return viewHolder
    }

    override fun onBindViewHolder(binding: AdapterItemNoneBinding, viewType: Int, position: Int, item: TestViewItem, payloads: List<String>) {
        super.onBindViewHolder(binding, viewType, position, item, payloads)

        if (payloads.isEmpty() || payloads.contains("text")) {
            // todo set text
        }
    }
}
```

**Quy tắc quan trọng về `payloads`:**
- `payloads` là danh sách các tag từ `getContentsCompare()` của những trường **thực sự thay đổi**.
- Khi `payloads.isEmpty()` → bind lần đầu hoặc full rebind → cập nhật tất cả.
- Khi `payloads.contains("tag")` → chỉ trường đó thay đổi → cập nhật riêng trường đó.
- Pattern chuẩn: `if (payloads.isEmpty() || payloads.contains("tag")) { ... }`.

### Bước 3 — Hiển thị bằng MultiRecyclerView

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

**Giải thích flow Cách 1:**
1. `AutoRegisterManager.subscribe(ViewItemAdapterProvider::class.java)` → trả về `Flow<List<ViewItemAdapterProvider>>` chứa tất cả provider đã đăng ký.
2. `.map { it.flatMap { it.provider() } }` → biến thành `Flow<List<String>>` — danh sách tên class adapter.
3. `.collect { ... }` → mỗi khi có adapter mới đăng ký, tự động submit lại list.
4. `submitListAndAwait(viewItemList, adapterList, isAnimation)` → suspend cho đến khi DiffUtil xử lý xong và animation hoàn tất.

#### Cách 2 — Dùng với LiveData + `attachAdapter()`

Phù hợp khi dữ liệu đến từ `LiveData` hoặc `StateFlow` trong ViewModel. `attachAdapter()` tự động combine flow dữ liệu với danh sách adapter đã đăng ký.

```kotlin
suspend fun testWithLivedata(fragment: Fragment) {

    val viewItemListLiveData = MutableLiveData(arrayListOf(TestViewItem()))

    val recyclerView = MultiRecyclerView(fragment.requireContext())
    recyclerView.layoutManager = LinearLayoutManager(fragment.requireContext())

    viewItemListLiveData.asFlow().attachAdapter().collect { (viewItemList, adapterList) ->

        recyclerView.submitListAndAwait(viewItemList = viewItemList, adapterList = adapterList, isAnimation = true)
    }
}
```

**Giải thích flow Cách 2:**
1. `viewItemListLiveData.asFlow()` → chuyển `LiveData<List<ViewItem>>` thành `Flow<List<ViewItem>>`.
2. `.attachAdapter()` → combine với `adapterStateFlow` (danh sách adapter đã đăng ký), trả về `Flow<Pair<List<ViewItem>, List<String>>>`.
3. `.collect { (viewItemList, adapterList) -> ... }` → destructure và truyền vào `submitListAndAwait`.

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

Bên trong, `adapterStateFlow` là một `StateFlow<List<String>>` được khởi tạo eagerly:

```kotlin
val adapterStateFlow: StateFlow<List<String>> = AutoRegisterManager
    .subscribe(ViewItemAdapterProvider::class.java)
    .map { providers -> providers.flatMap { it.provider() } }
    .stateIn(adapterScope, SharingStarted.Eagerly, emptyList())
```

### `@Adapter` annotation

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Adapter
```

Chỉ dùng trên class cụ thể (không phải abstract/sealed). KSP sẽ sinh `{ModuleName}ViewItemAdapterProvider` tự động.

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

Khi sử dụng thư viện, các import thường cần:

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
import androidx.lifecycle.MutableLiveData                      // LiveData (dùng cho Cách 2)
import androidx.lifecycle.asFlow                               // Chuyển LiveData thành Flow
import kotlinx.coroutines.flow.map                            // Flow operator
```

## Checklist khi tạo mới một item type

1. [ ] Tạo `data class XxxViewItem(...) : ViewItem` — override `areItemsTheSame()` và `getContentsCompare()`.
2. [ ] Tạo layout XML cho item → sẽ sinh ra ViewBinding class.
3. [ ] Tạo `@Adapter class XxxAdapter : ViewItemAdapter<XxxViewItem, XxxBinding>()` — override `viewItemClass`, `createViewBinding(...)`.
4. [ ] (Tuỳ chọn) Override `createViewHolder(...)` để gắn click listener.
5. [ ] (Tuỳ chọn) Override `onBindViewHolder(...)` để bind dữ liệu, dùng `payloads` cho partial bind.
6. [ ] Build project để KSP sinh code đăng ký tự động.
7. [ ] Thêm `XxxViewItem` vào danh sách truyền cho `submitListAndAwait`.

## Lưu ý quan trọng

- **Không gọi `submitList(...)` trực tiếp** trên `MultiAdapter`. Luôn dùng `submitListAndAwait(...)` với `adapterList`.
- **`@Adapter` không dùng trên abstract/sealed class** — KSP sẽ báo lỗi compile.
- **Mỗi `ViewItem` class chỉ nên có đúng một `ViewItemAdapter`** tương ứng. Nếu có nhiều adapter cho cùng ViewItem class, `MultiAdapter` sẽ dùng adapter đăng ký sau cùng.
- **`viewItemClass` nên dùng `by lazy`** để tránh vấn đề khởi tạo sớm.
- **Luôn gọi `super.onBindViewHolder(...)`** ở đầu hàm `onBindViewHolder` nếu override.
- **`MultiRecyclerView` đã tự gắn `MultiAdapter`** trong `init` block — không cần set adapter thủ công.
- **Ưu tiên dùng `attachAdapter()`** (Cách 2) khi dữ liệu đến từ ViewModel — code ngắn gọn hơn và tự xử lý combine.
