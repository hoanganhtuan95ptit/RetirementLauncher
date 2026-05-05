package com.simple.launcher.retirement.presentation.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simple.launcher.retirement.databinding.ItemSettingBinding

class SettingsAdapter(
    private val items: List<SettingItem>,
    private val onItemClick: (SettingItem) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSettingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvSettingTitle.text = item.title
            ivSettingIcon.setImageResource(item.iconRes)
            
            if (item.isSwitch) {
                swSetting.visibility = View.VISIBLE
                swSetting.isChecked = item.isChecked
                swSetting.setOnCheckedChangeListener { _, isChecked ->
                    item.isChecked = isChecked
                    onItemClick(item)
                }
                root.setOnClickListener {
                    swSetting.toggle()
                }
            } else {
                swSetting.visibility = View.GONE
                swSetting.setOnCheckedChangeListener(null)
                root.setOnClickListener { onItemClick(item) }
            }
        }
    }

    override fun getItemCount() = items.size
}
