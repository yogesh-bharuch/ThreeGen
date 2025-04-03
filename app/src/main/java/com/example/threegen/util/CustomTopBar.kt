package com.example.threegen.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.threegen.Home


@Composable
fun CustomTopBar(title: String, navController: NavHostController, onBackClick: () -> Unit) {

        Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding() // Automatically adjusts for the status bar
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 0.dp)
                )
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onBackClick){
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            // Title
            Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontSize = MaterialTheme.typography.titleMedium.fontSize, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

            // Home Icon
            IconButton(onClick = {
                navController.navigate(Home) {
                    popUpTo(Home) { inclusive = true }  // Clears back stack
                    launchSingleTop = true  // Prevents multiple instances
                    restoreState = true  // Preserves state
                }
            }) {
                Icon(imageVector = Icons.Filled.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}
