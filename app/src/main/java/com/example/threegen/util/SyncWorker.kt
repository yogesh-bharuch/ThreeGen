package com.example.threegen.util

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.threegen.data.ThreeGenViewModel
import kotlinx.coroutines.CompletableDeferred
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
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val viewModel = ThreeGenViewModel.getInstance(applicationContext)

        val syncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        Log.d("ThreeGenSync", "🔥 From SyncWorker Sync started at: $syncTime")

        return try {
            val syncResult = CompletableDeferred<String>()

            // ✅ Run sync in IO Dispatcher
            withContext(Dispatchers.IO) {
                viewModel.syncLocalDataToFirestore { message ->
                    Log.d("ThreeGenSync", "🔥 From SyncWorker Sync completed: $message")
                    syncResult.complete(message)  // 🔥 Complete with the result message
                }
            }

            // ✅ Wait for the sync result before proceeding
            val resultMessage = syncResult.await()

            Log.d("ThreeGenSync", "🔥 From SyncWorker Sync success: $resultMessage")

            // ✅ Return success with the correct result message
            Result.success(workDataOf("SYNC_RESULT" to resultMessage))

        } catch (e: Exception) {
            Log.e("ThreeGenSync", "🔥 From SyncWorker Sync failed: ${e.localizedMessage}", e)
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
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(SYNC_WORK_TAG)
                .build()

            workManager.enqueueUniquePeriodicWork(
                SYNC_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
            )

            Log.d("ThreeGenSync", "🔥 Periodic sync scheduled")
        }
    }
}
