package com.simple.launcher.retirement.domain.usecase

import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetHomeContactUseCase {

    fun invoke(): Flow<List<HomeContentEntity.Contact>> = ContactRepository.instance
        .getSelectedContactsFlow()
        .map { contacts -> contacts.map { HomeContentEntity.Contact(it) } }
        .flowOn(Dispatchers.IO)

    companion object {

        val instance: GetHomeContactUseCase by lazy {

            GetHomeContactUseCase()
        }
    }
}
