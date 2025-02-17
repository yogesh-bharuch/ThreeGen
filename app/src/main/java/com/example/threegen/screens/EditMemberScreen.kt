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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.threegen.SelectParent
import com.example.threegen.SelectSpouse
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun EditMemberScreen(
    memberId: Int,
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier
) {
    // Observe the member data using observeAsState
    val member by viewModel.getMemberById(memberId).observeAsState()
    val parent by viewModel.getMemberById(member?.parentID ?: 0).observeAsState()
    val spouse by viewModel.getMemberById(member?.spouseID ?: 0).observeAsState()
    Log.d("MemberDetails", "parent detail: First Name: ${parent?.firstName}, Last Name: ${parent?.lastName}")

    //loadMemberData()
    // States for text field values
    val firstNameState = remember { mutableStateOf("") }
    val middleNameState = remember { mutableStateOf("") }
    val lastNameState = remember { mutableStateOf("") }
    val townState = remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    // Update state values when member data changes
    LaunchedEffect(member) {
        member?.let {
            firstNameState.value = it.firstName
            middleNameState.value = it.middleName
            lastNameState.value = it.lastName
            townState.value = it.town
        }
    }

    Log.d("MemberDetails1", "member detail: First Name: ${member?.firstName}, Last Name: ${member?.lastName}  ${firstNameState.value}")

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

        // Text fields for editing member details
        OutlinedTextField(
            value = firstNameState.value,
            onValueChange = { firstNameState.value = it },
            label = { Text("First Name") },
            isError = showError && firstNameState.value.isBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = middleNameState.value,
            onValueChange = { middleNameState.value = it },
            label = { Text("Middle Name") },
            isError = showError && middleNameState.value.isBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = lastNameState.value,
            onValueChange = { lastNameState.value = it },
            label = { Text("Last Name") },
            isError = showError && lastNameState.value.isBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = townState.value,
            onValueChange = { townState.value = it },
            label = { Text("Town") },
            isError = showError && townState.value.isBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Display parent and spouse details
        Text(text = "Parent: ${parent?.firstName} ${parent?.middleName} ${parent?.lastName}")
        Text(text = "Parent Town: ${parent?.town}")
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

        // Save Button
        Button(
            onClick = {
                // Save the edited member details
                if (firstNameState.value.isBlank() || middleNameState.value.isBlank() || lastNameState.value.isBlank() || townState.value.isBlank()) {
                    showError = true
                } else {
                    showError = false
                viewModel.updateMember(
                    memberId,
                    firstNameState.value.trim(),
                    middleNameState.value.trim(),
                    lastNameState.value.trim(),
                    townState.value.trim(),
                    shortName = member?.shortName ?: ""
                )
                navController.popBackStack() // Navigate back after saving
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Save")
        }
    }
}

@Composable
fun loadMemberData(modifier: Modifier = Modifier) {


}



/*

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


 */