package com.simple.launcher.retirement.presentation.calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.ItemCalendarDayBinding

/**
 * Adapter đơn giản render lưới ngày trong 1 tháng.
 * Không dùng PrecomputedAdapter vì mỗi ô có logic màu/nền/label khác nhau,
 * dùng view thường dễ đọc và maintain hơn cho use-case này.
 */
class CalendarDayAdapter : RecyclerView.Adapter<CalendarDayAdapter.DayHolder>() {

    private val items = mutableListOf<CalendarDayItem>()

    fun submit(list: List<CalendarDayItem>) {

        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayHolder {

        val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayHolder(binding)
    }

    override fun onBindViewHolder(holder: DayHolder, position: Int) {

        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class DayHolder(private val binding: ItemCalendarDayBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalendarDayItem) {

            if (item.solarDay == 0) {

                // Ô placeholder - trống hoàn toàn
                binding.tvSolarDay.text = ""
                binding.tvLunarDay.visibility = View.GONE
                binding.dayContainer.setBackgroundResource(0)
                return
            }

            binding.tvSolarDay.text = item.solarDay.toString()

            // Highlight ngày hôm nay bằng background primary + chữ trắng
            if (item.isToday) {

                binding.dayContainer.setBackgroundResource(R.drawable.bg_calendar_day_today)
                binding.tvSolarDay.setTextColor(Color.WHITE)
            } else {

                binding.dayContainer.setBackgroundResource(0)

                val color = when {

                    !item.isCurrentMonth -> 0x66000000.toInt() // mờ cho tháng khác
                    item.isSunday -> 0xFFE53935.toInt() // Chủ nhật màu đỏ
                    else -> 0xFF1C1C1E.toInt() // primary text
                }
                binding.tvSolarDay.setTextColor(color)
            }

            val lunarText = item.lunarText
            if (lunarText != null) {

                binding.tvLunarDay.visibility = View.VISIBLE
                binding.tvLunarDay.text = lunarText

                val lunarColor = when {

                    item.isToday -> 0xE6FFFFFF.toInt()
                    item.isFirstOfLunarMonth -> 0xFFE53935.toInt() // mùng 1 tô đỏ
                    !item.isCurrentMonth -> 0x66000000.toInt()
                    else -> 0xFF6C63FF.toInt()
                }
                binding.tvLunarDay.setTextColor(lunarColor)
            } else {

                binding.tvLunarDay.visibility = View.GONE
            }
        }
    }
}
