
package com.example.threegen.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.threegen.*
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.login.AuthViewModel
import com.example.threegen.login.AuthState
import com.example.threegen.util.SnackbarManager
import com.example.threegen.util.WorkManagerHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val syncMessage by viewModel.syncMessage.collectAsState()
    //val authState by authViewModel.authState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Three Generations") },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavigationButton("Add New Member") { navController.navigate(AddMember) }
            NavigationButton("View All Members") { navController.navigate(ListScreen) }
            NavigationButton("Go To All Members Family Tree") { navController.navigate(MemberTree) }
            //NavigationButton("Go To All Members Family Tree") { navController.navigate(MemberTree(id = 8)) }
            NavigationButton("Go To Unused Members") { navController.navigate(UnusedMembers) }
            //var retMessage : String = "" //List<String>
            Button(
                onClick = {
                    WorkManagerHelper.scheduleImmediateSync(context)

                    /*
                    viewModel.syncLocalDataToFirestore { message ->
                        SnackbarManager.showMessage(message)
                        //Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        Log.d("FirestoreViewModel", "Callback message: $message")
                    }
                    */
                    //Log.d("FirestoreViewModel", "retMessage from Home screen SYnc Data button click after onClick method: $retMessage")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Sync Data")
            }
            // Display message after sync completion
            LaunchedEffect(syncMessage) {
                if (syncMessage.isNotEmpty()) {
                    Log.d("FirestoreViewModel", "LaunchedEffect Triggered: $syncMessage")
                    //Toast.makeText(context, syncMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}




/**
 * NavigationButton - A reusable composable function for creating navigation buttons.
 *
 * @param text - The label of the button.
 * @param onClick - The action to be executed when the button is clicked.
 */
@Composable
fun NavigationButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = text)
    }
}













/*
package com.example.threegen.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.threegen.AddMember
import com.example.threegen.HomeA
import com.example.threegen.ListScreen
import com.example.threegen.MemberFamilyTree
import com.example.threegen.MemberTree
import com.example.threegen.UnusedMembers
import com.example.threegen.data.ThreeGenViewModel

/**
 * HomeScreen - The main screen that serves as the entry point for navigation in the app.
 * It provides buttons to navigate to different sections like adding members, viewing family trees, etc.
 *
 * @param navController - The navigation controller for handling screen transitions.
 * @param viewModel - The ViewModel that provides necessary data (not used yet, but can be useful for dynamic IDs in future).
 * @param modifier - Modifier for customizing layout appearance and behavior.
 */
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize() // Makes the column take up the full screen space
            .padding(24.dp), // Adds padding around the content
        verticalArrangement = Arrangement.spacedBy(16.dp) // Spaces out elements vertically
    ) {
        // Title of the Home Screen
        Text(
            text = "Three Generations",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp, // Large font size for emphasis
                fontWeight = FontWeight.Bold // Bold text for better visibility
            ),
            modifier = Modifier.padding(bottom = 16.dp) // Adds space below the title
        )

        // Button to navigate to Add New Member screen
        NavigationButton("Add New Member") { navController.navigate(AddMember) }

        // Button to navigate to List of all members
        NavigationButton("View All Members") { navController.navigate(ListScreen) }

        // Button to navigate to HomeScreenA with a specific ID (8)
        //NavigationButton("Go To Home ScreenA") { /*navController.navigate(HomeA(id = 8))*/ }

        // Button to navigate to the full family tree starting from a specific member (ID: 8)
        NavigationButton("Go To All Members Family Tree") { /*navController.navigate(MemberTree(id = 8))*/ }

        // Button to navigate to Unused Members screen
        NavigationButton("Go To Unused Members") { /*navController.navigate(UnusedMembers())*/ }

        Button(onClick = {
            viewModel.syncLocalDataToFirestore { message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()}
            }) { Text(text = "Sync Data") }
    }
}

/**
 * NavigationButton - A reusable composable function for creating navigation buttons.
 * This helps avoid repetitive code and makes it easy to add new buttons in the future.
 *
 * @param text - The label of the button.
 * @param onClick - The action to be executed when the button is clicked.
 */
@Composable
fun NavigationButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick, // Triggers navigation when clicked
        modifier = Modifier.fillMaxWidth() // Makes the button expand to full width
    ) {
        Text(text = text) // Displays the button text
    }
}

*/












/*
package com.example.threegen.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.threegen.AddMember
import com.example.threegen.HomeA
import com.example.threegen.ListScreen
import com.example.threegen.MemberFamilyTree
import com.example.threegen.MemberTree
import com.example.threegen.UnusedMembers
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier
) {
   // Log.d("MemberDetails", "home screen started")
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
    ) {
        Text(
            text = "Three Generations",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { navController.navigate(AddMember) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Add New Member")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(ListScreen) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "View All Members")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(HomeA(id = 8)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Go To Home ScreenA")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(MemberTree(id = 8)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Go To All Members Family Tree")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate(UnusedMembers()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Go To Unused Members")
        }




    }
}

 */