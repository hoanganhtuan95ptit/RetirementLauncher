package com.simple.launcher.retirement.presentation.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.simple.deeplink.Deeplink
import com.simple.deeplink.DeeplinkHandler
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.databinding.FragmentCalendarBinding
import com.simple.launcher.retirement.presentation.DeepLinks
import com.simple.launcher.retirement.presentation.base.BaseFragment
import com.simple.launcher.retirement.utils.exts.getString
import com.simple.launcher.retirement.utils.exts.observe
import com.simple.launcher.retirement.utils.exts.setOnSafeClickListener
import com.simple.ui.precompute.image.setImage
import com.simple.ui.precompute.text.setText
import com.simple.launcher.retirement.presentation.base.buildBackIcon
import com.simple.launcher.retirement.presentation.base.buildToolbarTitle
import com.simple.launcher.retirement.utils.exts.textColorPrimary

/**
 * Màn hình lịch dành cho người cao tuổi:
 * - Chữ to, ô lớn dễ chạm.
 * - Hiện ngày hôm nay nổi bật.
 * - Bên dưới mỗi ngày dương có ngày âm (nếu người dùng bật lịch âm trong Cài đặt đồng hồ).
 * - Điều hướng < tháng trước, > tháng sau, nút "Hôm nay" đưa về tháng hiện tại.
 */
class CalendarFragment : BaseFragment<FragmentCalendarBinding>() {

    private val viewModel: CalendarViewModel by viewModels()

    private val dayAdapter = CalendarDayAdapter()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentCalendarBinding {

        return FragmentCalendarBinding.inflate(inflater, container, false)
    }

    override fun setupViews(view: View, savedInstanceState: Bundle?) {

        super.setupViews(view, savedInstanceState)

        val binding = binding ?: return

        binding.rvDays.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.rvDays.adapter = dayAdapter

        binding.toolbar.ivLeft.setOnSafeClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnPrevMonth.setOnSafeClickListener {

            viewModel.previousMonth()
        }

        binding.btnNextMonth.setOnSafeClickListener {

            viewModel.nextMonth()
        }

        binding.btnToday.setOnSafeClickListener {

            viewModel.goToday()
        }
    }

    override fun observeData() {

        super.observeData()

        viewModel.resources.observe(viewLifecycleOwner) { res ->

            val binding = binding ?: return@observe
            val color = res.textColorPrimary
            binding.toolbar.tvTitle.setText(buildToolbarTitle(res.getString(R.string.calendar_title), color))
            binding.toolbar.ivLeft.visibility = View.VISIBLE
            binding.toolbar.ivLeft.setImage(buildBackIcon(color))

            // Weekday header — update mỗi khi locale/string map đổi
            binding.tvWd0.text = res.getString(R.string.calendar_weekday_mon)
            binding.tvWd1.text = res.getString(R.string.calendar_weekday_tue)
            binding.tvWd2.text = res.getString(R.string.calendar_weekday_wed)
            binding.tvWd3.text = res.getString(R.string.calendar_weekday_thu)
            binding.tvWd4.text = res.getString(R.string.calendar_weekday_fri)
            binding.tvWd5.text = res.getString(R.string.calendar_weekday_sat)
            binding.tvWd6.text = res.getString(R.string.calendar_weekday_sun)
        }

        viewModel.monthTitle.observe(viewLifecycleOwner) { title ->

            binding?.tvMonthYear?.text = title
        }

        viewModel.days.observe(viewLifecycleOwner) { list ->

            dayAdapter.submit(list)
        }

        viewModel.todaySolarText.observe(viewLifecycleOwner) { text ->

            binding?.tvTodaySolar?.text = text
        }

        viewModel.todayLunarText.observe(viewLifecycleOwner) { text ->

            val binding = binding ?: return@observe
            if (text.isBlank()) {

                binding.tvTodayLunar.visibility = View.GONE
            } else {

                binding.tvTodayLunar.visibility = View.VISIBLE
                binding.tvTodayLunar.text = text
            }
        }
    }
}

@Deeplink
class CalendarDeeplinkHandler : DeeplinkHandler {

    override val deeplink: String = DeepLinks.CALENDAR

    override suspend fun navigate(
        fragmentActivity: FragmentActivity,
        deeplink: String,
        extras: Map<String, Any?>?,
        sharedElement: Map<String, View>?
    ): Boolean {

        val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CalendarFragment())

        if (extras?.get(DeepLinks.Extras.ADD_TO_BACK_STACK) == true) {

            transaction.addToBackStack(DeepLinks.CALENDAR)
        }

        transaction.commitAllowingStateLoss()
        return true
    }
}
