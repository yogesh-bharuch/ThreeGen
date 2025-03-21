package com.example.threegen.util

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.threegen.data.ThreeGenViewModel
import kotlinx.coroutines.runBlocking
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
        Log.d("SyncWorker", "🔥 From SyncWorker Sync started at: $syncTime")

        return try {
            runBlocking {
                viewModel.syncLocalDataToFirestore { message ->
                    Log.d("SyncWorker", "🔥 From SyncWorker Sync completed: $message")
                }
            }
            // ✅ Return success if sync works fine
            Result.success()

        } catch (e: Exception) {
            Log.e("SyncWorker", "🔥 Sync failed: ${e.localizedMessage}", e)  // ✅ Log errors properly
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

            println("🔥 Periodic sync scheduled")
        }
    }
}



/*
package com.example.threegen.util

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.threegen.data.ThreeGenViewModel
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SyncWorker handles the background sync job for Firestore.
 * It is triggered by WorkManager and runs when the network is available.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    /**
     * Called by WorkManager to perform the sync operation.
     */
    override fun doWork(): Result {
        val viewModel = ThreeGenViewModel.getInstance(applicationContext)

        val syncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        println("🔥 SyncWorker started at: $syncTime")

        // Perform the sync using a coroutine
        runBlocking {
            viewModel.syncLocalDataToFirestore { message ->
                println("🔥 Sync completed: $message")
            }
        }

        return Result.success()
    }
}
*/