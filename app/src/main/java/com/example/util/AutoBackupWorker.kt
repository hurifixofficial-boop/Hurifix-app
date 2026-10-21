package com.example.util

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AutoBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "AutoBackupWorker"
        private const val UNIQUE_PERIODIC_WORK_NAME = "HurifixScheduledGoogleDriveAutoBackup"
        private const val UNIQUE_ONETIME_WORK_NAME = "HurifixImmediateAutoBackup"

        fun scheduleAutoBackup(context: Context, isEnabled: Boolean, frequency: String) {
            try {
                val workManager = WorkManager.getInstance(context)
                if (!isEnabled) {
                    workManager.cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
                    return
                }
                val (interval, timeUnit) = if (frequency.equals("WEEKLY", ignoreCase = true)) {
                    Pair(7L, TimeUnit.DAYS)
                } else {
                    Pair(24L, TimeUnit.HOURS)
                }
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
                val backupRequest = PeriodicWorkRequest.Builder(AutoBackupWorker::class.java, interval, timeUnit)
                    .setConstraints(constraints)
                    .build()
                workManager.enqueueUniquePeriodicWork(
                    UNIQUE_PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    backupRequest
                )
            } catch (e: Throwable) {
                Log.w(TAG, "Failed to schedule AutoBackup: ${e.message}")
            }
        }

        fun runImmediateBackup(context: Context) {
            try {
                val workRequest = OneTimeWorkRequest.Builder(AutoBackupWorker::class.java).build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    UNIQUE_ONETIME_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            } catch (e: Throwable) {
                Log.w(TAG, "Failed to trigger immediate AutoBackup: ${e.message}")
            }
        }
    }
}
