package com.example.threegen

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.threegen.data.SyncStatus
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenRepository
import com.example.threegen.login.AuthViewModel
import com.example.threegen.login.AuthViewModelFactory
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.data.ThreeGenViewModelFactory
import com.example.threegen.ui.theme.ThreeGenTheme
import com.example.threegen.util.RequestPermissions
import com.example.threegen.util.SnackbarManager
import com.example.threegen.data.workers.WorkManagerHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // ✅ Initialize dependencies
        val dao = MainApplication.threeGenDatabase.getThreeGenDao()
        val firestore = MainApplication.instance.let { FirebaseFirestore.getInstance() }
        val auth = FirebaseAuth.getInstance()

        val viewModel: ThreeGenViewModel by viewModels { ThreeGenViewModelFactory(dao, firestore) }
        val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory(auth) }

        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        val lastSyncTime = sharedPreferences.getLong("last_sync_time", 0L)

        // ✅ Get current user ID from Firebase Authentication
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"

        // ✅ First-Time Sync Execution (Direct Repository Sync)
        if (isFirstRun) {
            Log.d("FirestoreSync", "🔥 First-time Sync Triggered")

            // ✅ Execute first-time sync directly without WorkManager
            runBlocking {
                performFirstTimeSync(applicationContext, currentUserId)
            }

            // ✅ Mark first run as complete
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }

        // ✅ Trigger chained sync jobs on app start (local → Firestore → Room)
        if (!isFirstRun && !viewModel.isSyncedInSession) {
            Log.d("MainActivityFirestoreSync", "From Mainactivity Onetime sync local->firestore->local sync jobs issued to WorkManagerHelper.chainSyncJobs")
            WorkManagerHelper.chainSyncOnStartup(context = applicationContext, lastSyncTime = lastSyncTime, currentUserId = currentUserId)
            viewModel.isSyncedInSession = true
        }

        setContent {
            ThreeGenTheme {
                val context = this@MainActivity
                val scope = rememberCoroutineScope()
                val snackbarHostState = SnackbarManager.getSnackbarHostState()
                val navController = rememberNavController()

                RequestPermissions(activity = this@MainActivity)

                // ✅ Schedule periodic Firestore-to-Room sync (every 15 min) in the background

                LaunchedEffect(true) {
                    //Log.d("MainActivityFirestoreSync", "From Mainactivity.schedulePeriodicSync sync jobs issued to WorkManagerHelper")
                    if (!isFirstRun){
                        //Log.d("FirestoreSync", "From Mainactivity.LaunchedEffect schedulePeriodicSync jobs issued to WorkManagerHelper")
                        viewModel.isSyncedInSession = false
                        if (!viewModel.isSyncedInSession) {
                            //Log.d("FirestoreSync", "From Mainactivity.LaunchedEffect schedulePeriodicSync jobs issued to WorkManagerHelper")
                            WorkManagerHelper.schedulePeriodicSync(context, timeIntervalInMinutes = 720, lastSyncTime = lastSyncTime, currentUserId = currentUserId)
                        }
                        viewModel.isSyncedInSession = true
                    }
                }

                // ✅ Observe and display sync results
                LaunchedEffect(true) {
                    WorkManagerHelper.observeSyncResult(
                        context,
                        this@MainActivity
                    ) { resultMessage ->
                        scope.launch {
                            SnackbarManager.showMessage(resultMessage)
                            Log.d("MainActivityFirestoreSync", "From Mainactivity.observeSyncResult: Scheduled Sync result: $resultMessage")
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    AppNavigation(
                        viewModel = viewModel,
                        authViewModel = authViewModel,
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(8.dp),
                        navController = navController
                    )
                }
            }
        }

/*        // ✅ First-time sync → Use WorkManager instead of direct call
        if (isFirstRun) {
            Log.d("FirestoreSync", "🔥 First-time sync triggered")

            // 🔥 Trigger full sync with WorkManager
            WorkManagerHelper.chainSyncOnStartup(
                context = applicationContext,
                lastSyncTime = 0L,      // First run → use 0L to fetch all records
                currentUserId = currentUserId
            )

            // ✅ Mark first run as complete
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }*/


        // ✅ Store current time as new sync timestamp
        val currentSyncTime = System.currentTimeMillis()
        sharedPreferences.edit().putLong("last_sync_time", currentSyncTime).apply()
    }
}

