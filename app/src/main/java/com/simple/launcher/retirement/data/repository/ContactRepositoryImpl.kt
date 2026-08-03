package com.simple.launcher.retirement.data.repository

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.edit
import com.google.gson.reflect.TypeToken
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.data.AppPrefs
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.domain.repository.ContactRepository
import com.simple.launcher.retirement.utils.exts.ActiveStateFlow
import com.simple.launcher.retirement.utils.exts.mutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactRepositoryImpl(private val context: Context) : ContactRepository {

    private val sharedPrefs = AppPrefs.sharedPrefs
    private val gson = AppPrefs.gson

    companion object {

        private const val KEY_SELECTED_CONTACTS = "selected_contacts"
        private const val TAG = "ContactRepositoryImpl"
    }

    // ActiveStateFlow tự cập nhật khi có thay đổi trong ContactsContract.
    // ContentObserver register chỉ khi có observer (onActive) và huỷ khi rời (onInactive).
    private val contactAll: ActiveStateFlow<List<ContactEntity>?> = object : ActiveStateFlow<List<ContactEntity>?>(null) {

        private var observerRegistered = false

        private val contactObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {

            override fun onChange(selfChange: Boolean) {

                if (BuildConfig.DEBUG) Log.d(TAG, "contact changed → reload")
                reloadAll()
            }
        }

        override suspend fun onActive() {

            withContext(Dispatchers.Main) { registerObserver() }
            reloadAll()
        }

        override suspend fun onInactive() {

            withContext(Dispatchers.Main) { unregisterObserver() }
        }

        private fun reloadAll() {

            scope.launch(Dispatchers.IO) {

                value = queryContacts(context)
            }
        }

        private fun registerObserver() {

            if (observerRegistered) return
            try {

                context.contentResolver.registerContentObserver(
                    ContactsContract.Contacts.CONTENT_URI, true, contactObserver
                )
                observerRegistered = true
            } catch (e: Exception) {

                Log.w(TAG, "Failed to register contactObserver", e)
            }
        }

        private fun unregisterObserver() {

            if (!observerRegistered) return
            try {

                context.contentResolver.unregisterContentObserver(contactObserver)
            } catch (e: Exception) {

                Log.w(TAG, "Failed to unregister contactObserver", e)
            } finally {

                observerRegistered = false
            }
        }
    }

    // Load lại danh sách đã chọn khi flow active, và update trực tiếp sau mỗi lần save.
    private val contactSelected = mutableStateFlow<List<ContactEntity>?>(null) {

        value = readSelectedContacts()
    }

    override fun getAllContactsFlow(): Flow<List<ContactEntity>> = contactAll.filterNotNull()

    override fun getSelectedContactsFlow(): Flow<List<ContactEntity>> {

        return contactSelected.filterNotNull()
    }

    override fun saveSelectedContacts(contacts: List<ContactEntity>) {

        val json = gson.toJson(contacts)
        sharedPrefs.edit { putString(KEY_SELECTED_CONTACTS, json) }
        contactSelected.value = contacts
    }

    /**
     * Query toàn bộ contact có số điện thoại từ ContactsContract.
     * De-dup theo CONTACT_ID vì 1 contact có thể có nhiều số → nhiều dòng.
     */
    private fun queryContacts(context: Context): List<ContactEntity> {

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

    /**
     * Đọc list contact đã chọn từ SharedPreferences. Chỉ dùng nội bộ.
     */
    private fun readSelectedContacts(): List<ContactEntity> {

        val data = sharedPrefs.all[KEY_SELECTED_CONTACTS] as? String ?: return emptyList()
        return try {

            val type = TypeToken.getParameterized(List::class.java, ContactEntity::class.java).type
            gson.fromJson(data, type) ?: emptyList()
        } catch (_: Exception) {

            emptyList()
        }
    }
}
