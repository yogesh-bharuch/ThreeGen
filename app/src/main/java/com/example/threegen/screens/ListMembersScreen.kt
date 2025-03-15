


package com.example.threegen.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.util.MemberState
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class) // ✅ Opt-in for debounce
@Composable
fun ListMembersScreen(
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.fetchMembers() // ✅ Fetch members when screen loads
    }

    val memberState by viewModel.memberState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    Log.d("MemberDetails", "ListMember screen started")

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Title
        Text(
            text = "Member List",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Search by Short Name", fontSize = 10.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ Handle different states
        when (val state = memberState) {
            is MemberState.Loading -> { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
            is MemberState.Error -> { Text(text = "Error: ${state.message}", color = Color.Red, modifier = Modifier.padding(16.dp)) }
            is MemberState.Empty -> { Text(text = "No members found", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
            is MemberState.Success -> { Text(text = "Wrong member State for List its for single member", color = Color.Gray, modifier = Modifier.padding(16.dp))}
            is MemberState.SuccessList -> {
                val filteredMembers = if (searchQuery.isBlank()) {
                    state.members // ✅ Show all members when search is empty
                } else {
                    state.members.filter { it.shortName.contains(searchQuery, ignoreCase = true) }
                }

                if (filteredMembers.isEmpty()) {
                    Text(text = "No matching members found", color = Color.Gray, modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn {
                        items(filteredMembers) { member ->
                            MemberListItem(
                                member = member,
                                onItemClick = {
                                    navController.navigate(MemberDetail(id = member.id))
                                    viewModel.updateSearchQuery("") },
                                onImageClick = { selectedImageUri = it }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    // ✅ Full-screen overlay for image preview
    selectedImageUri?.let { uri ->
        FullScreenImageOverlay(imageUri = uri, onDismiss = { selectedImageUri = null })
    }
}

// ✅ Member list item UI
@Composable
fun MemberListItem(
    member: ThreeGen,
    onItemClick: () -> Unit,
    onImageClick: (String) -> Unit
) {
    Log.d("MemberDetails", "Rendering item: ${member.id}, Image URI: ${member.imageUri}")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                modifier = Modifier.size(56.dp).clip(CircleShape),
                tint = Color.Gray
            )
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
























/*
package com.example.threegen.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.MemberDetail
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun ListMembersScreen(
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier
) {
    // Fetch members when screen loads
    LaunchedEffect(Unit) {
        viewModel.fetchMembers()
    }

    // Observe members list from ViewModel
    val memberList by viewModel.filteredMembers.collectAsState()

    // Observe search query from ViewModel
    val searchQuery by viewModel.searchQuery.collectAsState()

    // State for image overlay
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    Log.d("MemberDetails", "ListMember screen started with members: ${memberList.size}")

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Title
        Text(
            text = "Member List",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Search by Short Name", fontSize = 10.sp) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Person, contentDescription = null)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // List of Members
        if (memberList.isEmpty()) {
            Text(text = "No members found", color = Color.Gray, modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn {
                items(memberList) { member ->
                    MemberListItem(
                        member = member,
                        onItemClick = { navController.navigate(MemberDetail(id = member.id)) },
                        onImageClick = { selectedImageUri = it } // Set image for overlay
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    // Full-screen overlay for image preview
    selectedImageUri?.let { uri ->
        FullScreenImageOverlay(imageUri = uri, onDismiss = { selectedImageUri = null })
    }
}

@Composable
fun MemberListItem(
    member: ThreeGen,
    onItemClick: () -> Unit,
    onImageClick: (String) -> Unit
) {
    Log.d("MemberDetails", "Rendering item: ${member.id}, Image URI: ${member.imageUri}")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically // ✅ Centers the image vertically
    ) {
        // Circular Profile Image with Click for Full-Screen Preview
        if (!member.imageUri.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(member.imageUri),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop, // ✅ Crops the image to fit
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape) // ✅ Circular shape
                    .clickable { onImageClick(member.imageUri!!) } // ✅ Opens image in overlay
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "No Profile Image",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape), // ✅ Circular shape
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "${member.firstName} ${member.middleName} ${member.lastName}",
                fontSize = 12.sp, // ✅ Reduced font size
                fontWeight = FontWeight.Bold
            )
            Text(text = "Town: ${member.town}", fontSize = 12.sp) // ✅ Reduced font size
            Text(text = "Short Name: ${member.shortName}", fontSize = 12.sp) // ✅ Added short name
        }
    }
}

@Composable
fun FullScreenImageOverlay(imageUri: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() }, // ✅ Click anywhere to close overlay
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


 */















































/*
package com.example.threegen.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.MemberDetail
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel


@Composable
fun ListMembersScreen(
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Member List",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.padding(16.dp)
    )

    val memberList by viewModel.threeGenList.observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val filteredMembers = memberList.filter {
        it.shortName.contains(searchQuery, ignoreCase = true)
    }

    Log.d("MemberDetails", "ListMember screen started $memberList")

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Member List",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Field
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by Short Name") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(filteredMembers) { member ->
                MemberListItem(
                    member = member,
                    onItemClick = {
                        navController.navigate(MemberDetail(id = member.id))
                    }
                )
                Divider()
            }
        }
    }

}


@Composable
fun MemberListItem(
    member: ThreeGen,
    onItemClick: () -> Unit
) {
    // Add this log statement to check the image URI
    // Log.d("MemberDetails", "Image URI in list page: ${member.imageUri}")

    ListItem(
        leadingContent = {
            Log.d("MemberDetails", "Image URI in list page: ${member.imageUri}")
            if (member.imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(member.imageUri),
                    contentDescription = "Profile Image",
                    modifier = Modifier.size(56.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "No Profile Image",
                    modifier = Modifier.size(56.dp)
                )
            }
        },
        headlineContent = {
            Text(text = "${member.firstName} ${member.middleName} ${member.lastName}")
        },
        supportingContent = {
            Text(text = "Town: ${member.town}      id: ${member.id}")

        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}


 */





















/*

package com.example.threegen.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.MemberDetail
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun ListMembersScreen(
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier
) {
    val memberList by viewModel.threeGenList.observeAsState(emptyList())  //.observeAsState(emptyList())  //GenList.observeAsState(emptyList())
     //val memberList by viewModel.memberData.observeAsState(emptyList())  //GenList.observeAsState(emptyList())
    Log.d("MemberDetails", "ListMember screen started $memberList")
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Member List",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            //items()
            items(memberList) { member ->
                MemberListItem(
                    member = member,
                    onItemClick = {
                        navController.navigate(MemberDetail(id = member.id))
                    }
                )
                Divider()
            }
        }





    /*
        Button(
            onClick = { navController.navigate("addmember") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Go to Add New Member Page")
        }
    */
    }
}


@Composable
fun MemberListItem(
    member: ThreeGen,
    onItemClick: () -> Unit
) {
    // Add this log statement to check the image URI
    // Log.d("MemberDetails", "Image URI in list page: ${member.imageUri}")

    ListItem(
        leadingContent = {
            Log.d("MemberDetails", "Image URI in list page: ${member.imageUri}")
            if (member.imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(member.imageUri),
                    contentDescription = "Profile Image",
                    modifier = Modifier.size(56.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "No Profile Image",
                    modifier = Modifier.size(56.dp)
                )
            }
        },
        headlineContent = {
            Text(text = "${member.firstName} ${member.middleName} ${member.lastName}")
        },
        supportingContent = {
            Text(text = "Town: ${member.town}")
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}


 */