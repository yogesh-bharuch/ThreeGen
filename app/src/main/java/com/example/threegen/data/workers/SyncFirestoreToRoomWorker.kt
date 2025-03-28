package com.example.threegen.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.threegen.data.ThreeGenViewModel
import com.google.firebase.auth.FirebaseAuth


class SyncFirestoreToRoomWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val viewModel = ThreeGenViewModel.getInstance(applicationContext)

        return try {
            val lastSyncTime = inputData.getLong("LAST_SYNC_TIME", 0L)
            val currentUserId = inputData.getString("CURRENT_USER_ID")//FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"

            if (currentUserId != null) {
                viewModel.syncFirestoreToRoom(lastSyncTime, isFirstRun = false, currentUserId)
            }
            Log.d("WorkManager", "🔥 SyncFirestoreToRoom completed")
            Result.success()
        } catch (e: Exception) {
            Log.e("WorkManager", "🔥 SyncFirestoreToRoom failed: ${e.localizedMessage}")
            Result.retry()
        }
    }
}

