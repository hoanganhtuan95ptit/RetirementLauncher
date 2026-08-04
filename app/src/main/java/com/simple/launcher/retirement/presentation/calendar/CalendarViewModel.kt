package com.simple.launcher.retirement.presentation.calendar

import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.utils.LunarCalendarUtils
import com.simple.launcher.retirement.utils.exts.combineState
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.mutableStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * State container cho màn calendar.
 * - Tự đọc setting âm lịch từ [PreferenceRepository].
 * - Expose danh sách ô ngày + tiêu đề tháng + info hôm nay.
 * - Điều hướng previous / next / today.
 *
 * Tuần bắt đầu từ Thứ Hai theo thói quen VN.
 */
class CalendarViewModel : BaseViewModel() {

    private val repo = PreferenceRepository.instance

    // Anchor về đầu tháng đang xem.
    private val displayedMonth: MutableStateFlow<Calendar> = MutableStateFlow(currentMonth())

    val isLunarEnabled: StateFlow<Boolean> = mutableStateFlow(repo.isLunarCalendarEnabled()) {

        repo.lunarCalendarEnabledFlow().collect { value = it }
    }

    /** "Tháng 8 2026" / "August 2026" theo locale + pattern trong strings. */
    val monthTitle: StateFlow<String> = combineState(
        flow1 = resources,
        flow2 = displayedMonth,
        initialValue = ""
    ) { resources, cal ->

        val pattern = resources.getString(R.string.calendar_month_year_format)
        value = SimpleDateFormat(pattern, Locale.getDefault()).format(cal.time)
    }

    /** Grid ô ngày (tuần bắt đầu Thứ Hai). Luôn đủ 42 ô = 6 hàng. */
    val days: StateFlow<List<CalendarDayItem>> = combineState(
        flow1 = displayedMonth,
        flow2 = isLunarEnabled,
        initialValue = emptyList()
    ) { cal, showLunar ->

        value = buildMonthGrid(cal, showLunar)
    }

    /** Text hiển thị dưới cùng: "Thứ Ba, 04/08/2026". */
    val todaySolarText: StateFlow<String> = combineState(
        flow1 = resources,
        initialValue = ""
    ) { resources ->

        val pattern = resources.getString(R.string.solar_date_format)
        value = SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
    }

    /** Text hiển thị "Ngày 21/6 âm lịch". Rỗng nếu tắt âm lịch. */
    val todayLunarText: StateFlow<String> = combineState(
        flow1 = resources,
        flow2 = isLunarEnabled,
        initialValue = ""
    ) { resources, showLunar ->

        value = if (showLunar) {

            LunarCalendarUtils.getLunarDateString(Date(), resources.getString(R.string.lunar_date_format))
        } else {

            ""
        }
    }

    fun previousMonth() {

        displayedMonth.value = (displayedMonth.value.clone() as Calendar).apply {

            add(Calendar.MONTH, -1)
        }
    }

    fun nextMonth() {

        displayedMonth.value = (displayedMonth.value.clone() as Calendar).apply {

            add(Calendar.MONTH, 1)
        }
    }

    fun goToday() {

        displayedMonth.value = currentMonth()
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private fun currentMonth(): Calendar = Calendar.getInstance().apply {

        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun buildMonthGrid(monthAnchor: Calendar, showLunar: Boolean): List<CalendarDayItem> {

        val year = monthAnchor.get(Calendar.YEAR)
        val month = monthAnchor.get(Calendar.MONTH)

        val firstOfMonth = (monthAnchor.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        val daysInMonth = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Sunday=1..Saturday=7 → convert sang index tuần bắt đầu Thứ 2 (Mon=0..Sun=6)
        val dowSunFirst = firstOfMonth.get(Calendar.DAY_OF_WEEK)
        val leadingBlanks = ((dowSunFirst + 5) % 7) // Mon->0, Tue->1, ..., Sun->6

        val today = Calendar.getInstance()
        val isTodayInThisMonth =
            today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

        val totalCells = 42
        val items = ArrayList<CalendarDayItem>(totalCells)

        // Ô trống đầu tháng
        repeat(leadingBlanks) {

            items.add(
                CalendarDayItem(
                    solarDay = 0,
                    lunarText = null,
                    isToday = false,
                    isCurrentMonth = false,
                    isSunday = false,
                    isFirstOfLunarMonth = false
                )
            )
        }

        // Ngày thực trong tháng
        val cursor = firstOfMonth.clone() as Calendar
        for (day in 1..daysInMonth) {

            cursor.set(Calendar.DAY_OF_MONTH, day)
            val isSunday = cursor.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

            val lunarText: String?
            val isFirstOfLunar: Boolean
            if (showLunar) {

                val lunar = LunarCalendarUtils.getLunar(cursor.time)
                val lDay = lunar[0]
                val lMonth = lunar[1]
                isFirstOfLunar = lDay == 1
                // Mùng 1 âm hiển thị "1/tháng" để dễ nhận, ngày khác chỉ hiển thị số ngày
                lunarText = if (isFirstOfLunar) "$lDay/$lMonth" else lDay.toString()
            } else {

                lunarText = null
                isFirstOfLunar = false
            }

            items.add(
                CalendarDayItem(
                    solarDay = day,
                    lunarText = lunarText,
                    isToday = isTodayInThisMonth && day == todayDay,
                    isCurrentMonth = true,
                    isSunday = isSunday,
                    isFirstOfLunarMonth = isFirstOfLunar
                )
            )
        }

        // Ô trống cuối tháng cho đủ 42 ô
        while (items.size < totalCells) {

            items.add(
                CalendarDayItem(
                    solarDay = 0,
                    lunarText = null,
                    isToday = false,
                    isCurrentMonth = false,
                    isSunday = false,
                    isFirstOfLunarMonth = false
                )
            )
        }

        return items
    }
}
