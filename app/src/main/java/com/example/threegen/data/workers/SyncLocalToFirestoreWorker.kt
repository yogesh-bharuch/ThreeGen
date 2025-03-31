package com.example.threegen.data.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.threegen.data.ThreeGenRepository
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ✅ SyncLocalToFirestoreWorker
 * - Handles the background sync job for Local → Firestore.
 * - Triggered by WorkManager periodically or immediately when the user modifies data.
 */
class SyncLocalToFirestoreWorker(
    context: Context,           // Application context
    params: WorkerParameters    // WorkManager parameters (input/output data, etc.)
) : CoroutineWorker(context, params) {


    // ✅ Get current Firebase user ID
    //private val currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"

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
        Log.d("SyncLocalToFirestoreWorker", "\uD83D\uDCC5 From SyncLocalToFirestoreWorker: Local-> firestore sync Started at : $syncTime, LastSync Time: $lastSyncTime, User ID: $currentUserId")

        return try {
            // ✅ Use callback to handle the sync result properly
            var resultMessage = "From SyncLocalToFirestoreWorker: Sync started..."

            val callback: (String) -> Unit = { result ->
                resultMessage = result
                //Log.d("FirestoreSync", "🔥 From SyncLocalToFirestoreWorker.dowork: Sync result: $resultMessage")
            }

            // ✅ Trigger the sync with the callback
            repository.syncLocalDataToFirestore(callback)

            // ✅ Prepare WorkManager result data
            val outputData = workDataOf("SYNC_RESULT" to resultMessage)

            Log.d("FirestoreSync", "🔥 From SyncLocalToFirestoreWorker.dowork: Room → Firestore Sync completed: $resultMessage")

            Result.success(outputData)   // ✅ Return success with output data

        } catch (e: Exception) {
            Log.e("FirestoreSync", "🔥 From SyncLocalToFirestoreWorker: Sync failed: ${e.localizedMessage}")

            // ✅ Return failure with error message
            val outputData = workDataOf("SYNC_RESULT" to "🔥 From SyncLocalToFirestoreWorker.dowork: Room → Firestore Sync failed: ${e.localizedMessage}")

            Result.retry()  // 🔁 Request retry
        }
    }
}
