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
 /*
        Button(
            onClick = { navController.navigate(MemberFamilyTree(id = 26)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Go To Members Family Tree")
        }

 */


    }
}