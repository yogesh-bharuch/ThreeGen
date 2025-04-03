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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.Home
import com.example.threegen.MemberFamilyTree
import com.example.threegen.SelectMemberParent
import com.example.threegen.SelectMemberSpouse
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.util.CustomTopBar
import com.example.threegen.util.MemberState
import com.example.threegen.util.SnackbarManager
import com.example.threegen.data.workers.WorkManagerHelper
import com.example.threegen.util.formatDateTime

@Composable
fun MemberDetailScreen(memberId: String, navController: NavHostController, viewModel: ThreeGenViewModel, onNavigateBack: () -> Unit, modifier: Modifier = Modifier)
{
    val memberState by viewModel.memberState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var zoomedImageUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(memberId) {
        //Log.d("MemberDetailScreen", "Calling fetchMemberDetails for ID from launched effect : $memberState")
        viewModel.fetchMemberDetails(memberId)
    }
    Column(modifier = Modifier.fillMaxSize().padding(top = 40.dp).padding(bottom = 40.dp))
    {
        CustomTopBar(title = "Member Details", navController = navController, onBackClick = { navController.navigate(Home) })
        Box(modifier = Modifier.fillMaxSize().padding(1.dp))
        {
            when (val state = memberState) {
                is MemberState.Loading -> LoadingState()
                is MemberState.Empty -> EmptyState() //SuccessList
                is MemberState.Error -> ErrorState(state.message)
                is MemberState.SuccessList -> SuccessList()
                is MemberState.Success -> {
                    val member = state.member
                    val memberParent = state.parent
                    val memberSpouse = state.spouse

                    // ✅ Store editable fields together to optimize recomposition
                    val editableMember = rememberSaveable { mutableStateOf(member) }
                    val editableParent = rememberSaveable { mutableStateOf(memberParent) }
                    val editableSpouse = rememberSaveable { mutableStateOf(memberSpouse) } // Added for spouse detail

                        LazyColumn(modifier = Modifier.fillMaxSize().padding(PaddingValues(top = 1.dp, bottom = 1.dp))
                        ) {
                            //item { PageHeader() }
                            item {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically)
                                {
                                    AddImage(member, editableMember.value.imageUri ?: "", onImageClick = { zoomedImageUri = it }, onImageUriChange = { uri -> editableMember.value = editableMember.value.copy(imageUri = uri) })
                                    Spacer(modifier = Modifier.width(16.dp))
                                    EditableMemberDetails(
                                        firstName = editableMember.value.firstName, onFirstNameChange = { editableMember.value = editableMember.value.copy(firstName = it) },
                                        middleName = editableMember.value.middleName ?: "", onMiddleNameChange = { editableMember.value = editableMember.value.copy(middleName = it) },
                                        lastName = editableMember.value.lastName, onLastNameChange = { editableMember.value = editableMember.value.copy(lastName = it) },
                                        town = editableMember.value.town, onTownChange = { editableMember.value = editableMember.value.copy(town = it) },
                                        childNumber = editableMember.value.childNumber?.toString() ?: "", onChildNumberChange = { editableMember.value = editableMember.value.copy(childNumber = it.toIntOrNull()) },
                                        comment = editableMember.value.comment ?: "", onCommentChange = { editableMember.value = editableMember.value.copy(comment = it) }
                                    )
                                }
                            } // ✅ AddImage, Editable Member Details
                            item {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Created At: ${formatDateTime(member.createdAt)}", fontSize = 8.sp)
                                    Text(text = "Short Name: ${member.shortName}", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    Button(
                                        onClick = {
                                            if (memberId == ""){
                                                viewModel.addThreeGen(firstName = editableMember.value.firstName, middleName = editableMember.value.middleName ?: "", lastName = editableMember.value.lastName, town = editableMember.value.town, parentID = editableMember.value.parentID, spouseID = editableMember.value.spouseID, imageUri = editableMember.value.imageUri, childNumber = editableMember.value.childNumber, comment = editableMember.value.comment) { message, insertedRows ->
                                                    if (insertedRows > 0) {
                                                        SnackbarManager.showMessage("Successfully inserted Row number: $insertedRows row(s)!\n$message")
                                                    } else {
                                                        SnackbarManager.showMessage("Insert failed. No rows were inserted.")
                                                    } // ✅ Snackbar Message
                                                    navController.popBackStack()
                                                }
                                            } else {
                                                viewModel.updateMember(memberId = member.id, firstName = editableMember.value.firstName, middleName = editableMember.value.middleName ?: "", lastName = editableMember.value.lastName, town = editableMember.value.town, parentID = editableMember.value.parentID, spouseID = editableMember.value.spouseID, imageUri = editableMember.value.imageUri, childNumber = editableMember.value.childNumber, comment = editableMember.value.comment){ updatedRows ->
                                                    if (updatedRows > 0) {
                                                        SnackbarManager.showMessage("Successfully updated $updatedRows row(s)!")
                                                    } else {
                                                        SnackbarManager.showMessage("Update failed. No rows were updated.")
                                                    } // ✅ Snackbar Message
                                                }
                                            }
                                            viewModel.clearEditableSpouse() // Clear parent details if parent changed od add
                                            viewModel.clearEditableParent() // Clear parent details if spouse changed od add
                                            WorkManagerHelper.immediateSync(context) // copies local to firestore
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text(text = "Save") }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            // Handle cancel action
                                            navController.popBackStack()
                                            Toast.makeText(context, "Action cancelled!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text(text = "Cancel") }
                                }
                            } // ✅ Created, ShortName, Save Cancel Buttons
                            item {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                ParentDetail(editableMember = editableMember, editableParent = editableParent, navController = navController, viewModel = viewModel, onImageClick = { zoomedImageUri = it })
                            } // ✅ Parent Detail
                            item {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                SpouseDetail(editableMember = editableMember, editableSpouse = editableSpouse, navController = navController, viewModel = viewModel, onImageClick = { zoomedImageUri = it })
                            } // ✅ Spouse Detail
                            item {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                ActionButtons(navController, viewModel, member)
                            } // ✅ Action Buttons Familytree, Delete
                        }
                        // ✅ Image Zoom Overlay
                        zoomedImageUri?.let { ImageOverlay(it) { zoomedImageUri = null } }
                }
            } // when block in member detail screen
        }
    } // Top Most Column in MemberDetailScreen
} // MemberDetailScreen

// ✅ Loading State
@Composable
fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// ✅ Empty State
@Composable
fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Member not found", fontWeight = FontWeight.Bold)
    }
}

