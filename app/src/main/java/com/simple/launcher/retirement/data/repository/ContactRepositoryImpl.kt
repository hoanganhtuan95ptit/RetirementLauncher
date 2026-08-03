package com.simple.launcher.retirement.data.repository

import android.content.Context
import android.provider.ContactsContract
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.domain.repository.ContactRepository
import kotlinx.coroutines.flow.MutableSharedFlow

class ContactRepositoryImpl(private val context: Context) : ContactRepository {

    private val sharedPrefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Trigger để home data (contact) phát lại khi có thay đổi
    private val _dataTrigger = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }

    override fun homeDataFlow() = _dataTrigger

    override fun getAllContacts(context: Context): List<ContactEntity> {

        val contentResolver = context.contentResolver
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        )
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection, null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        val contactsList = mutableListOf<ContactEntity>()
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

                contactsList.add(ContactEntity(id, name, number, photoUri))
                processedIds.add(id)
            }
        }
        return contactsList
    }

    companion object {

        private const val KEY_SELECTED_CONTACTS = "selected_contacts"
    }

    // In-memory cache — tránh Gson.fromJson() mỗi lần getSelectedContacts() được gọi.
    // Invalidate khi saveSelectedContacts() được gọi.
    private var cachedContacts: List<ContactEntity>? = null

    override fun getSelectedContacts(): List<ContactEntity> {

        // Trả về cache nếu đã có
        cachedContacts?.let { cached ->
            // Vẫn trả về debug list nếu cache rỗng ở debug build
            if (cached.isNotEmpty()) return cached
//            val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
//            if (isDebug) return debugContacts()
            return cached
        }

        val data = sharedPrefs.all[KEY_SELECTED_CONTACTS]
        val contacts: List<ContactEntity> = if (data is String) {

            val type = TypeToken.getParameterized(List::class.java, ContactEntity::class.java).type
            try {

                gson.fromJson(data, type)
            } catch (_: Exception) {

                emptyList()
            }
        } else {

            emptyList()
        }

        cachedContacts = contacts

//        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
//        if (contacts.isEmpty() && isDebug) return debugContacts()
        return contacts
    }

    override fun saveSelectedContacts(contacts: List<ContactEntity>) {

        val json = gson.toJson(contacts)
        sharedPrefs.edit { putString(KEY_SELECTED_CONTACTS, json) }
        cachedContacts = contacts  // cập nhật cache ngay
        _dataTrigger.tryEmit(Unit)
    }

    private fun debugContacts() = listOf(
        ContactEntity("1", "Con gái", "0123456789"),
        ContactEntity("2", "Con trai", "0987654321"),
        ContactEntity("3", "Bác sĩ", "0112233445")
    )
}
