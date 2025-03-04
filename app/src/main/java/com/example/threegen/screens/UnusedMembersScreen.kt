package com.example.threegen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.MemberDetail
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.ui.theme.Shapes

@Composable
fun UnusedMembersScreen(
    viewModel: ThreeGenViewModel = viewModel(),
    navController: NavHostController,
    modifier: Modifier
) {
    val unusedMembers by viewModel.unusedMembers.collectAsState()

    if (unusedMembers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No unused members found", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(unusedMembers) { member ->
                UnusedMemberItem(member, navController)
            }
        }
    }
}

@Composable
fun UnusedMemberItem(member: ThreeGen, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate(MemberDetail(id = member.id)) // Navigate with member ID
            },
        shape = Shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!member.imageUri.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(member.imageUri),
                    contentDescription = "Member Image",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(Shapes.small)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "${member.firstName} ${member.lastName}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Town: ${member.town}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
