package com.simple.launcher.retirement.domain.model

data class RamInfo(
    val totalMB: Long,
    val usedMB: Long,
    val freeMB: Long
) {
    val percent: Float
        get() = if (totalMB == 0L) 0f else usedMB.toFloat() / totalMB.toFloat()

    val percentInt: Int
        get() = (percent * 100).toInt()

    val totalGB: String
        get() = "%.1f GB".format(totalMB / 1024f)

    val usedGB: String
        get() = "%.1f GB".format(usedMB / 1024f)

    val freeGB: String
        get() = "%.1f GB".format(freeMB / 1024f)

    val detail: String
        get() = "$usedGB / $totalGB"
}
