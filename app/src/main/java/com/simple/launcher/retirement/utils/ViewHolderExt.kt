package com.simple.launcher.retirement.utils

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

inline fun <reified T> RecyclerView.ViewHolder.getItem(): T? {
    return (bindingAdapter as? ListAdapter<*, *>)?.currentList?.getOrNull(bindingAdapterPosition) as? T
}
