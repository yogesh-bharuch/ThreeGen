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
import com.example.threegen.SelectMemberParent
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.util.MemberState
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class) // ✅ Opt-in for debounce
@Composable
fun SelectMemberSpouseScreen(
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier,
    //onSpouseSelected: (ThreeGen) -> Unit
) {
    //Log.d("selectMember", "SelectMember screen started")
    LaunchedEffect(Unit) {
        viewModel.fetchMembers() // ✅ Fetch members when screen loads
    }

    val memberState by viewModel.memberState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedImageUri by remember { mutableStateOf<String?>(null) }


    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Title
        Text(
            text = "Select Spouse",
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
            is MemberState.Error -> {Text(text = "Error: ${state.message}", color = Color.Red, modifier = Modifier.padding(16.dp)) }
            is MemberState.Empty -> {Text(text = "No members found", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
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
                            SelectMemberSpouseListItem(
                                member = member,
                                onItemClick = {
                                    //onSpouseSelected(member)
                                    // Save the selected spouse object to the savedStateHandle
                                    //navController.previousBackStackEntry?.savedStateHandle?.set("selectedSpouse", member)
                                    viewModel.updateSearchQuery("")
                                    viewModel.setEditableSpouse(member) // Update the ViewModel with the selected spouse
                                    navController.popBackStack()
                                },
                                onImageClick = { selectedImageUri = it }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            else -> {
                Text(text = "Unexpected state", color = Color.Red, modifier = Modifier.padding(16.dp))
            }
        }
    }

    // ✅ Full-screen overlay for image preview
    selectedImageUri?.let { uri ->
        FullScreenImageOverlaySelectMemberSpouse(imageUri = uri, onDismiss = { selectedImageUri = null })
    }
}

// ✅ Member list item UI
@Composable
fun SelectMemberSpouseListItem(
    member: ThreeGen,
    onItemClick: () -> Unit,
    onImageClick: (String) -> Unit
) {
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
fun FullScreenImageOverlaySelectMemberSpouse(imageUri: String, onDismiss: () -> Unit) {
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