/**
 * ✅ Performs the first-time sync directly, bypassing WorkManager
 */
private suspend fun performFirstTimeSync(context: Context, currentUserId: String) {
    val dao = MainApplication.threeGenDatabase.getThreeGenDao()

    try {
        Log.d("FirestoreSync", "🔥 Performing First-Time Sync...")


        // ✅ Force Room database creation before syncing
        withContext(Dispatchers.IO) {
            Log.d("FirestoreSync", "⚙️ Forcing Room Database Creation...")

            // Dummy insert to trigger database schema creation
            val dummyMember = ThreeGen(
                firstName = "Init",
                middleName = "Init",
                lastName = "Init",
                town = "InitTown",
                shortName = "INIT",
                isAlive = true,
                childNumber = 0,
                comment = "DB Init",
                imageUri = null,
                syncStatus = SyncStatus.SYNCED,
                deleted = false,
                createdAt = System.currentTimeMillis(),
                createdBy = currentUserId,
                updatedAt = System.currentTimeMillis(),
                id = "dummy_id",
                parentID = null,
                spouseID = null
            )

            // Insert dummy record → Forces schema creation
            dao.insert(dummyMember)

            // ✅ Delete dummy record immediately
            dao.deleteThreeGen(dummyMember.id)

            Log.d("FirestoreSync", "✅ Room Database Created Successfully.")
        }

        // ✅ Execute Firestore → Room sync directly
        withContext(Dispatchers.IO) {
            ThreeGenRepository.getInstance(context).syncFirestoreToRoom(
                lastSyncTime = 0L,         // First-time full sync
                isFirstRun = true,         // First-run flag
                currentUserId = currentUserId
            ) { message ->
                Log.d("FirestoreSync", "✅ First-time Sync Completed: $message")
            }
        }

    } catch (e: Exception) {
        Log.e("FirestoreSync", "🔥 First-time Sync Failed: ${e.localizedMessage}", e)
    }
}







/*
✅ Key Changes and Explanation:
Immediate Local-to-Firestore Sync on App Start:

Added WorkManagerHelper.chainSyncJobs() inside onCreate() to trigger the sync immediately on app launch.

This ensures any local changes are pushed to Firestore first, followed by Firestore-to-Room sync.

First-Time Sync Handling:

On the first run, it performs a full Firestore-to-Room sync.

Marks isFirstRun = false in SharedPreferences after the first sync is complete.

Ensures a clean Room database is filled with Firestore data initially.

Periodic Firestore-to-Room Sync:

Added WorkManagerHelper.schedulePeriodicSync() in LaunchedEffect to ensure periodic Firestore-to-Room sync in the background.

This sync occurs every 15 minutes, even if the app is not running, ensuring the local database is always relatively fresh.

Sync Observing and Snackbar Notifications:

Added WorkManagerHelper.observeSyncResult() to observe and display sync results in a Snackbar.

Improves visibility of sync results for better debugging and tracking.

✅ Recommendations:
Error Handling and Logging:

Improve the logging mechanism by adding more detailed logs with timestamps and specific operations.

Display more detailed error messages in the Snackbar for better debugging.

Optimize Sync Frequency:

You may want to experiment with the interval for periodic sync.

If 15 minutes feels too frequent, consider increasing it to 30 or 60 minutes to reduce Firestore reads and optimize battery usage.

User-Initiated Sync:

Add a manual sync button somewhere in the app to allow the user to force a sync when needed.

✅ This implementation ensures:

Immediate local-to-Firestore sync on app start.

Full Firestore-to-Room sync during the first run.

Periodic Firestore-to-Room sync in the background.

Efficient and consistent data sync across the app. 🚀

* */