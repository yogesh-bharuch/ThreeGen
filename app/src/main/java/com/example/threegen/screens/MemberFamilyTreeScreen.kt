
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
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.Home
import com.example.threegen.ListScreen
import com.example.threegen.MemberDetail
import com.example.threegen.MemberFamilyTree
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.login.AuthViewModel
import com.example.threegen.util.CustomTopBar
import com.example.threegen.util.MemberState
import com.example.threegen.util.MyBottomBar
import com.example.threegen.util.MyFloatingActionButton
import com.example.threegen.util.MyTopAppBar

/**
 * Finds the root ancestor of a given member within a hierarchical tree structure.
 *
 * @param member The starting member whose root ancestor needs to be determined.
 * @param members The list of all members in the tree.
 * @return The root ancestor (ThreeGen object) or null if no valid root is found.
 */
fun findRootMember(member: ThreeGen?, members: List<ThreeGen>): ThreeGen? {
    if (member == null || members.isEmpty()) return null

    val visited = mutableSetOf<String>()
    var currentMember = member

    while (currentMember?.parentID != null) {
        if (!visited.add(currentMember.id)) {
            Log.w("findRootMember", "Cycle detected in tree structure!")
            return null // Break if a cycle is detected
        }
        currentMember = members.find { it.id == currentMember!!.parentID }
    }

    return currentMember
}
/**
 * Finds all descendants of a given root member in a hierarchical tree structure.
 *
 * @param rootMember The root member whose descendants need to be determined.
 * @param members The list of all members in the tree.
 * @return A list of descendants of the root member.
 */
fun findAllDescendants(rootMember: ThreeGen, members: List<ThreeGen>): List<ThreeGen> {
    if (members.isEmpty()) return emptyList()

    val descendants = mutableListOf<ThreeGen>()
    val visited = mutableSetOf<String>()
    val memberMap = members.groupBy { it.parentID }

    fun findDescendants(member: ThreeGen) {
        if (visited.add(member.id)) { // Prevent infinite recursion caused by cycles
            memberMap[member.id]?.forEach {
                descendants.add(it)
                findDescendants(it)
            }
        }
    }

    findDescendants(rootMember)
    return descendants
}
/**
 * Finds all spouses within the tree structure originating from a given root member.
 *
 * @param rootMember The root member whose descendants' spouses need to be collected.
 * @param members The list of all members in the tree.
 * @return A list containing the spouses of the root member and all descendants.
 */
fun findSpousesInTree(rootMember: ThreeGen, members: List<ThreeGen>): List<ThreeGen> {
    if (members.isEmpty()) return emptyList()

    val spouses = mutableListOf<ThreeGen>()
    val visited = mutableSetOf<String>()
    val memberMap = members.groupBy { it.parentID }

    fun findDescendantsAndSpouses(member: ThreeGen) {
        if (visited.add(member.id)) { // Avoid cycles
            // Add the spouse of the current member, if any
            member.spouseID?.let { spouseId ->
                members.find { it.id == spouseId }?.let { spouses.add(it) }
            }

            // Process children (descendants)
            memberMap[member.id]?.forEach { findDescendantsAndSpouses(it) }
        }
    }

    // Start the recursion with the root member
    findDescendantsAndSpouses(rootMember)
    return spouses
}
/**
 * Retrieves a set of all member IDs from the root of the tree to the given member,
 * including the member itself, by traversing upwards in the hierarchy.
 *
 * @param memberId The ID of the starting member whose lineage is to be expanded.
 * @param members A list of all members in the tree.
 * @return A set of member IDs from the root to the given member.
 */
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
    viewModel: ThreeGenViewModel = viewModel(),
    authViewModel: AuthViewModel
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { MyTopAppBar("Member Family Tree",navController, authViewModel, "ListScreen") },
    /*
        bottomBar = { MyBottomBar(navController,viewModel) },

        floatingActionButton = { MyFloatingActionButton(onClick = {
            navController.navigate(MemberDetail(id = ""))   }
        ) },
        floatingActionButtonPosition = FabPosition.End,  // Positions FAB at bottom-end
    */
        snackbarHost = { SnackbarHost(snackbarHostState) } // Manages snackbars
    ) { paddingValues ->
        MemberFamilyTreeScreenContent(paddingValues, navController, viewModel, memberId)
    }
}

@Composable
fun MemberFamilyTreeScreenContent(paddingValues: PaddingValues, navController: NavHostController, viewModel: ThreeGenViewModel, memberId: String) {

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
    val totalSpousesInTheTree = rootMember?.let { findSpousesInTree(it, members) } ?: emptyList()


    Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(start = 0.dp, end = 8.dp))
    {
        //CustomTopBar(title = "Member Family Tree", navController = navController, onBackClick = { navController.navigate(ListScreen) })
        //Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp,bottom = 16.dp,start = 4.dp,end = 4.dp)) {
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
                        val totalDescendants = descendants.size + 1
                        val totalSpouses = totalSpousesInTheTree.size
                        Text(text = "Total Members : $totalDescendants + Total Spouses: $totalSpouses", fontSize = 10.sp, modifier = Modifier.padding(start = 8.dp))
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
        //}
    }
}

@Composable
fun CollapsibleFamilyTreeItem(navController: NavHostController, member: ThreeGen, members: List<ThreeGen>, indent: Int = 0, generation: Int = 1, onImageClick: (String) -> Unit, expandedMemberIds: Set<String>, selectedMemberId: String) {
    val isDarkTheme = isSystemInDarkTheme()
    val generationColors = if (isDarkTheme)
    {
        listOf(Color(0xFF263238), Color(0xFF37474F), Color(0xFF455A64), Color(0xFF1C313A), Color(0xFF546E7A), Color(0xFF2C3E50), Color(0xFF3E4A59))
    } else {
        listOf(Color(0xFFBBDEFB), Color(0xFFC8E6C9), Color(0xFFFFF9C4), Color(0xFFFFCCBC), Color(0xFFD1C4E9), Color(0xFFFFF176), Color(0xFFFF8A65))
    } // card colors based on generation level
    val backgroundColor = generationColors[indent % generationColors.size]
    var expanded by remember { mutableStateOf(expandedMemberIds.contains(member.id)) }
    val textColor = if (member.id == selectedMemberId) Color.Red else Color.Black

    Column(modifier = Modifier.padding(start = (indent + 4).dp, bottom = 8.dp).fillMaxWidth().background(color = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded })
            {
                if (!member.imageUri.isNullOrEmpty()) {
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
                } else
                {
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
                    val isAlive = if (member.isAlive) "" else " (Late)"
                    Text("${member.childNumber}. ${member.firstName} ${member.middleName} ${member.lastName}$isAlive", style = MaterialTheme.typography.bodyMedium.copy(color = textColor), fontWeight = FontWeight.Bold)
                    //Text( "${member.firstName} ${member.middleName} ${member.lastName}", style = MaterialTheme.typography.bodyMedium.copy(color = textColor), fontWeight = FontWeight.Bold)
                    Text("Town: ${member.town}", color = textColor)
                } // Member display Section
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
