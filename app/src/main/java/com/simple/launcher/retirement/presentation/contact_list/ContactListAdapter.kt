package com.simple.launcher.retirement.presentation.contact_list

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simple.launcher.retirement.databinding.ItemSelectableAppBinding
import com.simple.launcher.retirement.domain.model.SelectableContactEntity

class ContactListAdapter(
    private val contacts: List<SelectableContactEntity>
) : RecyclerView.Adapter<ContactListAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSelectableAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectableAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = contacts[position]
        with(holder.binding) {
            tvLabel.text = item.contact.name
            if (item.contact.photoUri != null) {
                ivIcon.setImageURI(Uri.parse(item.contact.photoUri))
            } else {
                ivIcon.setImageResource(android.R.drawable.ic_menu_call)
            }
            cbSelected.isChecked = item.isSelected
            
            root.setOnClickListener {
                item.isSelected = !item.isSelected
                cbSelected.isChecked = item.isSelected
            }
        }
    }

    override fun getItemCount() = contacts.size
}
