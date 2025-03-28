package com.example.threegen.data.workers


import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.threegen.data.ThreeGenViewModel
import com.google.firebase.auth.FirebaseAuth


class SyncLocalToFirestoreWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val viewModel = ThreeGenViewModel.getInstance(applicationContext)

        return try {
            viewModel.syncLocalDataToFirestore { message ->
                Log.d("WorkManager", "🔥 SyncLocalToFirestore completed: $message")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("WorkManager", "🔥 SyncLocalToFirestore failed: ${e.localizedMessage}")
            Result.retry()
        }
    }
}
