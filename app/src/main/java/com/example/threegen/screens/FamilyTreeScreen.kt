package com.example.threegen.screens
/*
    * This screen displays the family tree of all root members.
    * * Data Fetching:

    The viewModel.fetchMembers() is triggered once when the composable is first composed using LaunchedEffect.
    The screen observes memberState from the ThreeGenViewModel to display the appropriate UI state.
    Member Display:

    The family tree starts with root members (those without a parent) and recursively displays children using the FamilyTreeItem composable.
    Each item displays the member's name, town, and image (with a default icon if no image is available).
    If the member has a spouse, the spouse's details are displayed beneath the member.
    Expandable Tree Nodes:

    Each family tree item is expandable/collapsible, allowing users to view or hide the member's children.
    Dynamic Colors:

    Background colors for each generation vary based on the theme (light or dark) to visually distinguish hierarchical levels.
    Image Zoom:

    Clicking on any member or spouse image opens a zoomed overlay, enhancing the viewing experience.
    State Handling:

    The screen manages different states like Loading, Error, Empty, or Success and shows corresponding UI feedback.
* */

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.threegen.Home
import com.example.threegen.MemberFamilyTree
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.util.CustomTopBar
import com.example.threegen.util.MemberState

@Composable
fun FamilyTreeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ThreeGenViewModel = viewModel()
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

    /*// ✅ Extract all unique parent IDs from the members list.
    // This helps in identifying which members have children and assists in structuring the tree.*/
    val parentIds = remember(members) { members.mapNotNull { it.parentID }.toSet() }

    /*// ✅ Filter and identify root members for the family tree.
    //Members who do not have a parent ID, but are explicitly listed in parentid column of members.*/
    val rootMembers =  members.filter { it.parentID == null && it.id in parentIds }

    /*// ✅ State variable to track the currently zoomed image URI.
    // When an image is clicked, this variable holds its URI to display it in a full-screen overlay.*/
    var zoomedImageUri by remember { mutableStateOf<String?>(null) }
    Column(modifier = Modifier.fillMaxSize().padding(top = 0.dp).padding(bottom = 0.dp))
    {
        CustomTopBar(title = "Family Tree Root Members", navController = navController, onBackClick = { navController.navigate(Home) })
        Box(modifier = modifier.fillMaxSize().padding(4.dp))
        {
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
                    if (state.members.isEmpty()) {
                        Text(text = "No matching members found", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(rootMembers) { member ->
                                FamilyTreeItem(navController = navController, member = member, members = members, onImageClick = { uri -> zoomedImageUri = uri })
                            }
                        }
                    }
                }
            }
            // ✅ Image Zoom Overlay
            zoomedImageUri?.let { uri ->
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f))
                        .clickable { zoomedImageUri = null },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Zoomed Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )
                }
            } // display image in overlay
        }
    }
}

@Composable
fun FamilyTreeItem(navController: NavController, member: ThreeGen, members: List<ThreeGen>, indent: Int = 0, generation: Int = 1, onImageClick: (String) -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val generationColors = if (isDarkTheme) {
        listOf(Color(0xFF263238), Color(0xFF37474F), Color(0xFF455A64), Color(0xFF1C313A), Color(0xFF546E7A), Color(0xFF2C3E50), Color(0xFF3E4A59))
    } else {
        listOf(Color(0xFFBBDEFB), Color(0xFFC8E6C9), Color(0xFFFFF9C4), Color(0xFFFFCCBC), Color(0xFFD1C4E9), Color(0xFFFFF176), Color(0xFFFF8A65))
    }
    val backgroundColor = generationColors[indent % generationColors.size]
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(start = (indent + 6).dp, bottom = 8.dp).fillMaxWidth().background(color = backgroundColor).clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth())
            {
                if (!member.imageUri.isNullOrEmpty())
                {
                    AsyncImage(
                        model = member.imageUri,
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .padding(0.dp)
                            .clickable { onImageClick(member.imageUri ?: "") }
                    )
                } else {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "Default Person Icon", modifier = Modifier.size(48.dp).clip(CircleShape).padding(0.dp).clickable { })
                } // member image display
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f))
                {
                    Text("Generation: $generation", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("${member.firstName} ${member.middleName} ${member.lastName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("Town: ${member.town}")
                } // member name and town display
            } // Member display Section
            member.spouseID?.let{ spouseId ->
                members.find { it.id == spouseId }?.let { spouse ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                            .fillMaxWidth()
                            .background(color = backgroundColor)
                            .clickable { navController.navigate(MemberFamilyTree(spouseId)) }) {
                        if (!spouse.imageUri.isNullOrEmpty())
                        {
                            AsyncImage(
                                model = spouse.imageUri,
                                contentDescription = "Spouse Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .padding(0.dp)
                                    .clickable { onImageClick(spouse.imageUri ?: "") }
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Spouse Icon",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .padding(0.dp)
                            )
                        } // spouse image display
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f))
                        {
                            Text("Spouse", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("${spouse.firstName} ${spouse.middleName} ${spouse.lastName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            //Spouse name and town display
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Town: ${spouse.town}")
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Go to Spouse Detail")
                            }
                        } // spouse name and town display
                    }
                }
            } // Spouse display Section
        }
        if (expanded) {
            members.filter { it.parentID == member.id }
                .sortedWith(compareBy(nullsLast()) { it.childNumber }) // Sort by childNumber, placing nulls last
                .forEach { child ->
                // Recursively display children
                FamilyTreeItem(navController = navController, member = child, members = members, indent = indent + 1, generation = generation + 1, onImageClick = onImageClick)
            }
        } // Recursively display children
    }
}
