package com.example.threegen.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.threegen.data.ThreeGenViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SyncFirestoreToRoomWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val viewModel = ThreeGenViewModel.getInstance(applicationContext)
        var syncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val lastSyncTime = inputData.getLong("LAST_SYNC_TIME", 0L)
        val currentUserId = inputData.getString("CURRENT_USER_ID")//FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"
        //Log.d("FirestoreSync", "🔥 From SyncFirestoreToRoomWorker.dowork started at: $syncTime")
        return try {
                viewModel.syncFirestoreToRoom(lastSyncTime, isFirstRun = false, currentUserId = "dummy in the viewmodel fetched")
                { message ->
                    syncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    Log.d("FirestoreSync", "🔥 From SyncFirestoreToRoomWorker.dowork Sync completed at: $syncTime with message: $message")
                }
                Result.success()
        } catch (e: Exception) {
            Log.e("WorkManager", "🔥 SyncFirestoreToRoom failed: ${e.localizedMessage}")
            Result.retry()
        }
    }
}

