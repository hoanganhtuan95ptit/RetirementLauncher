package com.simple.launcher.retirement.domain.model

sealed class HomeItem {
    data class App(val entity: AppEntity) : HomeItem()
    data class Contact(val entity: ContactEntity) : HomeItem()
}
