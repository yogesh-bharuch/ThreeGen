package com.example.threegen.screens

import android.util.Log
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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun MemberFamilyTreeScreen(
    memberId: Int,
    navController: NavHostController,
    viewModel: ThreeGenViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    Log.d("MemberFamilyTreeScreen", "memberId: $memberId")
    val members by viewModel.threeGenList.observeAsState(initial = emptyList())
    val member = members.find { it.id == memberId }
    var zoomedImageUri by remember { mutableStateOf<String?>(null) }

    val rootMember = findRootMember(member, members)
    val descendants = rootMember?.let { findAllDescendants(it, members) } ?: emptyList()

    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        rootMember?.let {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = "Family Tree of ${rootMember.firstName} ${rootMember.middleName} ${rootMember.lastName}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                item {
                    CollapsibleFamilyTreeItem(navController = navController, member = rootMember, members = members, onImageClick = { uri -> zoomedImageUri = uri })
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

@Composable
fun CollapsibleFamilyTreeItem(
    navController: NavHostController,
    member: ThreeGen,
    members: List<ThreeGen>,
    indent: Int = 0,
    onImageClick: (String) -> Unit
) {
    val backgroundColor = if (indent % 2 == 0) Color.LightGray else Color.White
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(start = (indent + 16).dp, bottom = 8.dp)
            .fillMaxWidth()
            .background(color = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(member.imageUri),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
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
                            .background(color = backgroundColor)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(spouse.imageUri),
                            contentDescription = "Spouse Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
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
            // Display children recursively when expanded
            members.filter { it.parentID == member.id }.forEach { child ->
                CollapsibleFamilyTreeItem(navController = navController, member = child, members = members, indent = indent + 1, onImageClick = onImageClick)
            }
        }
    }
}

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



















/*
package com.example.threegen.screens

import android.util.Log
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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun MemberFamilyTreeScreen(
    memberId: Int,
    navController: NavHostController,
    viewModel: ThreeGenViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    Log.d("MemberFamilyTreeScreen", "memberId: $memberId")
    val members by viewModel.threeGenList.observeAsState(initial = emptyList())
    val member = members.find { it.id == memberId }
    var zoomedImageUri by remember { mutableStateOf<String?>(null) }

    val rootMember = findRootMember(member, members)
    val descendants = findAllDescendants(rootMember, members)

    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        rootMember?.let {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = "Family Tree of ${rootMember.firstName} ${rootMember.middleName} ${rootMember.lastName}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                items(descendants) { descendant ->
                    CollapsibleFamilyTreeItem(navController = navController, member = descendant, members = members, onImageClick = { uri -> zoomedImageUri = uri }, rootMember = rootMember)
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

@Composable
fun CollapsibleFamilyTreeItem(
    navController: NavHostController,
    member: ThreeGen,
    members: List<ThreeGen>,
    indent: Int = 0,
    onImageClick: (String) -> Unit,
    rootMember: ThreeGen
) {
    val backgroundColor = if (indent % 2 == 0) Color.LightGray else Color.White
    var expanded by remember { mutableStateOf(member == rootMember) }

    Column(
        modifier = Modifier
            .padding(start = (indent + 16).dp, bottom = 8.dp)
            .fillMaxWidth()
            .background(color = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .clickable { expanded = !expanded }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(member.imageUri),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
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
                            .background(color = backgroundColor)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(spouse.imageUri),
                            contentDescription = "Spouse Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
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
            // Display children recursively when expanded
            members.filter { it.parentID == member.id }.forEach { child ->
                CollapsibleFamilyTreeItem(navController = navController, member = child, members = members, indent = indent + 1, onImageClick = onImageClick, rootMember = rootMember)
            }
        }
    }
}

fun findRootMember(member: ThreeGen?, members: List<ThreeGen>): ThreeGen? {
    var currentMember = member
    while (currentMember?.parentID != null) {
        currentMember = members.find { it.id == currentMember?.parentID }
    }
    return currentMember
}

fun findAllDescendants(rootMember: ThreeGen?, members: List<ThreeGen>): List<ThreeGen> {
    val descendants = mutableListOf<ThreeGen>()
    fun findDescendants(member: ThreeGen) {
        descendants.add(member)
        members.filter { it.parentID == member.id }.forEach { findDescendants(it) }
    }
    rootMember?.let { findDescendants(it) }
    return descendants
}

*/