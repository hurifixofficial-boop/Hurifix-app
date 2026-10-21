package com.example.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.firebase.FirestoreSyncManager
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val syncManager = FirestoreSyncManager.getInstance(applicationContext)
            syncManager.syncNow()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "HurifixPeriodicCloudSync"

        fun schedulePeriodicSync(context: Context) {
            try {
                val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                    15, TimeUnit.MINUTES
                ).build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )
            } catch (_: Throwable) {
                // Safely handles test environments (like Robolectric) where WorkManager is mocked or not initialized
            }
        }
    }
}
