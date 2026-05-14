package com.simple.launcher.retirement.presentation.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simple.launcher.retirement.domain.usecase.CleanStorageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileCleanupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            CleanStorageUseCase.instance()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
