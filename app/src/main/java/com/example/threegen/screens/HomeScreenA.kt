package com.example.threegen.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun HomeScreenA(navController: NavHostController,
                viewModel: ThreeGenViewModel,
                memberId: Int,
                modifier: Modifier = Modifier
) {
    var imageUri by remember { mutableStateOf<String?>(null) }
    // Observe the member data using observeAsState
    val member by viewModel.getMemberById(memberId).observeAsState()

    // Update state values when member data changes
    LaunchedEffect(member) {
        member?.let {
            imageUri = it.imageUri
        }
    }
    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri?.toString()
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Add New Member")

        // Image Picker Button
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Select Profile Image")
        }

        // Display or Select Image
        Spacer(modifier = Modifier.height(8.dp))
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(128.dp)
                    .clickable { imagePickerLauncher.launch("image/*") }
            )
        } ?: Text(
            text = "Select Profile Image",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { imagePickerLauncher.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save member button
        Button(
            onClick = {
                    imageUri?.let {
                        viewModel.updateImageUri(
                            memberId = memberId,
                            imageUri = it
                        )
                    }
                    //navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save Member")
        }
    }
}


    /*
    Text("Home screen A")
    Button(onClick = {
        navController.navigate(HomeB(id = 10))
    })  {
        Text("Go to Home B")
    }

     */