// ✅ Error State
@Composable
fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Error: $message", color = Color.Red, fontWeight = FontWeight.Bold)
    }
}

// ✅ SuccessList State
@Composable
fun SuccessList() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Member state changed to SuccessList", fontWeight = FontWeight.Bold)
    }
}

// ✅ Image Zoom Overlay
@Composable
fun ImageOverlay(imageUri: String, onClose: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = "Zoomed Image",
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
    }
}

// ✅ Page Header with Title
@Composable
fun PageHeader() {
    Column {
        Text(
            text = "Member Detail",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ✅ Profile Image Handling
@Composable
fun AddImage(member: ThreeGen, imageUri: String?, onImageClick: (String) -> Unit, onImageUriChange: (String) -> Unit) {
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.toString()?.let {
            onImageUriChange(it) // ✅ Pass updated URI to parent
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable { imageUri?.let { onImageClick(it) } },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.PersonAdd, contentDescription = "No Profile Image", modifier = Modifier.size(40.dp))
            }
        }
        Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.padding(top = 8.dp)) {
            Text(text = "Change")
        }
    }
}

// ✅ Editable Member Details
@Composable
fun EditableMemberDetails(firstName: String, onFirstNameChange: (String) -> Unit, middleName: String, onMiddleNameChange: (String) -> Unit, lastName: String, onLastNameChange: (String) -> Unit, town: String, onTownChange: (String) -> Unit, childNumber: String, onChildNumberChange: (String) -> Unit, comment: String, onCommentChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = firstName, onValueChange = onFirstNameChange, label = { Text("First Name", style = TextStyle(fontSize = 8.sp), fontWeight = FontWeight.Bold) }, textStyle = TextStyle(fontSize = 14.sp), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = middleName, onValueChange = onMiddleNameChange, label = { Text("Middle Name", style = TextStyle(fontSize = 8.sp), fontWeight = FontWeight.Bold) }, textStyle = TextStyle(fontSize = 14.sp), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = lastName, onValueChange = onLastNameChange, label = { Text("Last Name", style = TextStyle(fontSize = 8.sp), fontWeight = FontWeight.Bold) }, textStyle = TextStyle(fontSize = 14.sp), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = town, onValueChange = onTownChange, label = { Text("Town", style = TextStyle(fontSize = 8.sp), fontWeight = FontWeight.Bold) }, textStyle = TextStyle(fontSize = 14.sp), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = childNumber, onValueChange = onChildNumberChange, label = { Text("Child Number of it's Parent", style = TextStyle(fontSize = 8.sp), fontWeight = FontWeight.Bold) }, textStyle = TextStyle(fontSize = 14.sp), modifier =  Modifier.fillMaxWidth())
        OutlinedTextField(value = comment, onValueChange = onCommentChange, label = { Text("Comment", style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold)) }, textStyle = TextStyle(fontSize = 14.sp), modifier = Modifier.fillMaxWidth(), maxLines = 5)
    }
}

