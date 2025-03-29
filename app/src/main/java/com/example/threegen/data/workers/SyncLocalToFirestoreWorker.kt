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
    private val currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"

    // ✅ Access the repository directly
    private val repository = ThreeGenRepository.getInstance(context)

    override suspend fun doWork(): Result {
        val syncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        Log.d("FirestoreSync", "🔥 From SyncLocalToFirestoreWorker - Started at: $syncTime")

        return try {
            // ✅ Use callback to handle the sync result properly
            var resultMessage = "Sync started..."

            val callback: (String) -> Unit = { result ->
                resultMessage = result
                Log.d("FirestoreSync", "🔥 Sync result: $resultMessage")
            }

            // ✅ Trigger the sync with the callback
            repository.syncLocalDataToFirestore(callback)

            // ✅ Prepare WorkManager result data
            val outputData = workDataOf("SYNC_RESULT" to resultMessage)

            Log.d("FirestoreSync", "🔥 Sync completed: $resultMessage")

            Result.success(outputData)   // ✅ Return success with output data

        } catch (e: Exception) {
            Log.e("FirestoreSync", "🔥 Sync failed: ${e.localizedMessage}")

            // ✅ Return failure with error message
            val outputData = workDataOf("SYNC_RESULT" to "🔥 Sync failed: ${e.localizedMessage}")

            Result.retry()  // 🔁 Request retry
        }
    }
}
