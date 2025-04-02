
package com.example.threegen.screens
/* brief overview
    * 1. MemberFamilyTreeScreen Composable
    Purpose: Displays the family tree of a selected member.
    Key Elements:
    memberId: The ID of the selected member to display their family tree.
    viewModel.threeGenList.observeAsState(): Observes the list of all members from the ViewModel.
    findRootMember(): Finds the root ancestor of the selected member.
    findAllDescendants(): Retrieves all descendants of the root member.
    LazyColumn: Displays the family tree in a collapsible format starting from the root member.
    zoomedImageUri: Handles image zoom overlay functionality.
    2. CollapsibleFamilyTreeItem Composable
    Purpose: Recursively displays each family member in the tree with expand/collapse behavior.
    Key Features:
    Expandable Row: Clicking a member expands/collapses their child members.
    Spouse Section: If the member has a spouse, it displays their information.
    Image Zoom: Clicking on the member's or spouse's image opens it in a zoom overlay.
    Background Color: Alternates based on generation level (indent) for visual distinction.
    Text Highlight: Highlights the selected member's name in red.
    3. Utility Functions
    findRootMember():

    Traverses up the family tree to find the root ancestor (a member with no parent).
    findAllDescendants():

    Recursively finds all descendants of the root member to build the full tree.
    getExpandedMemberIds():

    Prepares a set of member IDs that should be expanded by default, ensuring the lineage of the selected member is expanded on load.
    Key Features Implemented:
    Lazy Loading with LazyColumn for efficient rendering.
    Zoomable Images for both members and spouses.
    Collapsible Tree Structure to manage large trees.
    Visual Styling with alternating background colors and highlighted selected members.
    Automatic Expansion of the member's lineage for better user experience.
    This design ensures a smooth, interactive experience when exploring the family tree.
* */
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.Home
import com.example.threegen.MemberFamilyTree
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.util.CustomTopBar
import com.example.threegen.util.MemberState

//--------------------------
fun findRootMember(member: ThreeGen?, members: List<ThreeGen>): ThreeGen? {
    var currentMember = member
    while (currentMember?.parentID != null) {
        currentMember = members.find { it.id == currentMember?.parentID }
    }
    return currentMember
}
fun findAllDescendants(rootMember: ThreeGen, members: List<ThreeGen>): List<ThreeGen> {
    val descendants = mutableListOf<ThreeGen>()
    fun findDescendants(member: ThreeGen) {
        descendants.add(member)
        members.filter { it.parentID == member.id }.forEach { findDescendants(it) }
    }
    findDescendants(rootMember)
    return descendants
}
fun getExpandedMemberIds(memberId: String, members: List<ThreeGen>): Set<String> {
    val expandedMemberIds = mutableSetOf<String>()
    var currentMember = members.find { it.id == memberId }

    while (currentMember != null) {
        expandedMemberIds.add(currentMember.id)
        currentMember = currentMember.parentID?.let { parentId -> members.find { it.id == parentId } }
    }

    return expandedMemberIds
}
//---------------------------

@Composable
fun MemberFamilyTreeScreen(
    modifier: Modifier = Modifier,
    memberId: String,
    navController: NavHostController,
    viewModel: ThreeGenViewModel = viewModel()
) {
    // ✅ Trigger member fetch when the composable is first composed
    LaunchedEffect(Unit) {
        viewModel.fetchMembers() // ✅ Fetch members when screen loads
    }
    // ✅ Collect the current member state from the ViewModel as a StateFlow
    val memberState by viewModel.memberState.collectAsState()
    // ✅ Extract members list from the SuccessList state, or provide an empty list for other states
    val members = when (val state = memberState) {
        is MemberState.SuccessList -> state.members // Extract members when in SuccessList state
        else -> emptyList() // Return an empty list for other states (like Loading, Error, etc.)
    }

    val member = members.find { it.id == memberId }
    var zoomedImageUri by remember { mutableStateOf<String?>(null) }

    val rootMember = findRootMember(member, members)
    val descendants = rootMember?.let { findAllDescendants(it, members) } ?: emptyList()
    Column(modifier = Modifier.fillMaxSize().padding(top = 40.dp).padding(bottom = 40.dp))
    {
        CustomTopBar(title = "Member Family Tree", navController = navController, onHomeClick = { navController.navigate(Home) })
        Box(modifier = modifier.fillMaxSize().padding(16.dp))
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
                    } else
                    {
                        //displaying the FamilyTreeItem composable.
                        rootMember?.let {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                /*item {
                                    if (member != null) {
                                        Text(
                                            text = "Family Tree of ${member.firstName} ${member.middleName} ${member.lastName}",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                    }
                                }*/
                                item {
                                    CollapsibleFamilyTreeItem(navController = navController, member = rootMember, members = members, onImageClick = { uri -> zoomedImageUri = uri }, expandedMemberIds = getExpandedMemberIds(memberId, members), selectedMemberId = memberId)
                                }
                            }
                        }
                    }
                }
            }
            // Zoomed Image Overlay
            zoomedImageUri?.let { uri ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .clickable { zoomedImageUri = null },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Zoomed Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CollapsibleFamilyTreeItem(navController: NavHostController, member: ThreeGen, members: List<ThreeGen>, indent: Int = 0, generation: Int = 1, onImageClick: (String) -> Unit, expandedMemberIds: Set<String>, selectedMemberId: String) {
    val isDarkTheme = isSystemInDarkTheme()
    val generationColors = if (isDarkTheme) {
        listOf(
            Color(0xFF263238), Color(0xFF37474F), Color(0xFF455A64),
            Color(0xFF1C313A), Color(0xFF546E7A), Color(0xFF2C3E50), Color(0xFF3E4A59)
        )
    } else { listOf(Color(0xFFBBDEFB), Color(0xFFC8E6C9), Color(0xFFFFF9C4), Color(0xFFFFCCBC), Color(0xFFD1C4E9), Color(0xFFFFF176), Color(0xFFFF8A65)) }
    val backgroundColor = generationColors[indent % generationColors.size]
    var expanded by remember { mutableStateOf(expandedMemberIds.contains(member.id)) }
    val textColor = if (member.id == selectedMemberId) Color.Red else Color.Black

    Column(modifier = Modifier.padding(start = (indent + 4).dp, bottom = 8.dp).fillMaxWidth().background(color = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded })
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
                    Icon(imageVector = Icons.Default.Person, contentDescription = "Default Person Icon", modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .padding(0.dp)
                        .clickable { })
                } // member image display
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f))
                {
                    Text("Generation: $generation", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text( "${member.firstName} ${member.middleName} ${member.lastName}", style = MaterialTheme.typography.bodyMedium.copy(color = textColor), fontWeight = FontWeight.Bold)
                    Text("Town: ${member.town}", color = textColor)
                } // spouse name and town display
            } // Member display Section
            member.spouseID?.let { spouseId ->
                members.find { it.id == spouseId }?.let { spouse ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = backgroundColor)
                            .clickable { navController.navigate(MemberFamilyTree(spouseId)) }
                    ) {
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
            // Display children recursively when expanded
            members.filter { it.parentID == member.id }
                .sortedWith(compareBy(nullsLast()) { it.childNumber }) // Sort by childNumber, placing nulls last
                .forEach { child ->
                CollapsibleFamilyTreeItem(navController = navController, member = child, members = members, indent = indent + 1, generation = generation + 1, onImageClick = onImageClick, expandedMemberIds = expandedMemberIds, selectedMemberId = selectedMemberId)
            }
        } // Recursively display children
    }
}
