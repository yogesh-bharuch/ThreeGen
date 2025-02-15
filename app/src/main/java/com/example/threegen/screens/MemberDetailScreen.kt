package com.example.threegen.screens

import android.text.format.DateUtils.formatDateTime
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.threegen.EditMember
import com.example.threegen.MemberDetail
import com.example.threegen.MemberTree
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun MemberDetailScreen(
    memberId:Int,
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier
) {
    // Observe the member data using observeAsState
    val member by viewModel.getMemberById(memberId).observeAsState()
   // Log.d("MemberDetails", "Member ID from member detail screen: ${member?.id}, First Name: ${member?.firstName}, Last Name: ${member?.lastName}")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Page Header
        Text(
            text = "Member Detail",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Member Name
        Text(
            text = "${member?.firstName} ${member?.middleName} ${member?.lastName}",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Profile Image
        if (member?.imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(member?.imageUri),
                contentDescription = "Profile Image",
                modifier = Modifier.size(128.dp)
            )
        } else {
            Log.d("MemberDetailScreen", "Image URI is null or invalid")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Member Details
        Text(text = "Town: ${member?.town}")
        Text(text = "Short Name: ${member?.shortName}")
        //Text(text = "Created At: ${formatDateTime(member?.createdAt)}")

        // Parent Information
        Spacer(modifier = Modifier.height(16.dp))
        member?.parentID?.let { parentId ->
            val parentMember by viewModel.getMemberById(parentId).observeAsState()
            parentMember?.let { parent ->
                Text(text = "Parent: ${parent.firstName} ${parent.middleName} ${parent.lastName}")
            }
        }

        // Spouse Information
        Spacer(modifier = Modifier.height(16.dp))
        member?.spouseID?.let { spouseId ->
            val spouseMember by viewModel.getMemberById(spouseId).observeAsState()
            spouseMember?.let { spouse ->
                Text(text = "Spouse: ${spouse.firstName} ${spouse.middleName} ${spouse.lastName}")
            }
        }

        // Edit and Delete Buttons
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(
                onClick = { navController.navigate(EditMember(id = memberId)) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Edit")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    //viewModel.deleteMember(member)
                   // viewModel.deleteThreeGen(member)
                    member?.let { viewModel.deleteThreeGen(it) }
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = "Delete")
            }
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { navController.navigate(MemberTree(id = memberId)) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Family Tree")
            }
        }
    }
}