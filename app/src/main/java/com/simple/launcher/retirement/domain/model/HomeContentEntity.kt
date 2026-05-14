package com.simple.launcher.retirement.domain.model

sealed class HomeContentEntity {
    data class App(val entity: AppEntity) : HomeContentEntity()
    data class Contact(val entity: ContactEntity) : HomeContentEntity()
}
