package com.simple.launcher.retirement.domain.model

data class ExclusionPeriod(
    val id: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
) {

    val startTotalMinutes: Int get() = startHour * 60 + startMinute
    val endTotalMinutes: Int get() = endHour * 60 + endMinute

    /**
     * Kiểm tra xem một thời điểm (tổng số phút từ nửa đêm) có nằm trong khoảng này không.
     */
    fun contains(totalMinutes: Int): Boolean {

        val t = totalMinutes % 1440
        return if (startTotalMinutes <= endTotalMinutes) {

            t in startTotalMinutes..endTotalMinutes
        } else {

            // Trường hợp qua đêm (ví dụ: 22:00 đến 07:00)
            t >= startTotalMinutes || t <= endTotalMinutes
        }
    }
}
