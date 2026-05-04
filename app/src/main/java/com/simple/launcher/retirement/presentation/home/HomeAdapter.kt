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
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_CLOCK = 0
        private const val TYPE_APP = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HomeItem.Clock -> TYPE_CLOCK
            else -> TYPE_APP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CLOCK -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clock, parent, false)
                ClockViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
                AppViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ClockViewHolder -> {
                // TextClock handles itself
            }
            is AppViewHolder -> {
                val context = holder.itemView.context
                // Reset common states
                holder.ivIcon.setBackgroundResource(0)
                holder.ivIcon.setPadding(0, 0, 0, 0)

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
                            holder.ivIcon.setPadding(16, 16, 16, 16)
                        }
                    }
                    is HomeItem.CleanFiles -> {
                        holder.tvLabel.text = context.getString(R.string.home_strange_files, item.fileCount)
                        holder.ivIcon.setImageResource(android.R.drawable.ic_menu_delete)
                        holder.ivIcon.setBackgroundResource(android.R.color.holo_orange_light)
                        holder.ivIcon.setPadding(16, 16, 16, 16)
                    }
                    is HomeItem.CleanMemory -> {
                        holder.tvLabel.text = context.getString(R.string.home_memory_status, item.memoryMB)
                        holder.ivIcon.setImageResource(android.R.drawable.ic_media_play)
                        holder.ivIcon.setBackgroundResource(android.R.color.holo_blue_light)
                        holder.ivIcon.setPadding(16, 16, 16, 16)
                    }
                    else -> {}
                }
                holder.itemView.setOnClickListener { onItemClick(item) }
            }
        }
    }

    override fun getItemCount() = items.size

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvLabel: TextView = view.findViewById(R.id.tvLabel)
    }

    class ClockViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
