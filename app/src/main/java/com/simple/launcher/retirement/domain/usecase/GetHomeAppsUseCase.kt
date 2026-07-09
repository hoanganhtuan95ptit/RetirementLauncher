package com.simple.launcher.retirement.domain.usecase

import android.util.Log
import com.simple.launcher.retirement.domain.model.AppEntity
import com.simple.launcher.retirement.domain.model.HomeContentEntity
import com.simple.launcher.retirement.domain.repository.AppRepository
import com.simple.launcher.retirement.domain.repository.ContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn

class GetHomeAppsUseCase(
    private val appRepository: AppRepository,
    private val contactRepository: ContactRepository
) {

    // Scope riêng cho usecase singleton để shared upstream không bị tạo lại theo từng collector.
    private val usecaseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sharedFlow: Flow<List<HomeContentEntity>> =
        merge(appRepository.homeDataFlow(), contactRepository.homeDataFlow())
            .map { invoke() }
            .flowOn(Dispatchers.IO)
            .shareIn(
                scope = usecaseScope,
                started = SharingStarted.WhileSubscribed(5_000),
                replay = 1
            )

    fun asFlow(): Flow<List<HomeContentEntity>> = sharedFlow

    operator fun invoke(): List<HomeContentEntity> {

        Log.d("tuanha", "invoke: ")

        val apps = buildHomeApps()
        val contacts = buildHomeContacts()

        return apps + contacts
    }

    private fun buildHomeApps(): List<HomeContentEntity.App> {

        val allApps = appRepository.getInstalledApps()
        val selectedPackages = appRepository.getSelectedPackages()
        val apps = createSelectedHomeApps(allApps, selectedPackages).toMutableList()

        appendCurrentAppIfMissing(apps)
        return apps
    }

    private fun createSelectedHomeApps(
        allApps: List<AppEntity>,
        selectedPackages: List<String>
    ): List<HomeContentEntity.App> {

        if (selectedPackages.isEmpty()) {
            return allApps
                .map { HomeContentEntity.App(it) }
                .sortedBy { it.entity.label.lowercase() }
        }

        return selectedPackages
            .mapNotNull { packageName -> allApps.find { it.packageName == packageName } }
            .map { HomeContentEntity.App(it) }
    }

    private fun appendCurrentAppIfMissing(apps: MutableList<HomeContentEntity.App>) {

        val currentApp = appRepository.getCurrentApp()
        val isCurrentAppShown = apps.any { it.entity.packageName == currentApp.packageName }
        if (isCurrentAppShown) return

        apps.add(HomeContentEntity.App(currentApp))
    }

    private fun buildHomeContacts(): List<HomeContentEntity.Contact> {

        return contactRepository
            .getSelectedContacts()
            .map { HomeContentEntity.Contact(it) }
    }

    companion object {

        val instance: GetHomeAppsUseCase by lazy {
            GetHomeAppsUseCase(AppRepository.instance, ContactRepository.instance)
        }
    }
}
