package com.example.threegen.util

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * WorkManagerHelper manages scheduling and executing sync jobs.
 */
object WorkManagerHelper {

    /**
     * ✅ Schedules an immediate one-time sync with network constraints and retry strategy.
     */
    fun scheduleImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)  // Only sync when connected
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(                             // ✅ Retry on failure
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .addTag("ImmediateSync")                         // 🔥 Unique tag for one-time sync
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(                             // ✅ Prevent duplicate immediate syncs
                "ImmediateSync",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
    }

    /**
     * ✅ Schedules periodic background sync with network constraints and retry strategy.
     * Period: Every 6 hours.
     */
    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)   // Sync only when online
            .setRequiresBatteryNotLow(true)                  // Avoid low-battery sync
            .setRequiresCharging(false)                      // Sync even if not charging
            .build()

        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            6, TimeUnit.HOURS                                // ⏱️ Repeat every 6 hours
        )
            .setConstraints(constraints)
            .setBackoffCriteria(                             // ✅ Retry on failure
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .addTag("PeriodicSync")                          // 🔥 Unique tag for periodic sync
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(                      // ✅ Prevent duplicate periodic syncs
                "PeriodicSync",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
            )
    }
}
