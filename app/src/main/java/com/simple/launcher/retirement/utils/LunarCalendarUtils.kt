package com.simple.launcher.retirement.utils

import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

/**
 * Vietnamese Lunar Calendar (âm lịch) based on Ho Ngoc Duc's algorithm.
 * Reference: https://www.informatik.uni-leipzig.de/~duc/amlich/
 *
 * All calculations use the Vietnam time zone (GMT+7).
 */
object LunarCalendarUtils {

    private const val TIME_ZONE_OFFSET = 7
    private val VN_TZ: TimeZone = TimeZone.getTimeZone("GMT+7")

    /** Cache the last conversion so repeated calls on the same day are free. */
    @Volatile
    private var cache: Entry? = null

    private data class Entry(val key: Long, val lunar: IntArray)

    /**
     * Returns the lunar date formatted with [format] which must accept two
     * integer arguments: day and month (in that order).
     * Example: `"Ngày %1$d/%2$d âm lịch"`.
     */
    fun getLunarDateString(date: Date, format: String): String {

        val lunar = getLunar(date)
        return try {

            String.format(format, lunar[0], lunar[1])
        } catch (_: Exception) {

            "Ngày ${lunar[0]}/${lunar[1]} âm lịch"
        }
    }

    /** Returns `[day, month, year, isLeap]` for the given solar [date]. */
    fun getLunar(date: Date): IntArray {

        val cal = Calendar.getInstance(VN_TZ)
        cal.time = date
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val m = cal.get(Calendar.MONTH) + 1
        val y = cal.get(Calendar.YEAR)

        val key = (y.toLong() * 10000L) + (m.toLong() * 100L) + d.toLong()
        cache?.let { if (it.key == key) return it.lunar }

        val lunar = convertSolar2Lunar(d, m, y, TIME_ZONE_OFFSET)
        cache = Entry(key, lunar)
        return lunar
    }

    // ---------------------------------------------------------------------
    // Ho Ngoc Duc's astronomical algorithm
    // ---------------------------------------------------------------------

    private fun jdFromDate(dd: Int, mm: Int, yy: Int): Int {

        val a = (14 - mm) / 12
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        var jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        if (jd < 2299161) {

            jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083
        }
        return jd
    }

    private fun jdToDate(jd: Int): IntArray {

        val a: Int
        val b: Int
        val c: Int
        if (jd > 2299160) {

            a = jd + 32044
            b = (4 * a + 3) / 146097
            c = a - (146097 * b) / 4
        } else {

            b = 0
            c = jd + 32082
        }
        val d = (4 * c + 3) / 1461
        val e = c - (1461 * d) / 4
        val m = (5 * e + 2) / 153
        val day = e - (153 * m + 2) / 5 + 1
        val month = m + 3 - 12 * (m / 10)
        val year = b * 100 + d - 4800 + m / 10
        return intArrayOf(day, month, year)
    }

    private fun newMoon(k: Int): Double {

        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val dr = PI / 180.0
        var jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
        jd1 += 0.00033 * sin((166.56 + 132.87 * t - 0.009173 * t2) * dr)
        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
        val mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
        val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3
        var c1 = (0.1734 - 0.000393 * t) * sin(m * dr) + 0.0021 * sin(2 * dr * m)
        c1 -= 0.4068 * sin(mpr * dr) + 0.0161 * sin(dr * 2 * mpr)
        c1 -= 0.0004 * sin(dr * 3 * mpr)
        c1 += 0.0104 * sin(dr * 2 * f) - 0.0051 * sin(dr * (m + mpr))
        c1 -= 0.0074 * sin(dr * (m - mpr)) + 0.0004 * sin(dr * (2 * f + m))
        c1 -= 0.0004 * sin(dr * (2 * f - m)) - 0.0006 * sin(dr * (2 * f + mpr))
        c1 += 0.0010 * sin(dr * (2 * f - mpr)) + 0.0005 * sin(dr * (2 * mpr + m))
        val deltat = if (t < -11) {

            0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
        } else {

            -0.000278 + 0.000265 * t + 0.000262 * t2
        }
        return jd1 + c1 - deltat
    }

