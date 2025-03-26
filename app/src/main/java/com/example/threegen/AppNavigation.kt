package com.example.threegen

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.threegen.data.NewThreeGenViewModel
import com.example.threegen.data.ThreeGen
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
    viewModelNew: NewThreeGenViewModel,
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

        // Add Member screen
        composable<AddMember> {
            AddMemberScreen(
                navController = navController,
                viewModel = viewModel,
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
        // shares FamilyTreeScreen with Unused Orphan Members screen with orphanMember = false as different parameter
        composable<MemberTree> {
            FamilyTreeScreen(
                orphanMember = false,
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }
        // Unused Orphan Members screen
        // shares FamilyTreeScreen with Unused Orphan Members screen with orphanMember = false as different parameter
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


// Alternative Home Screen with an ID argument
@Serializable
data class HomeA(
    val id: Int = 1
)

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
/*@Serializable
data class MemberTree(
    val id: Int = 0
)*/

// Unused Members screen (May require an ID in the future)
@Serializable
object UnusedMembers

// Member Family Tree screen (Requires a member ID)
@Serializable
data class MemberFamilyTree(
    val id: String
)

// Add Member screen (No arguments)
@Serializable
object AddMember

// List Members screen (No arguments)
@Serializable
object ListScreen
















/*
package com.example.threegen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.threegen.data.NewThreeGenViewModel
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.screens.*
import kotlinx.serialization.Serializable

/**
 * AppNavigation handles the navigation between different screens in the app.
 * It uses Jetpack Compose's Navigation API and follows a type-safe navigation approach.
 *
 * @param viewModel The main ViewModel handling ThreeGen data operations.
 * @param viewModelNew A separate ViewModel (if applicable) for additional functionalities.
 * @param modifier Modifier to style and manage layout.
 * @param navController The navigation controller to manage app navigation.
 */
@Composable
fun AppNavigation(
    viewModel: ThreeGenViewModel,
    viewModelNew: NewThreeGenViewModel,
    modifier: Modifier,
    navController: NavHostController
) {
    // Navigation graph setup
    NavHost(navController = navController, startDestination = Home) {

        // Home screen route
        composable<Home> {
            HomeScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        // Add Member screen
        composable<AddMember> {
            AddMemberScreen(
                navController = navController,
                viewModel = viewModel,
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
        /*
        // Select Parent screen
        composable<SelectParent> {
            val arg = it.toRoute<SelectParent>()
            SelectMemberScreen(
                memberId = arg.id,
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        // Select Spouse screen
        composable<SelectSpouse> {
            val arg = it.toRoute<SelectSpouse>()
            SelectSpouseScreen(
                memberId = arg.id,
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        // Family Tree screen for a specific member
        composable<MemberTree> {
            val arg = it.toRoute<MemberTree>()
            FamilyTreeScreen(
                memberId = arg.id,
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        // Unused Members screen
        composable<UnusedMembers> {
            val arg = it.toRoute<UnusedMembers>()
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

        // Home Screen A (Possibly another version of Home, requires an ID argument)
        composable<HomeA> {
            val arg = it.toRoute<HomeA>()
            HomeScreenA(
                navController = navController,
                viewModel = viewModelNew,
                modifier = modifier,
                memberId = arg.id
            )
        }
        */
    }
}

/**
 * Navigation destinations for the app.
 * These classes represent different screens and their required arguments.
 */

// Home Screen (No arguments)
@Serializable
object Home

// Alternative Home Screen with an ID argument
@Serializable
data class HomeA(
    val id: Int = 1
)

// Member Detail screen (Requires a member ID)
@Serializable
data class MemberDetail(
    val id: String?
)

// Select Parent screen (Requires a member ID)
@Serializable
data class SelectParent(
    val id: Int = 0
)

// Select Spouse screen (Requires a member ID)
@Serializable
data class SelectSpouse(
    val id: Int = 0
)

// Family Tree screen (Requires a member ID)
@Serializable
data class MemberTree(
    val id: Int = 0
)

// Unused Members screen (May require an ID in the future)
@Serializable
data class UnusedMembers(
    val id: Int = 0
)

// Member Family Tree screen (Requires a member ID)
@Serializable
data class MemberFamilyTree(
    val id: Int = 0
)

// Add Member screen (No arguments)
@Serializable
object AddMember

// List Members screen (No arguments)
@Serializable
object ListScreen



 */