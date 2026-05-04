package com.simple.launcher.retirement.domain.model

sealed class HomeItem {
    object Clock : HomeItem()
    data class App(val entity: AppEntity) : HomeItem()
    data class Contact(val entity: ContactEntity) : HomeItem()
    data class CleanFiles(val fileCount: Int) : HomeItem()
    data class CleanMemory(val memoryMB: Long) : HomeItem()
}
