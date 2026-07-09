package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.data.repository.FileRepositoryImpl
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Contract quản lý việc quét / xóa file không xác định (unrecognized) trên external storage.
 *
 * "File không xác định" là file có đuôi không thuộc danh sách cho phép (ảnh/nhạc/video),
 * hoặc file không có đuôi mở rộng.
 */
interface FileRepository {

    /**
     * Xóa tất cả file thuộc [category] trên external storage.
     *
     * @return Pair(số file đã xóa, tổng bytes đã giải phóng)
     */
    fun deleteStrangeFilesByCategory(category: StrangeFileCategory): Pair<Int, Long>

    /** Đếm tổng số file unrecognized trên toàn bộ external storage (blocking). */
    fun countStrangeFiles(): Int

    /**
     * Flow phát ra số lượng file unrecognized hiện tại.
     * Tự động cập nhật mỗi khi hệ thống file thay đổi.
     */
    fun countStrangeFilesFlow(): Flow<Int>

    /**
     * Phát tín hiệu broadcast để các flow đang lắng nghe đếm lại số file.
     * Gọi sau mỗi thao tác thêm / xóa file.
     */
    fun refreshFileStatus()

    /**
     * Cold flow phát ra mỗi [File] unrecognized mới xuất hiện trên storage.
     *
     * - Dùng `channelFlow` + `awaitClose` để tự động dừng [android.os.FileObserver] khi flow bị cancel.
     * - Đệ quy watch toàn bộ cây thư mục từ external storage root.
     * - Kết hợp tốt với `flatMapLatest` bên ngoài để bật/tắt theo preference.
     */
    fun watchFilesFlow(): Flow<File>

    companion object {

        val instance: FileRepository by lazy { FileRepositoryImpl }
    }
}
