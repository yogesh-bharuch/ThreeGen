
package com.example.threegen.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.data.ThreeGenRepository
import com.example.threegen.data.ThreeGenDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SyncFirestoreToRoomWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val viewModel = ThreeGenViewModel.getInstance(applicationContext)
        val syncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val lastSyncTime = inputData.getLong("LAST_SYNC_TIME", 0L)
        val currentUserId = inputData.getString("CURRENT_USER_ID")
        //Log.d("FirestoreSync", "🔥 From SyncFirestoreToRoomWorker.doWork last sync time is : $lastSyncTime and currentUserId is $currentUserId")
        //val currentUserId = inputData.getString("CURRENT_USER_ID") ?: "Unknown"
        Log.d("FirestoreSync", "🔥 From SyncFirestoreToRoomWorker.doWork Worker started at: $syncTime")
        Log.d("FirestoreSync", "📅 From SyncFirestoreToRoomWorker.doWork Last Sync Time: $lastSyncTime, User ID: $currentUserId")

        return try {
            // ✅ Trigger Firestore → Room sync
            viewModel.syncFirestoreToRoom(lastSyncTime, isFirstRun = false, currentUserId = currentUserId ?: "Unknown") { message ->
                val newSyncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                Log.d("FirestoreSync", "🔥 Periodic sync completed at: $newSyncTime with message: $message")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "🔥 SyncFirestoreToRoom failed: ${e.localizedMessage}")
            Result.retry()
        }
    }
}
