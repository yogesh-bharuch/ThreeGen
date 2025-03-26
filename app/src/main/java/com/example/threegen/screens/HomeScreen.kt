package com.example.threegen.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.threegen.*
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.login.AuthViewModel
import com.example.threegen.util.SnackbarManager
import com.example.threegen.util.WorkManagerHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
            //NavigationButton("Add New Member") { navController.navigate(AddMember) }
            NavigationButton("View All Members") { navController.navigate(ListScreen) }
            NavigationButton("Go To All Members Family Tree") { navController.navigate(MemberTree) }
            NavigationButton("Go To Unused Members") { navController.navigate(UnusedMembers) }
            // ✅ Manual Sync Button
            /*
            Button(
                onClick = {
                    // ✅ Trigger manual sync (no result callback needed)
                    WorkManagerHelper.scheduleImmediateSync(context)
                    Log.d("HomeScreen", "🔥 Manual sync triggered")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Sync Data")
            }
            */
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

