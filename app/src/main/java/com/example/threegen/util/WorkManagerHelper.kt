package com.example.threegen.util

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.threegen.data.workers.SyncFirestoreToRoomWorker
import com.example.threegen.data.workers.SyncLocalToFirestoreWorker
import java.util.concurrent.TimeUnit

/**
 * ✅ WorkManagerHelper manages scheduling and executing sync jobs.
 * - Chained Sync: Ensures local → Firestore sync runs before Firestore → Room sync.
 * - Immediate Sync: Runs a one-time sync with exponential backoff.
 * - Periodic Sync: Runs periodically in the background (even when the app is closed).
 * - Observation: Monitors the sync status using LiveData and logs the result.
 * - Tags and Constraints: Adds tags for easy tracking and constraints for reliability.
 */
object WorkManagerHelper {

    /**
     * ✅ Store lastSyncTime and currentUserId in SharedPreferences.
     */
    private fun saveSyncParams(context: Context, lastSyncTime: Long, currentUserId: String) {
        val sharedPreferences = context.getSharedPreferences("SyncPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putLong("LAST_SYNC_TIME", lastSyncTime)
            putString("CURRENT_USER_ID", currentUserId)
            apply()
        }
        //Log.d("FirestoreSync", "✅ From WorkManagerHelper: Saved Sync Params → lastSyncTime: $lastSyncTime, currentUserId: $currentUserId")
    }

    /**
     * ✅ Chain Sync for App Startup:
     * 1. Local → Firestore
     * 2. Firestore → Room (only after local → Firestore is complete)
     */
    fun chainSyncOnStartup(context: Context, lastSyncTime: Long, currentUserId: String) {
        val workManager = WorkManager.getInstance(context)
        // ✅ Save sync parameters to SharedPreferences
        saveSyncParams(context, lastSyncTime, currentUserId)

        // 🔥 Step 1: Local → Firestore Sync
        val syncLocalToFirestoreWork = OneTimeWorkRequestBuilder<SyncLocalToFirestoreWorker>()
            .addTag("SyncLocalToFirestore")   // Add tag for easy tracking
            .build()

        // 🔥 Step 2: Firestore → Room Sync
        val syncFirestoreToRoomWork = OneTimeWorkRequestBuilder<SyncFirestoreToRoomWorker>()
            .addTag("SyncFirestoreToRoom")        // Add tag for tracking
            .build()

        // ✅ Chain Execution
        workManager.beginWith(syncLocalToFirestoreWork)   // First: Local → Firestore
            .then(syncFirestoreToRoomWork)                // Then: Firestore → Room
            .enqueue()                                    // Enqueue the chain

        Log.d("WorkManagerHelper", "🔥 From WorkManagerHelper.chainSyncOnStartup: (One time) Chained sync jobs enqueued on app startup \n  Sync Params: lastSyncTime: $lastSyncTime, currentUserId: $currentUserId \n  With lastSyncTime= $lastSyncTime, currentUserId= $currentUserId")
    }

