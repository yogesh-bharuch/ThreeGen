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
            Text(text = "Town: ${member.town}")
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}



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