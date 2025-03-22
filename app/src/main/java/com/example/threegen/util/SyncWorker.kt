package com.example.threegen.util

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.threegen.data.ThreeGenViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * SyncWorker handles the background sync job for Firestore.
 * It is triggered by WorkManager periodically and on network availability.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {  // ✅ Use CoroutineWorker for better coroutine handling

    /**
     * Perform the background sync operation.
     */
    override suspend fun doWork(): Result {
        val viewModel = ThreeGenViewModel.getInstance(applicationContext)

        val syncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        Log.d("ThreeGenSync", "🔥 From SyncWorker Sync started at: $syncTime")

        return try {
            var syncResult = "From SyncWorker: No changes synced"

            // ✅ Use coroutine for proper async handling
            withContext(Dispatchers.IO) {
                viewModel.syncLocalDataToFirestore { message ->
                    syncResult = message
                    Log.d("ThreeGenSync", "🔥 From SyncWorker Sync completed: $message")
                }
            }
            // ✅ Log the success result
            Log.d("ThreeGenSync", "🔥 From SyncWorkerSync success: $syncResult")

            // ✅ Return success with the sync result
            Result.success(
                workDataOf("SYNC_RESULT" to syncResult)
            )
            // ✅ Return success with sync result data
            //val outputData = Data.Builder()
            //    .putString("SYNC_RESULT", syncResult)
            //    .build()

            //Result.success(outputData)

        } catch (e: Exception) {
            Log.e("ThreeGenSync", "🔥 From SyncWorker Sync failed: ${e.localizedMessage}", e)  // ✅ Log errors properly
            Result.retry()
        }
    }

    companion object {
        private const val SYNC_WORK_TAG = "SYNC_WORK"

        /**
         * Schedule periodic sync job every 15 minutes with network constraints.
         */
        fun schedulePeriodicSync(context: Context) {
            val workManager = WorkManager.getInstance(context)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)  // ✅ Trigger only when network is available
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES  // ✅ Sync every 15 minutes
            )
                .setConstraints(constraints)
                .addTag(SYNC_WORK_TAG)  // ✅ Tag to identify the job
                .build()

            workManager.enqueueUniquePeriodicWork(
                SYNC_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,  // ✅ Prevent duplicate jobs
                periodicSyncRequest
            )

            Log.d("ThreeGenSync", "🔥 Periodic sync scheduled")
        }
    }
}
