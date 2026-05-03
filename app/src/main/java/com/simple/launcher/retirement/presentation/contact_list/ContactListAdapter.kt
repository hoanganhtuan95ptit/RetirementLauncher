package com.simple.launcher.retirement.presentation.contact_list

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simple.launcher.retirement.R
import com.simple.launcher.retirement.domain.model.SelectableContactEntity

class ContactListAdapter(
    private val contacts: List<SelectableContactEntity>
) : RecyclerView.Adapter<ContactListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvLabel: TextView = view.findViewById(R.id.tvLabel)
        val cbSelected: CheckBox = view.findViewById(R.id.cbSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selectable_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = contacts[position]
        holder.tvLabel.text = item.contact.name
        if (item.contact.photoUri != null) {
            holder.ivIcon.setImageURI(Uri.parse(item.contact.photoUri))
        } else {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_call)
        }
        holder.cbSelected.isChecked = item.isSelected
        
        holder.itemView.setOnClickListener {
            item.isSelected = !item.isSelected
            holder.cbSelected.isChecked = item.isSelected
        }
    }

    override fun getItemCount() = contacts.size
}
