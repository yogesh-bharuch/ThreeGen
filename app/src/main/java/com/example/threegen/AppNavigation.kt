package com.example.threegen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.login.AuthState
import com.example.threegen.login.AuthViewModel
import com.example.threegen.login.LoginScreen
import com.example.threegen.login.RegistrationScreen
import com.example.threegen.screens.*
import kotlinx.serialization.Serializable

@Composable
fun AppNavigation(
    viewModel: ThreeGenViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier,
    navController: NavHostController
) {

    // Direct Firebase user check to avoid unnecessary navigation to Login
    val isUserLoggedIn = authViewModel.currentUser != null
    val authState by authViewModel.authState.collectAsState()

    // Set the initial start destination based on direct Firebase check
    val startDestination = if (isUserLoggedIn) Home else Login

    // Check current user on app start to ensure correct state
    LaunchedEffect(Unit) {
        authViewModel.checkCurrentUser()
    }

    // Handle only explicit logout scenario
    LaunchedEffect(authState) {
        if (authState is AuthState.Idle && !isUserLoggedIn) {
            navController.navigate(Login) {
                popUpTo(Home) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // Home screen route
        composable<Home> {
            HomeScreen(
                navController = navController,
                viewModel = viewModel,
                authViewModel = authViewModel,
                modifier = modifier
            )
        }

        // List Members screen
        composable<ListScreen> {
            ListMembersScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        // Member Detail screen (requires a member ID argument)
        composable<MemberDetail> {
            val arg = it.toRoute<MemberDetail>()
            arg.id?.let { it1 ->
                MemberDetailScreen(
                    memberId = it1,
                    navController = navController,
                    viewModel = viewModel,
                    modifier = modifier,
                    onNavigateBack = { navController.popBackStack() } // ✅ Corrected placement
                )
            }
        }

        // Select parent selection
        composable<SelectMemberParent> {
            SelectMemberParentScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        // Select spouse selection
        composable<SelectMemberSpouse> {
            SelectMemberSpouseScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        // Login Screen
        composable<Login> {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }

        composable<Registration> {
            RegistrationScreen(navController, authViewModel)
        }

        // Family Tree screen for Root members
        composable<MemberTree> {
            FamilyTreeScreen(
                modifier = modifier,
                navController = navController,
                viewModel = viewModel
            )
        }

        // Unused Orphan Members screen
        composable<UnusedMembers> {
            UnusedMembersScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        // Member Family Tree screen (shows detailed tree for one member)
        composable<MemberFamilyTree> {
            val arg = it.toRoute<MemberFamilyTree>()
            MemberFamilyTreeScreen(
                memberId = arg.id,
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }
    }
}


// Home Screen (No arguments)
@Serializable
object Home

// Home Screen (No arguments)
@Serializable
object Registration

@Serializable
object Login


// Member Detail screen (Requires a member ID)
@Serializable
data class MemberDetail(
    val id: String?
)

// Select Member screen
@Serializable
object SelectMemberParent

@Serializable
object SelectMemberSpouse

// Family Tree screen (Requires a member ID)
@Serializable
object MemberTree

// Unused Members screen (May require an ID in the future)
@Serializable
object UnusedMembers

// Member Family Tree screen (Requires a member ID)
@Serializable
data class MemberFamilyTree(
    val id: String
)

// List Members screen (No arguments)
@Serializable
object ListScreen
