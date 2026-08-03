package com.simple.launcher.retirement.domain.repository

import com.simple.launcher.retirement.MainApplication
import com.simple.launcher.retirement.data.repository.ContactRepositoryImpl
import com.simple.launcher.retirement.domain.model.ContactEntity
import kotlinx.coroutines.flow.Flow

interface ContactRepository {

    fun getAllContactsFlow(): Flow<List<ContactEntity>>
    fun getSelectedContactsFlow(): Flow<List<ContactEntity>>

    fun saveSelectedContacts(contacts: List<ContactEntity>)

    companion object {

        val instance: ContactRepository by lazy { ContactRepositoryImpl(MainApplication.instance) }
    }
}
