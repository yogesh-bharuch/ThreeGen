


package com.example.threegen.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.MemberDetail
import com.example.threegen.MemberFamilyTree
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.util.MemberState
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class) // ✅ Opt-in for debounce
@Composable
fun ListMembersScreen(
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier
)
{
    LaunchedEffect(Unit)
    {
        viewModel.fetchMembers() // ✅ Fetch members when screen loads
    }

    val memberState by viewModel.memberState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(
            modifier = Modifier.heightIn(max = 56.dp),
            title = { Text("Member List") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /*navController.navigate("home")*/ }) {
                    Icon(Icons.Default.Home, contentDescription = "Home")
                }
            }
        )},
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(MemberDetail(id = "")) }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            } },
        bottomBar = { /* Slot for a BottomBar */ },
        //floatingActionButton = { /* Slot for a FAB (Floating Action Button) */ },
        floatingActionButtonPosition = FabPosition.EndOverlay, // Default is End
        //drawerContent = { /* Slot for a Navigation Drawer */ },
        snackbarHost = { /* Slot for displaying SnackbarHost */ }
    )
    {
        Column(modifier = modifier.fillMaxSize().padding(it).padding(0.dp), horizontalAlignment = Alignment.CenterHorizontally)
        {
            // Screen Title
            //Text(text = "Member List", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            //Spacer(modifier = Modifier.height(12.dp))
            // Search Field
            Text(text = "e.g. YJVT for yogesh jayendra vyas town : thavad", fontSize = 10.sp)
            OutlinedTextField(value = searchQuery, onValueChange = { viewModel.updateSearchQuery(it) }, label = { Text("Search by Short Name", fontSize = 10.sp) }, leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            // ✅ Handle different states
            when (val state = memberState) {
                is MemberState.Loading -> { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
                is MemberState.Error -> { Text(text = "Error: ${state.message}", color = Color.Red, modifier = Modifier.padding(16.dp)) }
                is MemberState.Empty -> { Text(text = "No members found", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
                is MemberState.Success -> { Text(text = "Wrong member State for List its for single member", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
                is MemberState.SuccessList -> {
                    val filteredMembers = if (searchQuery.isBlank()) {
                        state.members // ✅ Show all members when search is empty
                    } else {
                        state.members.filter {
                            it.shortName.contains(
                                searchQuery,
                                ignoreCase = true
                            )
                        } }

                    if (filteredMembers.isEmpty()) { Text(text = "No matching members found", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn {
                            items(filteredMembers) { member ->
                                MemberListItem(
                                    member = member,
                                    onItemClick = {
                                        //navController.navigate(MemberDetail(id = ""))
                                        navController.navigate(MemberDetail(id = member.id))
                                        viewModel.updateSearchQuery("")
                                    },
                                    onImageClick = { selectedImageUri = it },
                                    onTreeClick = { navController.navigate(MemberFamilyTree(member.id)) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        } // main column in screen

        // ✅ Full-screen overlay for image preview
        selectedImageUri?.let{ uri ->
            FullScreenImageOverlay(imageUri = uri, onDismiss = { selectedImageUri = null })
        } // overlay in main screen
    } // scaffold
} // listmember

// ✅ Member list item UI
@Composable
fun MemberListItem(
    member: ThreeGen,
    onItemClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onTreeClick: (String) -> Unit
) {
    Log.d("MemberDetails", "Rendering item: ${member.id}, Image URI: ${member.imageUri}")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // display image
        if (!member.imageUri.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(member.imageUri),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable { onImageClick(member.imageUri!!) }
            )
        } else {
            Icon(imageVector = Icons.Default.Person, contentDescription = "No Profile Image", modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape), tint = Color.Gray)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "${member.firstName} ${member.middleName} ${member.lastName}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = "Town: ${member.town}", fontSize = 12.sp)
            Text(text = "Short Name: ${member.shortName}", fontSize = 12.sp)
            Text(text = "id: ${member.id}", fontSize = 9.sp)
        } // displays fullname, town, shortname

        // ✅ Spacer to push the tree icon to the right
        Spacer(modifier = Modifier.weight(0.1f))

        // ✅ Tree Icon with Light Gray color on the right side
        IconButton(onClick = { onTreeClick(member.id) }) {
            Icon(
                imageVector = Icons.Default.Groups2,  // 🔥 Alternative M3 Icon
                contentDescription = "View Family Tree",
                modifier = Modifier.size(48.dp),    // ✅ Double size
                tint = Color.LightGray  // ✅ Light Gray color
            )
        }

    }
}

// ✅ Full-screen image preview
@Composable
fun FullScreenImageOverlay(imageUri: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = "Full-Screen Image",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

