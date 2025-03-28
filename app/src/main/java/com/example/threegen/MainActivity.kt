package com.example.threegen

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.threegen.login.AuthViewModel
import com.example.threegen.login.AuthViewModelFactory
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.data.ThreeGenViewModelFactory
import com.example.threegen.ui.theme.ThreeGenTheme
import com.example.threegen.util.RequestPermissions
import com.example.threegen.util.SnackbarManager
import com.example.threegen.util.WorkManagerHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

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

        // ✅ Trigger chained sync jobs on app start (local → Firestore → Room)
        if (!isFirstRun && !viewModel.isSyncedInSession) {
            Log.d("FirestoreSync", "From Mainactivity.chainSyncJobs Triggering chained sync jobs")
            WorkManagerHelper.chainSyncJobs(context = applicationContext, lastSyncTime = lastSyncTime, currentUserId = currentUserId)
            viewModel.isSyncedInSession = true
        }

        setContent {
            ThreeGenTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val snackbarHostState = SnackbarManager.getSnackbarHostState()
                val navController = rememberNavController()

                RequestPermissions(activity = this@MainActivity)

                // ✅ Schedule periodic Firestore-to-Room sync (every 15 min) in the background
                LaunchedEffect(true) {
                    Log.d("FirestoreSync", "From Mainactivity.schedulePeriodicSync sync jobs")
                    //WorkManagerHelper.schedulePeriodicSync(context, timeIntervalInMinute = 180)
                }

                // ✅ Observe and display sync results
                LaunchedEffect(true) {
                    WorkManagerHelper.observeSyncResult(
                        context,
                        this@MainActivity
                    ) { resultMessage ->
                        scope.launch {
                            SnackbarManager.showMessage(resultMessage)
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

        // ✅ First-time sync: Firestore → Room
        if (isFirstRun) {
            viewModel.syncFirestoreToRoom(
                lastSyncTime = 0L,
                isFirstRun = true,
                currentUserId = currentUserId
            ) { message ->
                Log.d("FirestoreSync", "🔥 First-time sync completed: $message")
            }

            // ✅ Mark first run as complete
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }

        // ✅ Store current time as new sync timestamp
        val currentSyncTime = System.currentTimeMillis()
        sharedPreferences.edit().putLong("last_sync_time", currentSyncTime).apply()
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