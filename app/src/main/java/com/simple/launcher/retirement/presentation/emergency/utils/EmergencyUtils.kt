package com.simple.launcher.retirement.presentation.emergency.utils

import com.simple.launcher.retirement.domain.model.ExclusionPeriod
import java.util.Calendar

object EmergencyUtils {

    // ── 3. Public API ─────────────────────────────────────────────────────
    /**
     * Tính tổng thời gian "hoạt động" (nằm ngoài các khung giờ loại trừ)
     * giữa hai mốc thời gian.
     *
     * Semantics giữ nguyên như phiên bản loop-per-minute cũ:
     *  - Chia [startTime, endTime) thành các bước 1 phút.
     *  - Với mỗi bước, lấy minute-of-day tại thời điểm hiện tại;
     *    nếu rơi vào bất kỳ [ExclusionPeriod] nào → không cộng.
     *
     * Tối ưu:
     *  - Precompute mask 1440 phút (1 lần / call) thay vì gọi `any { it.contains(..) }`
     *    mỗi phút.
     *  - Đọc HOUR/MINUTE của startTime 1 lần bằng Calendar rồi tự cộng dồn thay vì
     *    reset Calendar mỗi vòng.
     */
    fun calculateActiveElapsedMillis(
        startTime: Long,
        endTime: Long,
        exclusionPeriods: List<ExclusionPeriod>
    ): Long {

        if (endTime <= startTime) return 0L
        if (exclusionPeriods.isEmpty()) return endTime - startTime

        val iterations = ((endTime - startTime + STEP_MILLIS - 1) / STEP_MILLIS).toInt()
        if (iterations <= 0) return 0L

        val excludedMask = buildExcludedMask(exclusionPeriods)
        val startMinuteOfDay = resolveMinuteOfDay(startTime)

        var excludedCount = 0
        var minuteOfDay = startMinuteOfDay
        var i = 0
        while (i < iterations) {

            if (excludedMask[minuteOfDay]) excludedCount++
            minuteOfDay++
            if (minuteOfDay == MINUTES_PER_DAY) minuteOfDay = 0
            i++
        }

        val activeSteps = iterations - excludedCount
        return activeSteps * STEP_MILLIS
    }

    // ── 4. Private helpers ────────────────────────────────────────────────
    private fun buildExcludedMask(exclusionPeriods: List<ExclusionPeriod>): BooleanArray {

        val mask = BooleanArray(MINUTES_PER_DAY)
        exclusionPeriods.forEach { period ->

            val start = period.startTotalMinutes.coerceIn(0, MINUTES_PER_DAY - 1)
            val end = period.endTotalMinutes.coerceIn(0, MINUTES_PER_DAY - 1)

            // ExclusionPeriod.contains dùng phạm vi inclusive-inclusive, ta mô phỏng lại.
            if (start <= end) {

                for (m in start..end) mask[m] = true
            } else {

                // Khoảng qua đêm: [start .. 1439] ∪ [0 .. end]
                for (m in start until MINUTES_PER_DAY) mask[m] = true
                for (m in 0..end) mask[m] = true
            }
        }
        return mask
    }

    private fun resolveMinuteOfDay(timeMillis: Long): Int {

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }

    // ── 6. Constants (companion-equivalent trong object) ──────────────────
    private const val MINUTES_PER_DAY = 1440
    private const val STEP_MILLIS = 60L * 1000L
}
