package com.example.threegen.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun SelectMemberScreen(
    memberId:Int,
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier
) {
    val member1 by viewModel.getMemberById(memberId).observeAsState()

    var searchQuery by remember { mutableStateOf("") }
    val allMembers by viewModel.threeGenList.observeAsState(emptyList())
    val filteredMembers = allMembers.filter {
        it.shortName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Name SHORTNAME Field
        Text(
            text = "Search by Short Name YJVT Yogesh Jayendra Vyas Thavad",
            modifier = Modifier.padding(8.dp)
        )
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by Short Name") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.PersonSearch, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(filteredMembers) { member ->
                ListItem(
                    headlineContent = { Text("${member.firstName} ${member.middleName} ${member.lastName} \n Town: ${member.town} ") },
                    supportingContent = { Text("Short Name: ${member.shortName}") },
                        modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Update the parentId using the clicked member's id and member1.id to be updated
                            val currentMemberId = member1?.id
                            currentMemberId?.let {
                                //Log.d("SelectMemberScreen", "Current Member ID: $it Current parent ID: $member.id")
                                viewModel.updateParentId(it, member.id)
                                navController.popBackStack()
                            }
                        }
                )
               // Divider()
            }
        }
    }
}