    private fun sunLongitude(jdn: Double): Double {

        val t = (jdn - 2451545.0) / 36525.0
        val t2 = t * t
        val dr = PI / 180.0
        val m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
        var dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * sin(dr * m)
        dl += (0.019993 - 0.000101 * t) * sin(dr * 2 * m) + 0.000290 * sin(dr * 3 * m)
        var l = l0 + dl
        l *= dr
        l -= PI * 2 * floor(l / (PI * 2))
        return l
    }

    private fun getNewMoonDay(k: Int, timeZone: Int): Int {

        return floor(newMoon(k) + 0.5 + timeZone / 24.0).toInt()
    }

    private fun getSunLongitude(dayNumber: Int, timeZone: Int): Int {

        return floor(sunLongitude(dayNumber - 0.5 - timeZone / 24.0) / PI * 6).toInt()
    }

    private fun getLunarMonth11(yy: Int, timeZone: Int): Int {

        val off = jdFromDate(31, 12, yy) - 2415021
        val k = floor(off / 29.530588853).toInt()
        var nm = getNewMoonDay(k, timeZone)
        val sunLong = getSunLongitude(nm, timeZone)
        if (sunLong >= 9) {

            nm = getNewMoonDay(k - 1, timeZone)
        }
        return nm
    }

    private fun getLeapMonthOffset(a11: Int, timeZone: Int): Int {

        val k = floor((a11 - 2415021.076998695) / 29.530588853 + 0.5).toInt()
        var last = 0
        var i = 1
        var arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        do {

            last = arc
            i++
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        } while (arc != last && i < 14)
        return i - 1
    }

    /** Returns `[lunarDay, lunarMonth, lunarYear, isLeap]`. */
    private fun convertSolar2Lunar(dd: Int, mm: Int, yy: Int, timeZone: Int): IntArray {

        val dayNumber = jdFromDate(dd, mm, yy)
        val k = floor((dayNumber - 2415021.076998695) / 29.530588853).toInt()
        var monthStart = getNewMoonDay(k + 1, timeZone)
        if (monthStart > dayNumber) {

            monthStart = getNewMoonDay(k, timeZone)
        }
        var a11 = getLunarMonth11(yy, timeZone)
        var b11 = a11
        val lunarYear: Int
        if (a11 >= monthStart) {

            lunarYear = yy
            a11 = getLunarMonth11(yy - 1, timeZone)
        } else {

            lunarYear = yy + 1
            b11 = getLunarMonth11(yy + 1, timeZone)
        }
        val lunarDay = dayNumber - monthStart + 1
        val diff = ((monthStart - a11) / 29).toInt()
        val (lunarMonth, lunarLeap) = resolveLeapMonth(diff, a11, b11, timeZone)
        val adjustedMonth = if (lunarMonth > 12) lunarMonth - 12 else lunarMonth
        val yearOut = if (adjustedMonth >= 11 && diff < 4) lunarYear - 1 else lunarYear
        return intArrayOf(lunarDay, adjustedMonth, yearOut, lunarLeap)
    }

    /** Returns `(lunarMonth, lunarLeap)` — tách nhánh xử lý tháng nhuận để giảm nesting. */
    private fun resolveLeapMonth(diff: Int, a11: Int, b11: Int, timeZone: Int): Pair<Int, Int> {

        if (b11 - a11 <= 365) return (diff + 11) to 0
        val leapMonthDiff = getLeapMonthOffset(a11, timeZone)
        if (diff < leapMonthDiff) return (diff + 11) to 0
        val lunarLeap = if (diff == leapMonthDiff) 1 else 0
        return (diff + 10) to lunarLeap
    }
}