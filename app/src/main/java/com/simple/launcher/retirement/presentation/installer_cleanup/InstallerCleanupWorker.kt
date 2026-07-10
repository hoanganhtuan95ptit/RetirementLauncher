package com.simple.launcher.retirement.presentation.installer_cleanup

import android.content.Context
import com.simple.launcher.retirement.domain.repository.FileRepository
import com.simple.launcher.retirement.domain.repository.PreferenceRepository
import com.simple.launcher.retirement.presentation.services.worker.BackgroundWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn

class InstallerCleanupWorker(context: Context) : BackgroundWorker(context) {

    private var job: Job? = null

    override fun observeEnabled(): Flow<Boolean> = PreferenceRepository.instance.fileCleanupEnabledFlow()

    override fun onStart() {

        if (job != null) return
        job = FileRepository.instance.watchFilesFlow().launchIn(scope ?: return)
    }

    override fun onStop() {

        job?.cancel()
        job = null
    }
}
