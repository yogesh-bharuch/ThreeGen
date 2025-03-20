package com.example.threegen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.threegen.login.AuthViewModel
import com.example.threegen.login.AuthViewModelFactory
import com.example.threegen.data.NewThreeGenViewModel
import com.example.threegen.data.NewThreeGenViewModelFactory
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.data.ThreeGenViewModelFactory
import com.example.threegen.ui.theme.ThreeGenTheme
import com.example.threegen.util.RequestPermissions
import com.example.threegen.util.SnackbarManager

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensures the app content extends to the system bars for better UI appearance
        enableEdgeToEdge()

        // Initialize DAO and Firestore
        val dao = MainApplication.threeGenDatabase.getThreeGenDao()
        val firestore = MainApplication.instance.let { com.google.firebase.firestore.FirebaseFirestore.getInstance() }
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

        // Initialize ViewModels using custom factories
        val viewModel: ThreeGenViewModel by viewModels { ThreeGenViewModelFactory(dao, firestore) }
        val viewModelNew: NewThreeGenViewModel by viewModels { NewThreeGenViewModelFactory(dao, firestore) }
        val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory(auth) }

        // Set up the Jetpack Compose UI
        setContent {
            ThreeGenTheme { // Apply the app's theme

                // Initialize Jetpack Navigation Controller
                val navController = rememberNavController()

                // ✅ Request necessary permissions for accessing storage, camera, etc.
                RequestPermissions(activity = this@MainActivity)

                // Scaffold provides a layout structure with top bars, bottom bars, etc.
                Scaffold(
                    modifier = Modifier.fillMaxSize(),

                    // ✅ Include global SnackbarHost
                    snackbarHost = {
                        SnackbarHost(SnackbarManager.getSnackbarHostState())
                    }
                ) { innerPadding ->

                    // Load the main navigation graph of the app
                    AppNavigation(
                        navController = navController,
                        viewModel = viewModel,
                        viewModelNew = viewModelNew,
                        authViewModel = authViewModel,
                        modifier = Modifier
                            .padding(innerPadding)  // Adjust padding for system bars
                            .padding(8.dp)          // Add additional padding for UI spacing
                    )
                }
            }
        }
    }
}












/*
package com.example.threegen

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.threegen.data.NewThreeGenViewModel
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.ui.theme.ThreeGenTheme
import com.example.threegen.util.RequestPermissions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThreeGenTheme {
                val navController = rememberNavController()
                val viewModel : ThreeGenViewModel = viewModel()
                val viewModelNew : NewThreeGenViewModel = viewModel()
                // Request permissions
                RequestPermissions(activity = this)
                // Call the function to copy data to Firestore
                //viewModel.copyDataToFirestore()


                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // Call the function to copy data to Firestore
                    AppNavigation(
                        navController = navController,
                        viewModel = viewModel,
                        viewModelNew = viewModelNew,
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
*/