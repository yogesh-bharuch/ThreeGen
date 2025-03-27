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
import kotlinx.coroutines.launch

//@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val dao = MainApplication.threeGenDatabase.getThreeGenDao()
        val firestore =
            MainApplication.instance.let { com.google.firebase.firestore.FirebaseFirestore.getInstance() }
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

        val viewModel: ThreeGenViewModel by viewModels { ThreeGenViewModelFactory(dao, firestore) }
        val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory(auth) }

        // ✅ Schedule periodic sync on network availability
        WorkManagerHelper.schedulePeriodicSync(applicationContext)
        Log.d("Sync", "🔥 From MainActivity Periodic Sync Scheduled")

        setContent {
            ThreeGenTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val snackbarHostState = SnackbarManager.getSnackbarHostState()

                val navController = rememberNavController()

                RequestPermissions(activity = this@MainActivity)

                // ✅ Schedule periodic background sync
                // WorkManagerHelper.schedulePeriodicSync(applicationContext)

                // ✅ Trigger immediate sync on app start
                WorkManagerHelper.scheduleImmediateSync(applicationContext)

                // ✅ Observe sync result using the work ID // syncRequest.id
                LaunchedEffect(Unit) {
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
        // ✅ Trigger Firestore sync on fresh install
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        //sharedPreferences.edit().putBoolean("isFirstRun", true).apply()
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        val lastSyncTime = sharedPreferences.getLong("last_sync_time", 0L)
        // 🔥 Get current user ID from Firebase Authentication
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"
        Log.d(
            "FirestoreSync",
            "🔥 From MainActivity isFirstRun: $isFirstRun, lastSyncTime: $lastSyncTime"
        )

        if (isFirstRun) {
            // ✅ First-time sync → Clear local DB
            viewModel.syncFirestoreToRoom(0L, isFirstRun = true, currentUserId)

            // ✅ Mark first run as complete
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        } else {
            // ✅ Normal sync → Only update modified members
            viewModel.syncFirestoreToRoom(lastSyncTime, isFirstRun = false, currentUserId)
        }

        // ✅ Store the current time as the new sync timestamp
        val currentSyncTime = System.currentTimeMillis()
        sharedPreferences.edit().putLong("last_sync_time", currentSyncTime).apply()
    }
}
