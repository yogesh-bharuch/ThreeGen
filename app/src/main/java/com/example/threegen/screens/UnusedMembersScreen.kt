package com.example.threegen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.Home
import com.example.threegen.MemberDetail
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.ui.theme.Shapes
import com.example.threegen.util.CustomTopBar
import com.example.threegen.util.MemberState


@Composable
fun UnusedMembersScreen(
    viewModel: ThreeGenViewModel = viewModel(),
    navController: NavHostController,
    modifier: Modifier
) {
    // ✅ Trigger member fetch when the composable is first composed
    LaunchedEffect(Unit) {
        viewModel.fetchMembers()
    }
    // ✅ Collect the current member state from the ViewModel as a StateFlow
    val memberState by viewModel.memberState.collectAsState()
    // ✅ Extract members list from the SuccessList state, or provide an empty list for other states
    val members = when (val state = memberState) {
        is MemberState.SuccessList -> state.members
        else -> emptyList()
    }

    // ✅ Extract all unique parent IDs from the members list.
    // This helps in identifying which members have children and assists in structuring the tree.
    val parentIds = remember(members) { members.mapNotNull { it.parentID }.toSet() }

    //have no parent, no spouse, and are not referenced by other members as a parent or spouse.
    val unusedMembers = members.filter { it.parentID == null && it.spouseID == null && it.id !in parentIds && it.id !in members.mapNotNull { member -> member.spouseID } }

    // ✅ State variable to track the currently zoomed image URI.
    // When an image is clicked, this variable holds its URI to display it in a full-screen overlay.
    var zoomedImageUri by remember { mutableStateOf<String?>(null) }
    Column(modifier = Modifier.fillMaxSize().padding(top = 0.dp).padding(bottom = 0.dp))
    {
        CustomTopBar(title = "Orphan Members", navController = navController, onBackClick = { navController.navigate(Home) })
        Box(modifier = modifier.fillMaxSize().padding(4.dp)) {
            when (val state = memberState) {
                is MemberState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                is MemberState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is MemberState.Empty -> {
                    Text(
                        text = "No members found",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is MemberState.Success -> {
                    Text(
                        text = "Its a individual member not a list",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is MemberState.SuccessList -> {
                    if (unusedMembers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No unused members found",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            items(unusedMembers) { member ->
                                UnusedMemberItem(member, navController)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnusedMemberItem(member: ThreeGen, navController: NavHostController) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
            .clickable {
                navController.navigate(MemberDetail(id = member.id)) // Navigate with member ID
            }, // onclick to member detail
        shape = Shapes.medium
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!member.imageUri.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(member.imageUri),
                    contentDescription = "Member Image",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(Shapes.small)
                )
            }// ✅ Member image
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "${member.firstName} ${member.lastName}", style = MaterialTheme.typography.titleMedium)
                Text(text = "Town: ${member.town}", style = MaterialTheme.typography.bodySmall)
            } // ✅ Member Name, Town
        }
    }
}
