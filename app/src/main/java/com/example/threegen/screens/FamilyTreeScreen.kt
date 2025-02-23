package com.example.threegen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.MemberFamilyTree
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun FamilyTreeScreen(
    memberId: Int,
    navController: NavHostController,
    viewModel: ThreeGenViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val members by viewModel.threeGenList.observeAsState(initial = emptyList())
    var zoomedImageUri by remember { mutableStateOf<String?>(null) }

    val parentIds = remember(members) { members.mapNotNull { it.parentID }.toSet() }
    val rootMembers = members.filter { it.parentID == null && it.id in parentIds }

    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(rootMembers) { member ->
                FamilyTreeItem(
                    navController = navController,
                    member = member,
                    members = members,
                    onImageClick = { uri -> zoomedImageUri = uri }
                )
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

@Composable
fun FamilyTreeItem(
    navController: NavController,
    member: ThreeGen,
    members: List<ThreeGen>,
    indent: Int = 0,
    onImageClick: (String) -> Unit
) {
    val backgroundColor = if (indent % 2 == 0) Color.LightGray else Color.White
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(start = (indent + 16).dp, bottom = 8.dp) // Adjusted padding
            .fillMaxWidth()
            .background(color = backgroundColor) // Use the same background color for member and spouse
            .clickable {
                expanded = !expanded
            }
    ) {
        Column(modifier = Modifier.padding(start = 16.dp)) { // Ensure same indent for member and spouse
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(member.imageUri),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp) // Adjusted size to 72.dp
                        .padding(8.dp)
                        .clickable { onImageClick(member.imageUri ?: "") }
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "${member.firstName} ${member.middleName} ${member.lastName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Town: ${member.town}")
                }
            }

            member.spouseID?.let { spouseId ->
                members.find { it.id == spouseId }?.let { spouse ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = backgroundColor) // Use the same background color for spouse
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(spouse.imageUri),
                            contentDescription = "Spouse Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp) // Adjusted size to 72.dp
                                .padding(8.dp)
                                .clickable { onImageClick(spouse.imageUri ?: "") }
                        )
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Spouse: ${spouse.firstName} ${spouse.middleName} ${spouse.lastName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Town: ${spouse.town}")
                        }
                    }
                }
            }
        }

        if (expanded) {
            // Display children recursively
            members.filter { it.parentID == member.id }.forEach { child ->
                FamilyTreeItem(
                    navController = navController,
                    member = child,
                    members = members,
                    indent = indent + 1,
                    onImageClick = onImageClick
                )
            }
        }
    }
}