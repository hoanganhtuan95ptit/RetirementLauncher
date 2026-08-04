package com.simple.launcher.retirement.domain.model

/**
 * Cấu hình tính năng "Chặn thông báo":
 * - [blockedPackages]: các app bị chặn hoàn toàn (dismiss ngay khi post).
 * - [retentionMillis]: thời gian tối đa (ms) một notification được phép tồn tại
 *   trước khi bị tự xoá. `0` = tắt tính năng tự xoá.
 */
data class NotificationBlockConfig(
    val blockedPackages: Set<String>,
    val retentionMillis: Long
) {

    companion object {

        val DEFAULT = NotificationBlockConfig(
            blockedPackages = emptySet(),
            retentionMillis = 0L
        )
    }
}
