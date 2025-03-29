package com.example.threegen.data.workers
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.threegen.data.ThreeGenViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SyncWorker handles the background sync job for Firestore.
 * It is triggered by WorkManager periodically and on network availability.
 * The worker inherits from CoroutineWorker, which allows it to run asynchronous, coroutine-based operations.
 * The CoroutineWorker constructor receives:
 * context: The application context.
 * params: Configuration parameters passed by WorkManager (like input data, constraints, etc.).
 */
class SyncLocalToFirestoreWorker(
    context: Context,           // Application context
    params: WorkerParameters    // WorkManager parameters (input/output data, etc.)
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val viewModel = ThreeGenViewModel.getInstance(applicationContext)  // Get ViewModel instance
        var syncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        Log.d("FirestoreSync", "🔥 From SyncLocalToFirestoreWorker.dowork started at: $syncTime")

        return try {
            // ✅ Calls ViewModel function to sync local data to Firestore
            viewModel.syncLocalDataToFirestore()
            { message ->
                syncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                Log.d("FirestoreSync", "🔥 From SyncLocalToFirestoreWorker.dowork Sync completed at: $syncTime with message: $message")
            }
            Result.success()   // ✅ Sync successful
        } catch (e: Exception) {
            Log.e("FirestoreSync", "🔥 From SyncLocalToFirestoreWorker.dowork failed: ${e.localizedMessage}")
            Result.retry()     // 🔁 Request retry
        }
    }
}