// ✅ Parent Section
@Composable
fun ParentDetail(editableMember: MutableState<ThreeGen>, editableParent: MutableState<ThreeGen?>, navController: NavHostController, viewModel: ThreeGenViewModel, onImageClick: (String) -> Unit) {
    // Observing states from ViewModel
    val currentEditableParent by viewModel.editableParent.observeAsState()
    // If a new parent is selected, update editableParent
    currentEditableParent?.let {
        if (it != editableParent.value) {
            editableParent.value = it
            editableMember.value = editableMember.value.copy(parentID = it.id)
            //viewModel.clearEditableParent() // Clear after assigning
        }
    }

    // Main UI Column
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Parent Detail", style = MaterialTheme.typography.titleMedium)
            // Add/Change Parent Button
            Button(onClick = {
                // Navigate to SelectParentScreen to allow reassignment
                //viewModel.clearEditableParent() // Clear the current parent if the user wants to change it
                navController.navigate(SelectMemberParent)
            }, modifier = Modifier.padding(start = 8.dp)) { Text(text = "Add/Change Parent") }
        }
        // Display parent details if parentID is not null
        if (editableMember.value.parentID != null && editableParent.value != null) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // Parent Image
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Gray)) {
                    editableParent.value?.imageUri?.let { imageUri ->
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUri),
                            contentDescription = "Parent Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onImageClick(imageUri) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Parent Details
                Column (modifier=Modifier.weight(1f)){
                    editableParent.value?.let { parent ->
                        Text(text = "${parent.firstName} ${parent.middleName} ${parent.lastName}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = parent.town, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Delete Parent Icon
                IconButton(onClick = {
                    // Clear the parent details in the ViewModel and locally
                    editableMember.value = editableMember.value.copy(parentID = null)
                    editableParent.value = null
                    //viewModel.clearEditableSpouse() // Clear parent details
                    //viewModel.clearEditableParent() // Clear parent details
                }) { Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Parent") }
            }
        }
    }
}

// ✅ Spouse Section
@Composable
fun SpouseDetail(editableMember: MutableState<ThreeGen>, editableSpouse: MutableState<ThreeGen?>, navController: NavHostController, viewModel: ThreeGenViewModel, onImageClick: (String) -> Unit) {
    // Observing states from ViewModel
    val currentEditableSpouse by viewModel.editableSpouse.observeAsState()
    // If a new parent is selected, update editableParent
    currentEditableSpouse?.let {
        if (it != editableSpouse.value) {
            editableSpouse.value = it
            editableMember.value = editableMember.value.copy(spouseID = it.id)
            //viewModel.clearEditableSpouse() // Clear after assigning shifted to save button
        }
    }

    // Main UI Column
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Spouse Detail", style = MaterialTheme.typography.titleMedium)
            // Add/Change Spouse Button
            Button(onClick = {
                // Navigate to SelectParentScreen to allow reassignment
                //viewModel.clearEditableSpouse() // Clear the current parent if the user wants to change it
                navController.navigate(SelectMemberSpouse)
            }, modifier = Modifier.padding(start = 8.dp)) { Text(text = "Add/Change Spouse") }
        }
        // Display spouse details if spouseID is not null
        //(editableMember.value.spouseID != null && editableSpouse.value != null)
        if (editableMember.value.spouseID != null && editableSpouse.value != null) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically)
            {
                // Spouse Image
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Gray)) {
                    // Display parent image if available
                    editableSpouse.value?.imageUri?.let { imageUri ->
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUri),
                            contentDescription = "Parent Image",
                            modifier = Modifier.fillMaxSize().clickable { onImageClick(imageUri) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Spouse Details
                Column(modifier=Modifier.weight(1f)) {
                    // Display parent's full name if available
                    editableSpouse.value?.let { spouse ->
                        Text(text = "${spouse.firstName} ${spouse.middleName} ${spouse.lastName}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = spouse.town, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Delete Spouse Icon
                IconButton(onClick = {
                    // Clear the Spouse details in the ViewModel and locally
                    editableMember.value = editableMember.value.copy(spouseID = null)
                    editableSpouse.value = null // Clear editableSpouse
                    //viewModel.clearEditableSpouse() // Clear parent details
                }) { Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Parent") }
            }
        }
    }
}

// ✅ Buttons for Actions
@Composable
fun ActionButtons(navController: NavHostController, viewModel: ThreeGenViewModel, member: ThreeGen) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // FamilyTree button
            Button(onClick = { navController.navigate(MemberFamilyTree(member.id)) }) { Text(text = "Family Tree") }
            // call delete button
            DeleteButton(member, viewModel, navController)
        }
    }
} // called from lazzy column as item

// ✅ Delete Button with Confirmation
@Composable
fun DeleteButton(member: ThreeGen, viewModel: ThreeGenViewModel, navController: NavHostController) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
    ) {
        Text(text = "Delete Member")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Delete") },
            confirmButton = {
                Button(onClick = {
                    viewModel.markAsDeletedMember(member.id)
                    navController.popBackStack()
                    showDialog = false
                    SnackbarManager.showMessage("Successfully marked as deleted ")
                    WorkManagerHelper.immediateSync(context)
                    //Toast.makeText(context, "Member deleted", Toast.LENGTH_SHORT).show()
                }) { Text("Yes, Delete") }
            },
            dismissButton = { Button(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
} // called from action buttons


