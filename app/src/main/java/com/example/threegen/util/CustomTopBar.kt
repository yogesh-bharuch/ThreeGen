package com.example.threegen.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.threegen.ListScreen
import com.example.threegen.ui.theme.ThreeGenTheme

@Composable
fun CustomTopBar(title: String, navController: NavHostController, onHomeClick: () -> Unit) {
    ThreeGenTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Back button ALWAYS navigates to ListScreen
            IconButton(onClick = {
                navController.navigate(ListScreen) {
                    popUpTo(ListScreen) {
                        inclusive = true
                    }  // Clear the back stack to avoid duplicates
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to List",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            // Title
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // Home Icon
            IconButton(onClick = onHomeClick) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
