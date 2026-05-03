package com.simple.launcher.retirement.presentation.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.simple.launcher.retirement.R

class SettingsAdapter(
    private val items: List<SettingItem>,
    private val onItemClick: (SettingItem) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivSettingIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvSettingTitle)
        val swSetting: SwitchMaterial = view.findViewById(R.id.swSetting)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_setting, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.ivIcon.setImageResource(item.iconRes)
        
        if (item.isSwitch) {
            holder.swSetting.visibility = View.VISIBLE
            holder.swSetting.isChecked = item.isChecked
            holder.swSetting.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
                onItemClick(item)
            }
            holder.itemView.setOnClickListener {
                holder.swSetting.toggle()
            }
        } else {
            holder.swSetting.visibility = View.GONE
            holder.swSetting.setOnCheckedChangeListener(null)
            holder.itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = items.size
}
