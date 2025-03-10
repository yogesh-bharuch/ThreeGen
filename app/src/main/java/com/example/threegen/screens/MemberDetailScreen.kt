package com.example.threegen.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.MemberFamilyTree
import com.example.threegen.SelectParent
import com.example.threegen.SelectSpouse
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.util.formatDateTime

@Composable
fun MemberDetailScreen(
    memberId: Int,
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier
) {
    // Observe the member data using observeAsState
    val member by viewModel.getMemberById(memberId).observeAsState()

    // State for zoomed image
    var zoomedImageUri by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Page Header
            item {
                PageHeader(member = member)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Profile Image
            item {
                AddImage(
                    navController = navController,
                    viewModel = viewModel,
                    member = member,
                    memberId = memberId,
                    onImageClick = { uri -> zoomedImageUri = uri }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Buttons Edit and Delete
            item {
                Buttons(
                    navController = navController,
                    viewModel = viewModel,
                    member = member,
                    memberId = memberId
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
                Image(painter = rememberAsyncImagePainter(uri), contentDescription = "Zoomed Image", modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp))
            }
        }
    }
}

@Composable
fun PageHeader(member: ThreeGen?, modifier: Modifier = Modifier) {
    Text(
        text = "Member Detail",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        )
    )
    Spacer(modifier = Modifier.height(16.dp))

    // Member Name
    Text(
        text = "${member?.firstName} ${member?.middleName} ${member?.lastName}",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
fun AddImage(navController: NavHostController, viewModel: ThreeGenViewModel, member: ThreeGen?, modifier: Modifier = Modifier, memberId: Int, onImageClick: (String) -> Unit) {
    // States for text field values
    val firstNameState = remember { mutableStateOf("") }
    val middleNameState = remember { mutableStateOf("") }
    val lastNameState = remember { mutableStateOf("") }
    val townState = remember { mutableStateOf("") }
    val parentIDState = remember { mutableStateOf<Int?>(null) }
    val spouseIDState = remember { mutableStateOf<Int?>(null) }
    var showError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var imageUriState by remember { mutableStateOf<String?>(null) }

    // Update state values when member data changes
    LaunchedEffect(member) {
        member?.let {
            firstNameState.value = it.firstName
            middleNameState.value = it.middleName
            lastNameState.value = it.lastName
            townState.value = it.town
            parentIDState.value = it.parentID
            spouseIDState.value = it.spouseID
            imageUriState = it.imageUri
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUriState = uri?.toString()
    }

    Column(modifier = modifier.padding(0.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profile Image
            if (imageUriState != null) {
                Image(painter = rememberAsyncImagePainter(imageUriState), contentDescription = "Profile Image", modifier = Modifier
                        .size(100.dp)
                        .clickable { imageUriState?.let { uri -> onImageClick(uri) } })
            } else {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "No Profile Image", modifier = Modifier
                        .size(100.dp)
                        .clickable { imagePickerLauncher.launch("image/*") })
            }

            // Member Details
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                // Text fields for editing member details
                OutlinedTextField(
                    value = firstNameState.value,
                    onValueChange = { firstNameState.value = it },
                    label = { Text("First Name") },
                    isError = showError && firstNameState.value.isBlank(),
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = middleNameState.value,
                    onValueChange = { middleNameState.value = it },
                    label = { Text("Middle Name") },
                    isError = showError && middleNameState.value.isBlank(),
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastNameState.value,
                    onValueChange = { lastNameState.value = it },
                    label = { Text("Last Name") },
                    isError = showError && lastNameState.value.isBlank(),
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = townState.value,
                    onValueChange = { townState.value = it },
                    label = { Text("Town") },
                    isError = showError && townState.value.isBlank(),
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row {
                    Text(text = "Short Name: ${member?.shortName ?: "N/A"}", fontSize = 10.sp, modifier = Modifier.weight(0.7f))
                    Text(text = "ID: ${member?.id ?: "N/A"}", fontSize = 10.sp)
                }
            }
        }

        // Button change image
        Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)) { Text(text = "Change Image") }

        // Save Button
        Button(onClick = {
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
                        parentIDState.value,
                        spouseIDState.value,
                        shortName = member?.shortName ?: "",
                        imageUri = imageUriState ?: ""
                    )
                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)) { Text(text = "Save") }

        // created at text
        Text(text = "Created At: ${formatDateTime(member?.createdAt)}", fontSize = 8.sp)

        // ParentInformation
        HorizontalDivider(thickness = 1.dp, color = Color.Gray)
        Text(text = "Parent:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            member?.parentID?.let { parentId ->
                val parentMember by viewModel.getMemberById(parentId).observeAsState()
                parentMember?.let { parent ->
                    val imageUri = parent.imageUri // Local variable to hold imageUri

                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(56.dp)
                                .clickable { onImageClick(imageUri) }
                        )
                    } else {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "No Profile Image", modifier = Modifier.size(56.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    // parent name
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(text = "${parent.firstName} ${parent.middleName} ${parent.lastName}", modifier = Modifier.weight(1f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { parentIDState.value = null }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                        Text(text = "Town: ${parent.town}      ID: ${parent.id}", fontSize = 14.sp)
                    }
                }
            }
        }

        // Spouse Information
        HorizontalDivider(thickness = 1.dp, color = Color.Gray)
        Text(text = "Spouse:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            member?.spouseID?.let { spouseId ->
                val spouseMember by viewModel.getMemberById(spouseId).observeAsState()
                spouseMember?.let { spouse ->
                    val imageUri = spouse.imageUri // Local variable to hold imageUri
                    if (imageUri != null) {
                        Image(painter = rememberAsyncImagePainter(imageUri), contentDescription = "Profile Image", modifier = Modifier.size(56.dp).clickable { onImageClick(imageUri) })
                    } else { Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "No Profile Image", modifier = Modifier.size(56.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(0.dp)) {
                            Text(text = "${spouse.firstName} ${spouse.middleName} ${spouse.lastName}", modifier = Modifier.weight(1f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { spouseIDState.value = null }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                        Text(text = "Town: ${spouse.town}      ID: ${spouse.id}", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun Buttons(navController: NavHostController, viewModel: ThreeGenViewModel, member: ThreeGen?, modifier: Modifier = Modifier, memberId: Int) {
    HorizontalDivider(thickness = 1.dp, color = Color.Gray) // Add a divider here

    Spacer(modifier = Modifier.height(16.dp))

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // First Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DeleteButton(member, viewModel, navController)
            Button(
                onClick = { navController.navigate(MemberFamilyTree(id = member!!.id)) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Family Tree")
            }
        }

        // Second Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Select Parent Button
            Button(
                onClick = { navController.navigate(SelectParent(id = memberId)) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Change Parent")
            }

            // Select Spouse Button
            Button(
                onClick = { navController.navigate(SelectSpouse(id = memberId)) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Change Spouse")
            }
        }
    }
}

@Composable
fun DeleteButton(member: ThreeGen?, viewModel: ThreeGenViewModel, navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Button(
        onClick = { showDialog = true }, // 🔹 Show confirmation dialog
        //modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
    ) {
        Text(text = "Delete Member")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false }, // 🔹 Close dialog on dismiss
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this member? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        member?.let { viewModel.deleteThreeGen(it) } // 🔹 Delete the member
                        // 🔹 Save action (if needed)
                        Toast.makeText(context, "Member Deleted", Toast.LENGTH_SHORT).show()
                        navController.popBackStack() // 🔹 Navigate back
                    }
                ) {
                    Text("Yes, Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false } // 🔹 Cancel delete
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
