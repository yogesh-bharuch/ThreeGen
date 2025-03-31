package com.example.threegen.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.threegen.data.ThreeGenDatabase
import com.example.threegen.data.ThreeGenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Worker for syncing Firestore data to the local Room database periodically.
 *
 * Steps:
 * 1. Retrieves the last sync timestamp and current user ID from WorkManager input.
 * 2. Triggers Firestore → Room sync using the `ThreeGenRepository`.
 * 3. Logs the sync process and handles success or failure.
 */
class SyncFirestoreToRoomWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // ✅ Initialize the repository directly
    private val repository: ThreeGenRepository by lazy { ThreeGenRepository.getInstance(context) }
    // ✅ Read sync parameters from SharedPreferences
    private fun getSyncParams(context: Context): Pair<Long, String> {
        val sharedPreferences = context.getSharedPreferences("SyncPrefs", Context.MODE_PRIVATE)
        val lastSyncTime = sharedPreferences.getLong("LAST_SYNC_TIME", 0L)
        val currentUserId = sharedPreferences.getString("CURRENT_USER_ID", "Unknown") ?: "Unknown"
        return Pair(lastSyncTime, currentUserId)
    }

    override suspend fun doWork(): Result {
        val syncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        // ✅ Read the sync parameters from SharedPreferences
        val (lastSyncTime, currentUserId) = getSyncParams(applicationContext)

        Log.d("SyncFirestoreToRoomWorker", "📅 From SyncFirestoreToRoomWorker: Firestore-> local sync Started at : $syncTime, LastSync Time: $lastSyncTime, User ID: $currentUserId")
        // 📅 From SyncLocalToFirestoreWorker: Local-> firestore sync Started at : $syncTime, LastSync Time: $lastSyncTime, User ID: $currentUserId
        var resultMmessage = "?..."
        return try {
            // ✅ Perform the Firestore → Room sync
            withContext(Dispatchers.IO) {
                repository.syncFirestoreToRoom(lastSyncTime = lastSyncTime, isFirstRun = false, currentUserId = currentUserId) { message ->
                    resultMmessage = message
                    val newSyncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    Log.d("FirestoreSync", "✅ From SyncFirestoreToRoomWorker.dowork : Periodic Sync completed for Time: $lastSyncTime, User ID: $currentUserId \n at: $newSyncTime → $message")
                }
            }

            // ✅ Output sync completion message
            val outputData = workDataOf(
                "SYNC_RESULT" to "🔥 From SyncFirestoreToRoomWorker.dowork : Firestore → Room sync completed successfully /n at $syncTime : result: $resultMmessage"
            )

            Result.success(outputData)

        } catch (e: Exception) {
            Log.e("FirestoreSync", "🔥 From SyncFirestoreToRoomWorker.dowork : SyncFirestoreToRoomWorker → Sync failed: ${e.localizedMessage}", e)

            // ✅ Return failure and retry on error
            val outputData = workDataOf(
                "SYNC_RESULT" to "❌ From SyncFirestoreToRoomWorker.dowork : Firestore → Room sync failed: ${e.localizedMessage}"
            )

            Result.retry()  // 🔁 Request retry on failure
        }
    }
}
