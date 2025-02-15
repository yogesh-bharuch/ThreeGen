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
                // Request permissions
                RequestPermissions(activity = this)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    AppNavigation(
                        navController = navController,
                        viewModel = viewModel,
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}


/*
    Nav(modifier = Modifier
                        .padding(innerPadding))
 */