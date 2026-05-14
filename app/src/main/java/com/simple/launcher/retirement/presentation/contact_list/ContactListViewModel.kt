package com.simple.launcher.retirement.presentation.contact_list

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.simple.launcher.retirement.presentation.base.BaseViewModel
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.domain.model.SelectableContactEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactListViewModel(
    private val repository: AppRepository
) : BaseViewModel() {

    private val _contacts = MutableLiveData<List<SelectableContactEntity>>()
    val contacts: LiveData<List<SelectableContactEntity>> = _contacts

    fun loadContacts(context: Context) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val selectedIds = repository.getSelectedContacts().map { it.id }.toSet()
                val contentResolver = context.contentResolver
                val cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )

                val contactsList = mutableListOf<SelectableContactEntity>()
                cursor?.use {
                    val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                    val processedIds = mutableSetOf<String>()

                    while (it.moveToNext()) {
                        val id = it.getString(idIndex)
                        if (processedIds.contains(id)) continue
                        
                        val name = it.getString(nameIndex)
                        val number = it.getString(numberIndex)
                        val photoUri = it.getString(photoIndex)

                        val contact = ContactEntity(id, name, number, photoUri)
                        contactsList.add(SelectableContactEntity(contact, selectedIds.contains(id)))
                        processedIds.add(id)
                    }
                }
                contactsList
            }
            _contacts.value = result
        }
    }

    fun updateItem(item: SelectableContactEntity) {
        val currentList = _contacts.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.contact.id == item.contact.id }
        if (index != -1) {
            currentList[index] = item.copy()
            _contacts.value = currentList
        }
    }

    fun saveSelection() {
        val selected = _contacts.value?.filter { it.isSelected }?.map { it.contact } ?: emptyList()
        repository.saveSelectedContacts(selected)
    }
}

class ContactListViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ContactListViewModel(repository) as T
    }
}
