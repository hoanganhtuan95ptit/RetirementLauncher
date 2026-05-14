# Skill: Sử dụng StringResStore để quản lý và truy cập String Resources linh hoạt

## Mục đích

`StringResStore` là một công cụ giúp quản lý và truy cập toàn bộ `String Resources` (`R.string`) của ứng dụng một cách linh hoạt. Nó cho phép lấy chuỗi văn bản bằng cả **Resource ID** (truyền thống) và **Resource Name** (String key), đồng thời hỗ trợ quan sát sự thay đổi thông qua Kotlin Flow. Điều này cực kỳ hữu ích cho việc quốc tế hóa (Localization) động hoặc khi cần mapping dữ liệu từ API với string trong app.

## Kiến trúc tổng quan

Hệ thống sử dụng Reflection để quét lớp `R.string` khi ứng dụng khởi chạy và lưu trữ chúng vào các Map để truy xuất nhanh chóng.

### Các thành phần chính

| Thành phần | Mô tả |
|---|---|
| `StringResStore.load(context)` | Quét và tải toàn bộ string resources vào bộ nhớ. Cần được gọi ở Application class hoặc khi khởi tạo app. |
| `Map<String, String>.getString(resId)` | **(Mới)** Lấy string từ map dựa trên ID, dùng trong `combine` của ViewModel để đảm bảo tính reactive. |
| `Int.asStringRes()` | Hàm mở rộng lấy giá trị string từ Resource ID dựa trên trạng thái mới nhất của Store. |
| `String.asStringRes()` | Hàm mở rộng lấy giá trị string từ tên resource dựa trên trạng thái mới nhất của Store. |
| `String.asFormattedStringRes()` | Lấy string theo tên và format với các tham số (e.g., `"welcome_msg".asFormattedStringRes(name)`). |
| `String.observeStringRes()` | Trả về một `Flow<String>` quan sát giá trị của một string key cụ thể. |

## Hướng dẫn sử dụng

### 1. Khởi tạo (Initialization)

Trước khi sử dụng, bạn cần tải tài nguyên vào Store. Thông thường việc này được thực hiện một lần.

```kotlin
// Trong Application class hoặc Activity khởi đầu
StringResStore.load(context)
```

### 2. Truy cập String theo Resource ID

Thay vì dùng `context.getString(id)`, bạn có thể dùng hàm mở rộng ngắn gọn:

```kotlin
val appName = R.string.app_name.asStringRes()
```

### 3. Truy cập String theo Tên (Dynamic Key)

Hữu ích khi bạn nhận được key từ API hoặc database và muốn hiển thị string tương ứng:

```kotlin
val dynamicTitle = "label_settings".asStringRes() // Trả về nội dung của R.string.label_settings
```

### 4. Sử dụng String Format

Hỗ trợ điền tham số vào chuỗi định dạng (ví dụ: `welcome_msg` là "Chào %s"):

```kotlin
val welcome = "welcome_msg".asFormattedStringRes("Hoàng Anh Tuấn")
// Kết quả: "Chào Hoàng Anh Tuấn"
```

### 5. Quan sát sự thay đổi (Reactive UI)

Sử dụng Flow để UI tự động cập nhật nếu `StringResStore` được load lại (ví dụ khi đổi ngôn ngữ thủ công). Cách tốt nhất là dùng `combine` trong ViewModel:

```kotlin
val items = combine(strings, themes) { stringMap, themeMap ->
    val title = stringMap.getString(R.string.title_settings)
    // Khởi tạo list items dựa trên stringMap hiện tại
}.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
```

## Tại sao nên dùng StringResStore?

1.  **Truy cập linh hoạt**: Không cần `Context` ở mọi nơi để lấy string, giúp decouple logic khỏi Android Framework.
2.  **Mapping động**: Dễ dàng map các chuỗi từ Server trả về với Resource nội bộ mà không cần dùng `switch-case` hay `when` khổng lồ.
3.  **Hỗ trợ Flow**: Tích hợp mượt mà với lập trình phản ứng (Reactive Programming) trong ViewModel.
4.  **Hiệu suất**: Mặc dù dùng Reflection để load, nhưng việc truy xuất sau đó thông qua `HashMap` cực kỳ nhanh.

## Ví dụ thực tế

Trong một `ViewModel` cần hiển thị thông báo lỗi dựa trên mã lỗi trả về từ server:

```kotlin
fun getErrorMessage(errorCode: String): String {
    val resKey = "error_$errorCode"
    return resKey.asStringRes().ifEmpty { R.string.default_error.asStringRes() }
}
```
