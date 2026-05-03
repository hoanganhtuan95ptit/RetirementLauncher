package com.simple.launcher.retirement.presentation.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.HomeItem

class HomeAdapter(
    private val items: List<HomeItem>,
    private val onItemClick: (HomeItem) -> Unit
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvLabel: TextView = view.findViewById(R.id.tvLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        when (item) {
            is HomeItem.App -> {
                holder.tvLabel.text = item.entity.label
                holder.ivIcon.setImageDrawable(item.entity.icon)
            }
            is HomeItem.Contact -> {
                holder.tvLabel.text = item.entity.name
                if (item.entity.photoUri != null) {
                    holder.ivIcon.setImageURI(Uri.parse(item.entity.photoUri))
                } else {
                    holder.ivIcon.setImageResource(android.R.drawable.sym_action_call)
                    holder.ivIcon.setBackgroundResource(android.R.color.holo_green_light)
                }
            }
        }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}
