package com.simple.launcher.retirement.data.repository

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simple.launcher.retirement.BuildConfig
import com.simple.launcher.retirement.domain.model.ContactEntity
import com.simple.launcher.retirement.domain.repository.ContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ContactRepositoryImpl(private val context: Context) : ContactRepository {

    private val sharedPrefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {

        private const val KEY_SELECTED_CONTACTS = "selected_contacts"
        private const val TAG = "ContactRepositoryImpl"
    }

    // LiveData tự cập nhật khi có thay đổi trong ContactsContract.
    // ContentObserver register chỉ khi có observer (onActive) và huỷ khi rời (onInactive).
    private val contactAll: MutableLiveData<List<ContactEntity>> = object : MutableLiveData<List<ContactEntity>>() {

        @Volatile
        private var reloadJob: Job? = null

        // Scope riêng — mọi query cursor chạy IO, SupervisorJob để 1 job lỗi không kéo sập cả scope.
        private val contactScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private var observerRegistered = false

        private val contactObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {

            override fun onChange(selfChange: Boolean) {

                if (BuildConfig.DEBUG) Log.d(TAG, "contact changed → reload")
                reloadAll()
            }
        }

        override fun onActive() {
            super.onActive()
            registerObserver()
            reloadAll()
        }

        override fun onInactive() {
            super.onInactive()
            reloadJob?.cancel()
            reloadJob = null
            unregisterObserver()
        }

        private fun reloadAll() {

            reloadJob?.cancel()
            reloadJob = contactScope.launch {

                postValue(queryContacts(context))
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

    // Bump khi saveSelectedContacts được gọi — trigger getSelectedContactsFlow phát lại.
    private val contactSelected = MutableStateFlow(0L)

    override fun getAllContactsFlow(): Flow<List<ContactEntity>> = contactAll.asFlow()

    override fun getSelectedContactsFlow(): Flow<List<ContactEntity>> {

        return contactSelected.map {

            readSelectedContacts()
        }.flowOn(Dispatchers.Default)
    }

    override fun saveSelectedContacts(contacts: List<ContactEntity>) {

        val json = gson.toJson(contacts)
        sharedPrefs.edit { putString(KEY_SELECTED_CONTACTS, json) }
        contactSelected.value = System.currentTimeMillis()
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
