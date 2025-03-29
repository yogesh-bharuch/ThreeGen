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
 */
class SyncLocalToFirestoreWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val viewModel = ThreeGenViewModel.getInstance(applicationContext)
        var syncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        //Log.d("FirestoreSync", "🔥 From SyncLocalToFirestoreWorker.dowork started at: $syncTime")

        return try {
            viewModel.syncLocalDataToFirestore()
            { message ->
                syncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                Log.d("FirestoreSync", "🔥 From SyncLocalToFirestoreWorker.dowork Sync completed at: $syncTime with message: $message")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "🔥 From SyncLocalToFirestoreWorker.dowork failed: ${e.localizedMessage}")
            Result.retry()
        }
    }
}
