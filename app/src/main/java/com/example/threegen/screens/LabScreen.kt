package com.example.threegen.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.threegen.util.MyFloatingActionButton
import com.example.threegen.util.MyTopAppBar

@Composable
fun LabScreen(
    modifier: Modifier = Modifier,
    memberId: String,
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    authViewModel: AuthViewModel
)
{
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { MyTopAppBar("Members List",navController, authViewModel,"ListScreen") },
        //bottomBar = { MyBottomBar(navController) },

        floatingActionButton = { MyFloatingActionButton(onClick = {
            navController.navigate(MemberDetail(id = ""))   }
        ) },
        floatingActionButtonPosition = FabPosition.End,  // Positions FAB at bottom-end
        snackbarHost = { SnackbarHost(snackbarHostState) } // Manages snackbars
    ) { paddingValues ->
        MyContent(paddingValues, navController, viewModel)
    }
}


@Composable
fun MyContent(paddingValues: PaddingValues, navController: NavHostController, viewModel: ThreeGenViewModel) {
    LaunchedEffect(Unit)
    {
        viewModel.fetchMembers() // ✅ Fetch members when screen loads
    }

    val memberState by viewModel.memberState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var totalMembers = 0

    Column(modifier = Modifier.padding(paddingValues)) {
        Text(text = "e.g. YJVT for yogesh jayendra vyas, thavad", fontSize = 9.sp)
        OutlinedTextField(
            value = viewModel.searchQuery.collectAsState().value,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Search by Short Name") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp)
        )
        //Text(text = "Total Members : $totalMembers", fontSize = 10.sp)
        //Spacer(modifier = Modifier.height(8.dp))
        // ✅ Handle different states
        when (val state = memberState) {
            is MemberState.Loading -> { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
            is MemberState.Error -> { Text(text = "Error: ${state.message}", color = Color.Red, modifier = Modifier.padding(16.dp)) }
            is MemberState.Empty -> { Text(text = "No members found", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
            is MemberState.Success -> { Text(text = "Wrong member State for List its for single member", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
            is MemberState.SuccessList -> {
                val filteredMembers = if (searchQuery.isBlank()) {
                    val totalMembers = state.members.size
                    Text(text = "Total Members : $totalMembers", fontSize = 10.sp)
                    state.members // ✅ Show all members when search is empty
                } else {
                    state.members.filter {
                        it.shortName.contains(
                            searchQuery,
                            ignoreCase = true
                        )
                    }
                }

                if (filteredMembers.isEmpty()) { Text(text = "No matching members found", color = Color.Gray, modifier = Modifier.padding(16.dp))
                } else {
                    totalMembers = filteredMembers.size
                    Text(text = "Total Members : $totalMembers", fontSize = 10.sp, modifier = Modifier.padding(start = 8.dp))
                    LazyColumn(contentPadding = paddingValues, verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxSize().padding(start = 4.dp, end = 4.dp)) {
                        items(filteredMembers) { member ->
                            MemberListItem1(
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
    }
    // ✅ Full-screen overlay for image preview
    selectedImageUri?.let{ uri ->
        FullScreenImageOverlay1(imageUri = uri, onDismiss = { selectedImageUri = null })
    } // overlay in main screen

}


// ✅ Member list item UI
@Composable
fun MemberListItem1(member: ThreeGen, onItemClick: () -> Unit, onImageClick: (String) -> Unit, onTreeClick: (String) -> Unit) {
    Log.d("MemberDetails", "Rendering item: ${member.id}, Image URI: ${member.imageUri}")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    )
    {
        // display image
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
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
        Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(start = 4.dp, end = 4.dp)) {
                Text(
                    text = "${member.firstName} ${member.middleName} ${member.lastName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Town: ${member.town}", fontSize = 12.sp)
                //Text(text = "Short Name: ${member.shortName}", fontSize = 12.sp)
                //Text(text = "id: ${member.id}", fontSize = 9.sp)
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

// ✅ Full-screen image preview
@Composable
fun FullScreenImageOverlay1(imageUri: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() },
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



@Composable
fun ListItem(index: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = "Item #$index",
            modifier = Modifier.padding(16.dp)
        )
    }
}
