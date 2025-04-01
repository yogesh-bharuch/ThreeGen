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
import java.text.DateFormat
import java.text.DateFormat.getDateTimeInstance
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
class FirstTimeSyncFirestoreToRoomWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // ✅ Initialize the repository directly
    private val repository: ThreeGenRepository by lazy { ThreeGenRepository.getInstance(context) }
    // ✅ Read sync parameters from SharedPreferences
    /*private fun getSyncParams(context: Context): Pair<Long, String> {
        val sharedPreferences = context.getSharedPreferences("SyncPrefs", Context.MODE_PRIVATE)
        val lastSyncTime = sharedPreferences.getLong("LAST_SYNC_TIME", 0L)
        val currentUserId = sharedPreferences.getString("CURRENT_USER_ID", "Unknown") ?: "Unknown"
        return Pair(lastSyncTime, currentUserId)
    }*/

    override suspend fun doWork(): Result {
        val syncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        // ✅ Read the sync parameters from SharedPreferences
        //val (lastSyncTime, currentUserId) = getSyncParams(applicationContext)

        Log.d("FirestoreSync", "📅 From FirstTimeSyncFirestoreToRoomWorker: Firestore-> local sync Started at : $syncTime")
        // 📅 From SyncLocalToFirestoreWorker: Local-> firestore sync Started at : $syncTime, LastSync Time: $lastSyncTime, User ID: $currentUserId
        var resultMmessage = "?..."
        return try {
            // ✅ Perform the Firestore → Room sync
            withContext(Dispatchers.IO) {
                repository.syncFirestoreToRoom(lastSyncTime = 0L, isFirstRun = true, currentUserId = "FirstTime") { message ->
                    resultMmessage = message
                    val newSyncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    //val date = Date(lastSyncTime) // Convert timestamp to Date object
                    //val formattedTime = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(date) //SimpleDateFormat("HH:mm:ss").format(date)

                    Log.d("FirestoreSync", "✅ From FirstTimeSyncFirestoreToRoomWorker.dowork : First Time Sync completed for Time: $newSyncTime → $message")
                }
            }

            // ✅ Output sync completion message
            val outputData = workDataOf(
                "SYNC_RESULT" to "🔥 From FirstTimeSyncFirestoreToRoomWorker.dowork : Firestore → Room sync completed successfully result: $resultMmessage"
            )

            Result.success(outputData)

        } catch (e: Exception) {
            Log.e("FirstTimeSyncFirestoreToRoomWorker", "🔥 From FirstTimeSyncFirestoreToRoomWorker.dowork : SyncFirestoreToRoomWorker → Sync failed: ${e.localizedMessage}", e)

            // ✅ Return failure and retry on error
            val outputData = workDataOf(
                "SYNC_RESULT" to "❌ From FirstTimeSyncFirestoreToRoomWorker.dowork : Firestore → Room sync failed: ${e.localizedMessage}"
            )

            Result.retry()  // 🔁 Request retry on failure
        }
    }
}
