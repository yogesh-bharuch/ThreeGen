package com.example.threegen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.threegen.data.NewThreeGenViewModel
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.screens.AddMemberScreen
import com.example.threegen.screens.FamilyTreeScreen
import com.example.threegen.screens.HomeScreen
import com.example.threegen.screens.HomeScreenA
import com.example.threegen.screens.ListMembersScreen
import com.example.threegen.screens.MemberDetailScreen
import com.example.threegen.screens.MemberFamilyTreeScreen
import com.example.threegen.screens.SelectMemberScreen
import com.example.threegen.screens.SelectSpouseScreen
import kotlinx.serialization.Serializable

@Composable
fun AppNavigation(
    viewModel: ThreeGenViewModel,
    viewModelNew: NewThreeGenViewModel,
    modifier: Modifier,
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = Home) {
        // HomePage route
        composable<Home> {
            HomeScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        // AddMemberPage route (if applicable)
        composable<AddMember> {
            AddMemberScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        // ListMembersPage route
        composable<ListScreen> {
            ListMembersScreen(
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        //MemberDetailPage
        composable<MemberDetail> {
            val arg = it.toRoute<MemberDetail>()
            MemberDetailScreen(
                memberId = arg.id,
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        //Select Parent
        composable<SelectParent> {
            val arg = it.toRoute<SelectParent>()
            SelectMemberScreen(
                memberId = arg.id,
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        //Select Spouse
        composable<SelectSpouse> {
            val arg = it.toRoute<SelectSpouse>()
            SelectSpouseScreen(
                memberId = arg.id,
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        //Member tree
        composable<MemberTree> {
            val arg = it.toRoute<MemberTree>()
            FamilyTreeScreen(
                memberId = arg.id,
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        //Member tree
        composable<MemberFamilyTree> {
            val arg = it.toRoute<MemberFamilyTree>()
            MemberFamilyTreeScreen(
                memberId = arg.id,
                navController = navController,
                viewModel = viewModel,
                modifier = modifier
            )
        }

        //HomeA
        composable<HomeA> {
            val arg = it.toRoute<HomeA>()
            HomeScreenA(
                navController = navController,
                viewModel = viewModelNew,
                modifier = modifier,
                memberId = arg.id
            )
        }
    }
}



@Serializable
object Home

@Serializable
data class HomeA(
    val id : Int = 1
)

@Serializable
data class MemberDetail(
    val id : Int = 0,
)

@Serializable
data class SelectParent(
    val id : Int = 0
)

@Serializable
data class SelectSpouse(
    val id : Int = 0
)

@Serializable
data class MemberTree(
    val id : Int = 0
)

@Serializable
data class MemberFamilyTree(
    val id : Int = 0
)

@Serializable
object AddMember

@Serializable
object ListScreen

