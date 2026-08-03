package com.simple.launcher.retirement.presentation.home.adapter

import com.simple.adapter.ViewItem

interface HomeItem : ViewItem {

    val spanSize: Int get() = TOTAL_COLUMNS / 3 // default: 1/3 tổng cột

    companion object {

        const val TOTAL_COLUMNS = 6
    }
}
