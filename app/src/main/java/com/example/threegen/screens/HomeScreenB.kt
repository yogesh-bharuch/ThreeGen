package com.example.threegen.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.threegen.HomeA
import com.example.threegen.HomeB

@Composable
fun HomeScreenB(
    navController: NavHostController,
    args: HomeB,
    modifier: Modifier = Modifier
) {

    Text("Home screen B")
    Button(onClick = {
        navController.navigate(
            HomeA(retVal = "Hello from Home B"))
    })  {
        Text("Go to Home A")
    }


}