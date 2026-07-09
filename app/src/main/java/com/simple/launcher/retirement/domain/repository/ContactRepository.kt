package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.data.repository.ContactRepositoryImpl
import com.simple.launcher.retirement.domain.model.ContactEntity
import kotlinx.coroutines.flow.Flow

interface ContactRepository {

    fun getAllContacts(context: android.content.Context): List<ContactEntity>
    fun getSelectedContacts(): List<ContactEntity>
    fun saveSelectedContacts(contacts: List<ContactEntity>)

    // Flow phát lại khi danh sách contact thay đổi
    fun homeDataFlow(): Flow<Unit>

    companion object {

        val instance: ContactRepository by lazy { ContactRepositoryImpl(MainApplication.instance) }
    }
}
