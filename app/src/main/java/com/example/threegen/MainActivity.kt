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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // ✅ Initialize ViewModels and Firebase instances
        val dao = MainApplication.threeGenDatabase.getThreeGenDao()
        val firestore = MainApplication.instance.let {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
        }
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

        val viewModel: ThreeGenViewModel by viewModels { ThreeGenViewModelFactory(dao, firestore) }
        val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory(auth) }

        // ✅ Get shared preferences for first run and last sync time
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        val lastSyncTime = sharedPreferences.getLong("last_sync_time", 0L)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"

        Log.d("FirestoreSync", "🔥 From MainActivity: isFirstRun: $isFirstRun, lastSyncTime: $lastSyncTime")

        setContent {
            ThreeGenTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val snackbarHostState = SnackbarManager.getSnackbarHostState()
                val navController = rememberNavController()

                // ✅ Request necessary permissions
                RequestPermissions(activity = this@MainActivity)

                // ✅ Sync local → Firestore first, then Firestore → Room
                LaunchedEffect(Unit) {
                    scope.launch {
                        // ✅ Step 1: Room → Firestore sync
                        viewModel.syncLocalDataToFirestore { message ->
                            Log.d("SyncFlow", "🔥 Room → Firestore: $message")

                            // ✅ Step 2: Firestore → Room sync (only after Room → Firestore completes)
                            if (isFirstRun) {
                                viewModel.syncFirestoreToRoom(0L, isFirstRun = true, currentUserId)
                                sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
                            } else {
                                viewModel.syncFirestoreToRoom(lastSyncTime, isFirstRun = false, currentUserId)
                            }

                            // ✅ Store current sync time
                            val currentSyncTime = System.currentTimeMillis()
                            sharedPreferences.edit().putLong("last_sync_time", currentSyncTime).apply()

                            // ✅ Show sync result in Snackbar
                            SnackbarManager.showMessage("🔥 Sync completed successfully!")
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
    }
}
