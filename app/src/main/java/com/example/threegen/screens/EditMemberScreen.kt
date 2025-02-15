package com.example.threegen.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.threegen.SelectParent
import com.example.threegen.SelectSpouse
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun EditMemberScreen(
    memberId:Int,
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier
) {
    // Observe the member data using observeAsState
    val member by viewModel.getMemberById(memberId).observeAsState()
    val parent by viewModel.getMemberById(member?.parentID ?: 0).observeAsState()
    val spouse by viewModel.getMemberById(member?.spouseID ?: 0).observeAsState()
    Log.d("MemberDetails", "parent detail: First Name: ${parent?.firstName}, Last Name: ${parent?.lastName}")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Edit Member Data",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(text = "First Name: ${member?.firstName}")
        Text(text = "Middle Name: ${member?.middleName}")
        Text(text = "Last Name: ${member?.lastName}")
        Text(text = "Town: ${member?.town}")
        Text(text = "ParentId: ${member?.parentID}")
        Text(text = "Parent: ${parent?.firstName} ${parent?.middleName} ${parent?.lastName}")
        Text(text = "Parent Town: ${parent?.town}")
        Text(text = "SpouseId: ${member?.spouseID}")
        Text(text = "Spouse: ${spouse?.firstName} ${spouse?.middleName} ${spouse?.lastName}")
        Text(text = "Spouse Town: ${spouse?.town}")

        // Parent and Spouse Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Select Parent Button
            Button(
                onClick = { navController.navigate(SelectParent(id = memberId)) },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(text = "Change Parent")
            }
            // Select Spouse Button
            Button(
                onClick = { navController.navigate(SelectSpouse(id = memberId)) },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(text = "Add Spouse")
            }
        }

    }

    }