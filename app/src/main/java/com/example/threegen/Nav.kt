package com.example.threegen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.threegen.screens.HomeScreenA
import com.example.threegen.screens.HomeScreenB
import kotlinx.serialization.Serializable

@Composable
fun Nav(modifier: Modifier = Modifier) {

    val navController = rememberNavController()

    NavHost(navController = navController,
        startDestination = HomeA("Wel Come")
    ){
        composable<HomeA> {
            val args = it.toRoute<HomeA>()
            HomeScreenA(
                navController = navController,
                args,
                modifier = modifier)
        }

        composable<HomeB> {
            val args = it.toRoute<HomeB>()
            HomeScreenB(
                navController = navController,
                args,
                modifier = modifier)
        }
    }

}


@Serializable
data class HomeA(
    val retVal: String? = ""
)

@Serializable
data class HomeB(
    val id: Int = 21
)

