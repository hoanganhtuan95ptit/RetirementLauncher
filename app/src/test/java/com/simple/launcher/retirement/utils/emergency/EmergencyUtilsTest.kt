package com.simple.launcher.retirement.utils.emergency

import com.simple.launcher.retirement.domain.model.ExclusionPeriod
import com.simple.launcher.retirement.presentation.emergency.utils.EmergencyUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class EmergencyUtilsTest {

    @Before
    fun setup() {

        // Sử dụng UTC để kết quả tính toán ổn định trên mọi môi trường test
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    /**
     * Helper để tạo mốc thời gian millisecond dễ dàng.
     */
    private fun createTime(hour: Int, minute: Int, dayOffset: Int = 0): Long {

        val calendar = Calendar.getInstance()
        calendar.set(2026, Calendar.JULY, 17, hour, minute, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        if (dayOffset != 0) {
            calendar.add(Calendar.DAY_OF_YEAR, dayOffset)
        }
        return calendar.timeInMillis
    }

    @Test
    fun `calculateActiveElapsedMillis - không có khung giờ loại trừ`() {

        val start = createTime(10, 0)
        val end = createTime(12, 0)
        val result = EmergencyUtils.calculateActiveElapsedMillis(start, end, emptyList())

        // 12h - 10h = 2h = 7,200,000 ms
        assertEquals(2 * 60 * 60 * 1000L, result)
    }

    @Test
    fun `calculateActiveElapsedMillis - có khung giờ loại trừ đơn lẻ`() {

        val start = createTime(20, 0) // 8 PM
        val end = createTime(23, 0)   // 11 PM
        // Loại trừ: 22:00 - 07:00 (giờ ngủ)
        val periods = listOf(ExclusionPeriod("1", 22, 0, 7, 0))

        val result = EmergencyUtils.calculateActiveElapsedMillis(start, end, periods)

        // 20:00 -> 22:00: Hoạt động (2 tiếng)
        // 22:00 -> 23:00: Bị loại trừ
        // Tổng cộng: 2 tiếng
        assertEquals(2 * 60 * 60 * 1000L, result)
    }

    @Test
    fun `calculateActiveElapsedMillis - vượt qua khung giờ loại trừ đêm`() {

        val start = createTime(20, 0)   // Ngày 1, 8 PM
        val end = createTime(8, 0, 1)   // Ngày 2, 8 AM
        // Loại trừ: 22:00 - 07:00
        val periods = listOf(ExclusionPeriod("1", 22, 0, 7, 0))

        val result = EmergencyUtils.calculateActiveElapsedMillis(start, end, periods)

        // Ngày 1: 20:00 -> 22:00 (2 tiếng hoạt động)
        // Đêm: 22:00 -> 07:00 sáng mai (Bị loại trừ)
        // Ngày 2: 07:01 -> 08:00
        // (60 phút hoạt động - 07:00 vẫn bị coi là excluded do logic contains inclusive)
        // Lưu ý: Thuật toán chạy step 1 phút, t=420 (07:00) isExcluded=true, t=421 isExcluded=false.
        // Vậy 07:00 -> 07:01 là phút thứ 541 bị loại trừ.
        // Hoạt động thực tế: [20:00, 22:00) và [07:01, 08:00)
        // 120 phút + 59 phút = 179 phút
        assertEquals(179 * 60 * 1000L, result)
    }

    @Test
    fun `calculateActiveElapsedMillis - tuong tac cuoi trong gio loai tru`() {

        // Khung giờ loại trừ: 22:00 - 07:00
        val periods = listOf(ExclusionPeriod("1", 22, 0, 7, 0))

        // Tương tác cuối: 23:00 (đang trong giờ ngủ)
        val start = createTime(23, 0)
        // Hiện tại: 08:00 sáng mai
        val end = createTime(8, 0, 1)

        val result = EmergencyUtils.calculateActiveElapsedMillis(start, end, periods)

        // Phân tích:
        // 23:00 -> 07:00: Bị loại trừ (0 phút)
        // 07:01 -> 08:00: Hoạt động (59 phút)
        // Tổng: 59 phút
        assertEquals(59 * 60 * 1000L, result)
    }

    @Test
    fun `calculateActiveElapsedMillis - tuong tac truoc gio loai tru`() {

        // Khung giờ loại trừ: 22:00 - 07:00
        val periods = listOf(ExclusionPeriod("1", 22, 0, 7, 0))

        // Tương tác cuối: 21:00 (trước giờ ngủ 1 tiếng)
        val start = createTime(21, 0)
        // Hiện tại: 08:00 sáng mai
        val end = createTime(8, 0, 1)

        val result = EmergencyUtils.calculateActiveElapsedMillis(start, end, periods)

        // Phân tích:
        // 21:00 -> 22:00: Hoạt động (60 phút)
        // 22:00 -> 07:00: Bị loại trừ (0 phút)
        // 07:01 -> 08:00: Hoạt động (59 phút)
        // Tổng: 60 + 59 = 119 phút
        assertEquals(119 * 60 * 1000L, result)
    }

    @Test
    fun `calculateActiveElapsedMillis - nhiều khung giờ loại trừ`() {

        val start = createTime(10, 0)
        val end = createTime(15, 0)
        // Loại trừ 1: 12:00 - 13:00 (ngủ trưa)
        // Loại trừ 2: 14:00 - 14:30 (nghỉ ngơi)
        val periods = listOf(
            ExclusionPeriod("1", 12, 0, 13, 0),
            ExclusionPeriod("2", 14, 0, 14, 30)
        )

        val result = EmergencyUtils.calculateActiveElapsedMillis(start, end, periods)

        // 10:00 -> 12:00 (120p)
        // 12:00 -> 13:01 (Bị loại trừ - 61p)
        // 13:01 -> 14:00 (59p)
        // 14:00 -> 14:31 (Bị loại trừ - 31p)
        // 14:31 -> 15:00 (29p)
        // Tổng: 120 + 59 + 29 = 208 phút
        assertEquals(208 * 60 * 1000L, result)
    }
}
