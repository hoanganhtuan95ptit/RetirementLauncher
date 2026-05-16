package com.simple.launcher.retirement.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
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

    companion object {
        private const val KEY_SELECTED_CONTACTS = "selected_contacts"
    }

    override fun getSelectedContacts(): List<ContactEntity> {
        val json = sharedPrefs.getString(KEY_SELECTED_CONTACTS, null)
        val contacts = if (json != null) {
            val type = object : TypeToken<List<ContactEntity>>() {}.type
            gson.fromJson<List<ContactEntity>>(json, type)
        } else {
            emptyList()
        }

        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (contacts.isEmpty() && isDebug) {
            return listOf(
                ContactEntity("1", "Con gái", "0123456789"),
                ContactEntity("2", "Con trai", "0987654321"),
                ContactEntity("3", "Bác sĩ", "0112233445")
            )
        }
        return contacts
    }

    override fun saveSelectedContacts(contacts: List<ContactEntity>) {
        val json = gson.toJson(contacts)
        sharedPrefs.edit().putString(KEY_SELECTED_CONTACTS, json).apply()
        _dataTrigger.tryEmit(Unit)
    }
}