    /**
     * ✅ Immediate One-Time Sync: Local → Firestore
     * - Triggered when user modifies data
     */
    fun immediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)    // Only sync with network
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncLocalToFirestoreWorker>()  // Correct Worker!
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,                   // Retry with exponential backoff
                30, TimeUnit.SECONDS                         // Retry delay of 30 seconds
            )
            .addTag("ImmediateSync")                         // Add tag for tracking
            .build()

        val workManager = WorkManager.getInstance(context)

        // ✅ Enqueue unique work to avoid duplication
        workManager.enqueueUniqueWork(
            "ImmediateSync",
            ExistingWorkPolicy.REPLACE,      // Replace existing immediate sync if it exists
            syncRequest
        )
        Log.d("WorkManagerHelper", "🔥 From WorkManagerHelper.immediateSync: Immediate sync issued local->firestore")
    }

    /**
     * ✅ Periodic Sync: Runs in the background (even if the app is closed)
     * - Syncs Local → Firestore → Room in a chain.
     */
    fun schedulePeriodicSync(
        context: Context,
        timeIntervalInMinutes: Long = 720,
        lastSyncTime: Long,
        currentUserId: String
    ) {
        val workManager = WorkManager.getInstance(context)
        // ✅ Store the sync parameters in SharedPreferences
        saveSyncParams(context, lastSyncTime, currentUserId)

        // ✅ Local → Firestore (first in chain)
        val localToFirestoreWork = PeriodicWorkRequestBuilder<SyncLocalToFirestoreWorker>(timeIntervalInMinutes, TimeUnit.MINUTES)
            .addTag("PeriodicLocalToFirestore")
            .build()

        // ✅ Firestore → Room (second in chain)
        val firestoreToRoomWork = PeriodicWorkRequestBuilder<SyncFirestoreToRoomWorker>(timeIntervalInMinutes, TimeUnit.MINUTES)
           // .setInputData(inputData)                   // Pass sync time and user ID
            .addTag("PeriodicFirestoreToRoom")
            .build()

        // 🔥 Schedule the workers separately (no chaining allowed with periodic work)
        workManager.enqueueUniquePeriodicWork(
            "PeriodicLocalToFirestore",
            ExistingPeriodicWorkPolicy.KEEP,      // Prevent duplication
            localToFirestoreWork
        )

        workManager.enqueueUniquePeriodicWork(
            "PeriodicFirestoreToRoom",
            ExistingPeriodicWorkPolicy.KEEP,      // Prevent duplication
            firestoreToRoomWork
        )

        Log.d("WorkManagerHelper", "🔥 From WorkManagerHelper.schedulePeriodicSync : chain scheduled every $timeIntervalInMinutes minutes")
    }

    /**
     * ✅ Observes the sync result and logs it.
     * - Monitors both Immediate and Periodic Sync results
     */
    fun observeSyncResult(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        onResult: (String) -> Unit   // Callback for sync result
    ) {
        val workManager = WorkManager.getInstance(context)

        // ✅ Observe immediate sync results
        workManager.getWorkInfosByTagLiveData("ImmediateSync")
            .observe(lifecycleOwner) { workInfos ->
                for (workInfo in workInfos) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val resultMessage = workInfo.outputData.getString("SYNC_RESULT")
                            ?: "Immediate Sync completed"
                        Log.d("FirestoreSync", "🔥 From workManager.getWorkInfosByTagLiveData: Immediate Sync Result: $resultMessage")
                        onResult(resultMessage)

                        // ✅ Clear completed work after logging the message
                        workManager.pruneWork()    // Removes completed/cancelled work from WorkManager DB
                    }
                }
            }

        // ✅ Observe periodic sync results
        workManager.getWorkInfosByTagLiveData("PeriodicLocalToFirestore")
            .observe(lifecycleOwner) { workInfos ->
                for (workInfo in workInfos) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val resultMessage = workInfo.outputData.getString("SYNC_RESULT")
                            ?: "Periodic Sync Local → Firestore completed"
                        Log.d("FirestoreSync", "🔥 Periodic Local → Firestore Sync Result: $resultMessage")
                        onResult(resultMessage)

                        // ✅ Clear completed work
                        workManager.pruneWork()
                    }
                }
            }

        workManager.getWorkInfosByTagLiveData("PeriodicFirestoreToRoom")
            .observe(lifecycleOwner) { workInfos ->
                for (workInfo in workInfos) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val resultMessage = workInfo.outputData.getString("SYNC_RESULT")
                            ?: "Periodic Sync Firestore → Room completed"
                        Log.d("FirestoreSync", "🔥 From workManager.getWorkInfosByTagLiveData: Periodic Firestore → Room Sync Result: $resultMessage")
                        onResult(resultMessage)
                        // ✅ Clear completed work
                        workManager.pruneWork()
                    }
                }
            }
    }
}
