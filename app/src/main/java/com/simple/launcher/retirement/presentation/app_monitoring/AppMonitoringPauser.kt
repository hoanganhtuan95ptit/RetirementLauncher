package com.simple.launcher.retirement.presentation.app_monitoring

import java.util.concurrent.ConcurrentHashMap

/**
 * Cho phép tạm dừng [AppMonitoringWorker] trong những luồng đặc biệt mà foreground
 * package không phải app được phép nhưng KHÔNG được chặn:
 *
 * - Uninstall flow: system packageinstaller được launch bởi chính user từ màn Xoá app.
 *   Nếu để worker chặn thì popup xoá bị BlockActivity đè lên, user không confirm được.
 *
 * API là key-value map: mỗi caller (fragment / service) tự identify bằng key riêng
 * (thường là tên class). Ưu điểm so với counter:
 * - Idempotent: gọi acquire(key) 2 lần chỉ tính 1 → không sợ leak counter.
 * - Debug dễ: nhìn [holders] thấy ai đang giữ pause.
 * - Không lệch cặp acquire/release.
 */
object AppMonitoringPauser {

    // ConcurrentHashMap: thread-safe cho put/remove/isEmpty vì worker check ở HandlerThread
    // còn fragment gọi ở main thread.
    private val holders = ConcurrentHashMap<String, String>()

    val isPaused: Boolean
        get() = holders.isNotEmpty()

    /**
     * Đăng ký 1 pause holder.
     * @param key mã định danh duy nhất của caller (ví dụ tên class).
     * @param reason mô tả ngắn để debug (mặc định = key).
     */
    fun acquire(key: String, reason: String = key) {

        holders[key] = reason
    }

    /** Bỏ đăng ký pause holder theo [key]. No-op nếu key không tồn tại. */
    fun release(key: String) {

        holders.remove(key)
    }
}
