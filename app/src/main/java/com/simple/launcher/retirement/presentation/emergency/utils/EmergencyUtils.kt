package com.simple.launcher.retirement.presentation.emergency.utils

import com.simple.launcher.retirement.domain.model.ExclusionPeriod
import java.util.Calendar

object EmergencyUtils {

    /**
     * Tính toán tổng thời gian "hoạt động" (nằm ngoài các khung giờ loại trừ)
     * giữa hai mốc thời gian.
     */
    fun calculateActiveElapsedMillis(
        startTime: Long,
        endTime: Long,
        exclusionPeriods: List<ExclusionPeriod>
    ): Long {

        if (exclusionPeriods.isEmpty()) return endTime - startTime

        var totalActiveMillis = 0L
        var current = startTime
        val step = 60 * 1000L // Tính toán theo từng phút (60 giây)

        val calendar = Calendar.getInstance()

        while (current < endTime) {

            calendar.timeInMillis = current
            val minutesFromMidnight = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

            // Mỗi bước đại diện cho 1 phút;
            // phút nằm trong exclusion period sẽ không cộng vào timeout SOS.
            val isExcluded = exclusionPeriods.any { it.contains(minutesFromMidnight) }
            if (!isExcluded) {

                totalActiveMillis += step
            }
            current += step
        }

        return totalActiveMillis
    }
}
