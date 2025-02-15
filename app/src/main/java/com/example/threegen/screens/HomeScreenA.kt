package com.example.threegen.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.threegen.HomeA
import com.example.threegen.HomeB

@Composable
fun HomeScreenA(
                navController: NavHostController,
                args: HomeA,
                modifier: Modifier = Modifier
) {
    Text("Home screen A")
    Button(onClick = {
        navController.navigate(HomeB(id = 10))
    })  {
        Text("Go to Home B")
    }

}