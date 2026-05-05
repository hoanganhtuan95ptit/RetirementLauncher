package com.simple.launcher.retirement.presentation.app_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simple.launcher.retirement.databinding.ItemSelectableAppBinding
import com.simple.launcher.retirement.domain.model.SelectableAppEntity

class AppListAdapter(
    private val apps: List<SelectableAppEntity>
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSelectableAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectableAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = apps[position]
        with(holder.binding) {
            tvLabel.text = item.app.label
            ivIcon.setImageDrawable(item.app.icon)
            cbSelected.isChecked = item.isSelected
            
            root.setOnClickListener {
                item.isSelected = !item.isSelected
                cbSelected.isChecked = item.isSelected
            }
        }
    }

    override fun getItemCount() = apps.size
}
