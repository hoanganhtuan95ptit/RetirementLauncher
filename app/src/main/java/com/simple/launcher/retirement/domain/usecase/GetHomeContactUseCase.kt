package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetHomeContactUseCase {

    val contactRepository by lazy {
        ContactRepository.instance
    }

    fun invoke(): Flow<List<HomeContentEntity.Contact>> = contactRepository.homeDataFlow().map {

        contactRepository.getSelectedContacts().map { HomeContentEntity.Contact(it) }
    }.flowOn(Dispatchers.IO)


    companion object {
        val instance: GetHomeContactUseCase by lazy {
            GetHomeContactUseCase()
        }
    }
}