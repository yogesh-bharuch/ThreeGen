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
 * WorkManagerHelper manages scheduling and executing sync jobs.
 */
object WorkManagerHelper {

        fun chainSyncJobs(context: Context, lastSyncTime: Long, currentUserId: String) {
            val workManager = WorkManager.getInstance(context)
            val inputData = workDataOf("LAST_SYNC_TIME" to lastSyncTime, "CURRENT_USER_ID" to currentUserId)
            //Log.d("FirestoreSync", "🔥 From WorkManagerHelper.chainSyncJobs started")
            val syncLocalToFirestoreWork = OneTimeWorkRequestBuilder<SyncLocalToFirestoreWorker>()
                .addTag("SyncLocalToFirestore")
                .build()

            val syncFirestoreToRoomWork = OneTimeWorkRequestBuilder<SyncFirestoreToRoomWorker>()
                .setInputData(inputData)
                .addTag("SyncFirestoreToRoom")
                .build()

            workManager.beginWith(syncLocalToFirestoreWork)
                .then(syncFirestoreToRoomWork)
                .enqueue()

            //Log.d("FirestoreSync", "🔥 Chained sync jobs enqueued")
        }

    /**
     * ✅ Schedules an immediate one-time sync and observes the result.
     */// : WorkRequest
    fun scheduleImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .addTag("ImmediateSync")
            .build()

        val workManager = WorkManager.getInstance(context)

        // ✅ Enqueue the sync worker
        workManager.enqueueUniqueWork(
            "ImmediateSync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
        Log.d("ThreeGenSync", "🔥 From WorkManagerHelper Immediate sync scheduled")

        //return syncRequest
    }

    /**
     * ✅ Observes the sync result and logs it.
     */ //workId: UUID
    fun observeSyncResult(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        onResult: (String) -> Unit
    ) {
        val workManager = WorkManager.getInstance(context)

        // ✅ Get LiveData<WorkInfo?>
        //val workInfoLiveData: LiveData<WorkInfo?> = workManager.getWorkInfoByIdLiveData(workId)

        //fun observeSyncResult(context: Context, lifecycleOwner: LifecycleOwner, onResult: (String) -> Unit) {
        //    val workManager = WorkManager.getInstance(context)

        // ✅ Observe manual sync results
        workManager.getWorkInfosByTagLiveData("ImmediateSync")
            .observe(lifecycleOwner) { workInfos ->
                for (workInfo in workInfos) {
                    if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                        val resultMessage =
                            workInfo.outputData.getString("SYNC_RESULT") ?: "Sync completed"
                        Log.d("ThreeGenSync", "🔥 From WorkManagerHelper Manual Sync Result: $resultMessage")
                        onResult(resultMessage)
                    }
                }
            }
        // ✅ Observe periodic sync results
        workManager.getWorkInfosByTagLiveData("PeriodicSync").observe(lifecycleOwner) { workInfos ->
            for (workInfo in workInfos) {
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    val resultMessage =
                        workInfo.outputData.getString("SYNC_RESULT") ?: "Periodic sync completed"
                    Log.d(
                        "ThreeGenSync",
                        "🔥 From WorkManagerHelper Periodic Sync Result: $resultMessage"
                    )
                    onResult(resultMessage)
                }
            }
        }
        //}
    }

    /**
     * ✅ Schedules periodic sync (every 15 minutes) and returns the WorkRequest.
     */ //: WorkRequest
    fun schedulePeriodicSync(
        context: Context,
        timeIntervalInMinute: Long = 60,
        lastSyncTime: Long = 0L,         // Add last sync time
        currentUserId: String = "Unknown" // Add current user ID
    ) {
        Log.d("FirestoreSync", "🔥 From WorkManagerHelper.PeriodicSync start")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(false)
            .build()
        val inputData = workDataOf(
            "LAST_SYNC_TIME" to lastSyncTime,
            "CURRENT_USER_ID" to currentUserId
        )

        //val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncFirestoreToRoomWorker>(
            timeIntervalInMinute, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInputData(inputData)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .addTag("PeriodicSync")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PeriodicSync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
        Log.d("FirestoreSync", "🔥 From WorkManagerHelper.Periodicsync : scheduled successfully at every $timeIntervalInMinute minutes")

        // ✅ Return the WorkRequest for observation
        //return periodicSyncRequest
    }
}
