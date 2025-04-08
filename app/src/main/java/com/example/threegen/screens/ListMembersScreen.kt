
package com.example.threegen.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import com.example.threegen.login.AuthViewModel
import com.example.threegen.util.MemberState
import com.example.threegen.util.MyBottomBar
import com.example.threegen.util.MyFloatingActionButton
import com.example.threegen.util.MyTopAppBar
import kotlinx.coroutines.delay

@Composable
fun ListMembersScreen(
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
)
{
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { MyTopAppBar("Members List",navController, authViewModel, "ListScreen") },
        bottomBar = { MyBottomBar(navController,viewModel, authViewModel) },

        floatingActionButton = { MyFloatingActionButton(onClick = {
            navController.navigate(MemberDetail(id = ""))   }
        ) },
        floatingActionButtonPosition = FabPosition.End,  // Positions FAB at bottom-end
        snackbarHost = { SnackbarHost(snackbarHostState) } // Manages snackbars
    ) { paddingValues ->
        ListMembersScreenContent(paddingValues, navController, viewModel)
    }
}

@Composable
fun ListMembersScreenContent(paddingValues: PaddingValues, navController: NavHostController, viewModel: ThreeGenViewModel) {
    val memberState by viewModel.memberState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var totalMembers = 0
    val refreshMembersList by viewModel.refreshMembersList.collectAsState()


    LaunchedEffect(refreshMembersList)
    {
        delay(500)
        viewModel.fetchMembers() // ✅ Fetch members when screen loads
    }

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues))
    {
        Text(text = "e.g. YJVT for yogesh jayendra vyas, thavad", fontSize = 9.sp, modifier = Modifier.padding(start = 8.dp))
        OutlinedTextField(value = searchQuery, onValueChange = { viewModel.updateSearchQuery(it) }, label = { Text("Search by Short Name", fontSize = 10.sp) }, leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp))
        //Spacer(modifier = Modifier.height(4.dp))
        // ✅ Handle different states
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
                    text = "Wrong member State for List its for single member",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is MemberState.SuccessList -> {
                val filteredMembers = if (searchQuery.isBlank()) {
                    totalMembers = state.members.size
                    //Text(text = "Total Members : $totalMembers", fontSize = 10.sp)
                    state.members // ✅ Show all members when search is empty
                } else {
                    state.members.filter {
                        it.shortName.contains(
                            searchQuery,
                            ignoreCase = true
                        )
                    }
                }

                if (filteredMembers.isEmpty()) {
                    Text(text = "No matching members found", color = Color.Gray, modifier = Modifier.padding(16.dp))
                } else {
                    totalMembers = filteredMembers.size
                    Text(text = "Total Members : $totalMembers", fontSize = 10.sp, modifier = Modifier.padding(start = 8.dp))
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
                            //HorizontalDivider()
                        }
                    }
                }
            }
        }
    } // main column in screen

    // ✅ Full-screen overlay for image preview
    selectedImageUri?.let { uri ->
        FullScreenImageOverlay(imageUri = uri, onDismiss = { selectedImageUri = null })
    } // overlay in main screen
}

// ✅ Member list item UI
@Composable
fun MemberListItem(member: ThreeGen, onItemClick: () -> Unit, onImageClick: (String) -> Unit, onTreeClick: (String) -> Unit) {
    Log.d("MemberDetails", "Rendering item: ${member.id}, Image URI: ${member.imageUri}")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    )
    {
        // display image
        Row(modifier = Modifier.padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
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
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "No Profile Image",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    tint = Color.Gray
                )
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = "id: ${member.id}", fontSize = 7.5.sp)
                //Spacer(modifier = Modifier.height(1.dp)) // Add spacing between texts
                Text(text = "Short Name: ${member.shortName}", fontSize = 12.sp)
            }

        }
        //Spacer(modifier = Modifier.width(4.dp))
        Row(modifier = Modifier.padding(start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(start = 4.dp, end = 4.dp)) {
                val isAlive = if (member.isAlive) "" else " (Late)"
                val childNumber = " (Child# ${member.childNumber.toString()})"
                Text(text = "${member.firstName} ${member.middleName} ${member.lastName} $isAlive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = "${member.town}, $childNumber", fontSize = 12.sp)
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
}
/*
@Composable
fun MemberListItem(member: ThreeGen, onItemClick: () -> Unit, onImageClick: (String) -> Unit, onTreeClick: (String) -> Unit) {
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
*/

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

