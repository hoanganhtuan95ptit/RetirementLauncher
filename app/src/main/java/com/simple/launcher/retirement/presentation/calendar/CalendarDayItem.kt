package com.simple.launcher.retirement.presentation.calendar

/**
 * Một ô ngày trong lưới lịch tháng.
 *
 * @param solarDay    Ngày dương lịch (1..31), 0 nếu ô trống (leading/trailing).
 * @param lunarText   Chuỗi hiển thị ngày âm, ví dụ "1/8" cho mùng 1 tháng 8;
 *                    null nếu người dùng tắt lịch âm hoặc là ô trống.
 * @param isToday     True nếu ô này là ngày hôm nay.
 * @param isCurrentMonth  True nếu thuộc tháng đang xem (ô hợp lệ).
 * @param isSunday    True nếu là Chủ Nhật (tô đỏ chữ).
 * @param isFirstOfLunarMonth  True nếu là mùng 1 âm (hiện "1/tháng" thay vì chỉ ngày).
 */
data class CalendarDayItem(
    val solarDay: Int,
    val lunarText: String?,
    val isToday: Boolean,
    val isCurrentMonth: Boolean,
    val isSunday: Boolean,
    val isFirstOfLunarMonth: Boolean
)
