package com.example.threegen.util


import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.threegen.Home
import com.example.threegen.MemberTree
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.login.AuthViewModel
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(title: String, navController: NavHostController, authViewModel: AuthViewModel, screen: String) {
    TopAppBar(
        title = { Text(title) },
        modifier = Modifier.fillMaxWidth(),
        navigationIcon = {
            IconButton(onClick = {  if (navController.previousBackStackEntry != null) {
                navController.popBackStack() } }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Back arrow icon
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = { authViewModel.logout() }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
            }
        }
        /*actions = {
            IconButton(onClick = {
                navController.navigate(Home) { // Navigate to Home
                    popUpTo(Home) { inclusive = true }  // Clears back stack
                    launchSingleTop = true  // Prevents multiple instances
                    restoreState = true  // Preserves state
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            }
        }*/
    )
}

@Composable
fun MyBottomBar(navController: NavHostController, viewModel: ThreeGenViewModel) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Sync, contentDescription = "Sync Data") },
            label = { Text("Sync") },
            selected = true,
            onClick = { viewModel.syncFirestoreToRoom(0, false,
                callback = { Log.d("FirestoreSync", "✅ From Sync Data Button Home Screen : Updated last sync time: $it")})
                viewModel.refreshMembersList()
            }
                /*
            onClick = { navController.navigate(Home) {
            popUpTo(Home) { inclusive = true }  // Clears back stack
            launchSingleTop = true  // Prevents multiple instances
            restoreState = true  // Preserves state
            }

            }*/
        )
        NavigationBarItem(
            //icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back") },
            icon = { Icon(Icons.Default.AccountTree, contentDescription = "Family Tree") },
            label = { Text("All Members Tree") },
            selected = false,
            onClick = { navController.navigate(MemberTree) }
        )
    }
}

@Composable
fun MyFloatingActionButton(
    onClick: () -> Unit // Pass a lambda function for the click action
) {
    FloatingActionButton(
        onClick = { onClick() }, // Use the passed lambda
        shape = CircleShape
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Item")
    }
}

/*
fun MyFloatingActionButton(
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    FloatingActionButton(
        onClick = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("FAB Clicked!")
            }
        },
        shape = CircleShape
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Item")
    }
}
*/

