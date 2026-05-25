package com.simple.launcher.retirement.presentation.worker

import android.content.Context
import com.simple.launcher.retirement.domain.repository.FileRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn

class FileWatcherWorker(context: Context) : BackgroundWorker(context) {

    override fun observeEnabled(): Flow<Boolean> = PreferenceRepository.instance.isFileCleanupEnabledFlow()

    override fun onStart() = Unit

    override fun onStop() = Unit

    override fun attach(scope: CoroutineScope) {
        super.attach(scope)

        observeEnabled().flatMapLatest { enabled ->

            if (enabled) FileRepository.instance.watchFilesFlow() else emptyFlow()
        }.launchIn(scope)
    }
}
