package com.example.threegen.screens

import android.app.Application
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.threegen.MemberTree
import com.example.threegen.SelectParent
import com.example.threegen.SelectSpouse
import com.example.threegen.data.NewThreeGenViewModel
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel
import com.example.threegen.util.MemberState
import com.example.threegen.util.formatDateTime
import kotlinx.coroutines.delay

@Composable
fun HomeScreenA(
    memberId: Int,
    navController: NavHostController,
    viewModel: NewThreeGenViewModel,
    modifier: Modifier = Modifier
) {
    // Fetch member by ID when the Composable is first displayed
    LaunchedEffect(memberId) {
        //delay(1000L) // 1000 milliseconds = 1 second
        viewModel.getMemberById(memberId)
    }

    // Observe the member state using observeAsState
    val memberState by viewModel.memberState.observeAsState(MemberState.Loading)
    val zoomedImageUri by viewModel.zoomedImageUri.observeAsState()


    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        when (memberState) {
            is MemberState.Loading -> {
                // Show loading indicator
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is MemberState.Success -> {
                // Show member details
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    val member = (memberState as MemberState.Success).member
                    item {
                        // Page Header
                        PageHeaderA(member = member)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        // Profile Image
                        AddImageA(
                            navController = navController,
                            viewModel = viewModel,
                            member = member,
                            memberId = memberId,
                            onImageClick = { uri -> viewModel.setZoomedImageUri(uri) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
/*
                    item {
                        // Parent Information
                        ParentInformation1(member = member, viewModel = viewModel) { uri ->
                            viewModel.setZoomedImageUri(uri)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        // Spouse Information
                        SpouseInformation1(member = member, viewModel = viewModel) { uri ->
                            viewModel.setZoomedImageUri(uri)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        // Buttons Edit and Delete
                        Buttons1(
                            navController = navController,
                            viewModel = viewModel,
                            member = member,
                            memberId = memberId
                        )
                    }
                    */
                }

                // Zoomed Image Overlay
                ZoomedImageOverlay(zoomedImageUri) {
                    viewModel.setZoomedImageUri(null)
                }

            }
            is MemberState.Error -> {
                // Show error message
                Text(
                    text = "Failed to load member details.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.displaySmall
                    //style = MaterialTheme.typography.h6
                )
            }
        }
    }
}

@Composable
fun ZoomedImageOverlay(uri: String?, onDismiss: () -> Unit) {
    uri?.let {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Zoomed Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}


@Composable
fun PageHeaderA(member: ThreeGen?, modifier: Modifier = Modifier) {
    Text(
        text = "Member Detail",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        )
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Member Name
    Text(
        text = "${member?.firstName} ${member?.middleName} ${member?.lastName}",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
fun AddImageA(
    navController: NavController,
    viewModel: NewThreeGenViewModel,
    member: ThreeGen?,
    memberId: Int,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    // Observe the member state using observeAsState
    val memberState by viewModel.memberState.observeAsState(MemberState.Loading)

    // State variables for each field
    val firstNameState = remember { mutableStateOf("") }
    val middleNameState = remember { mutableStateOf("") }
    val lastNameState = remember { mutableStateOf("") }
    val shortNameState = remember { mutableStateOf("") }
    val townState = remember { mutableStateOf("") }
    val parentIdState = remember { mutableStateOf(0) }
    val spouseIdState = remember { mutableStateOf(0) }
    val imageUriState = remember { mutableStateOf("") }
    var showError = remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(memberState) {
        if (memberState is MemberState.Success) {
            val member = (memberState as MemberState.Success).member
            firstNameState.value = member.firstName
            middleNameState.value = member.middleName
            lastNameState.value = member.lastName
            shortNameState.value = member.shortName
            townState.value = member.town
            parentIdState.value = member.parentID ?: 0
            spouseIdState.value = member.spouseID ?: 0
            imageUriState.value = member.imageUri ?: ""
        }
    }


    var imageUri = member?.imageUri

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri?.toString()
    }

    val painter: Painter = if (imageUri.isNullOrEmpty()) {
        rememberVectorPainter(image = Icons.Default.Person) // Convert ImageVector to Painter
    } else {
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .crossfade(true)
                .transformations(CircleCropTransformation())
                .build(),
            error = rememberVectorPainter(image = Icons.Default.Person) // Fallback to placeholder if image not found
        )
    }

    Column(modifier = modifier.padding(0.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(painter = painter, contentDescription = "Profile Image", modifier = modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { imageUri?.let { onImageClick(it) } }, contentScale = ContentScale.Crop
            )
            // Member Details
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                // Text fields for editing member details
                OutlinedTextField(
                    value = firstNameState.value,
                    onValueChange = { firstNameState.value = it },
                    label = { Text("First Name") },
                    isError = showError.value && firstNameState.value.isBlank(),
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))

                OutlinedTextField(
                    value = middleNameState.value,
                    onValueChange = { middleNameState.value = it },
                    label = { Text("Middle Name") },
                    isError = showError.value && middleNameState.value.isBlank(),
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))

                OutlinedTextField(
                    value = lastNameState.value,
                    onValueChange = { lastNameState.value = it },
                    label = { Text("Last Name") },
                    isError = showError.value && lastNameState.value.isBlank(),
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = townState.value,
                    onValueChange = { townState.value = it },
                    label = { Text("Town") },
                    isError = showError.value && townState.value.isBlank(),
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Short Name: ${member?.shortName ?: "N/A"}", fontSize = 10.sp)
            }
        }
    }
    // Button to transfer onClick image action
    Button(
        onClick = { imagePickerLauncher.launch("image/*") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(text = "Change Image")
    }

    // Save Button
    Button(
        onClick = {
            // Save the edited member details
            if (firstNameState.value.isBlank() || middleNameState.value.isBlank() || lastNameState.value.isBlank() || townState.value.isBlank()) {
                showError.value = true
            } else {
                showError.value = false
                /*viewModel.updateMember(
                    memberId,
                    firstNameState.value.trim(),
                    middleNameState.value.trim(),
                    lastNameState.value.trim(),
                    townState.value.trim(),
                    shortName = member?.shortName ?: "",
                    imageUri = imageUriState ?: ""
                )*/
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        Text(text = "Save")
    }

    Text(
        text = "Created At: ${formatDateTime(member?.createdAt)}",
        fontSize = 8.sp
    )
}
/*


    Column(modifier = modifier.padding(0.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profile Image
            if (imageUriState != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUriState),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { imageUriState?.let { uri -> onImageClick(uri) } }
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "No Profile Image",
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { imagePickerLauncher.launch("image/*") }
                )
            }

            // Member Details
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
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
                Text(text = "Short Name: ${member?.shortName ?: "N/A"}", fontSize = 10.sp)
            }
        }

        // Button to transfer onClick image action
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(text = "Change Image")
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
                        shortName = member?.shortName ?: "",
                        imageUri = imageUriState ?: ""
                    )
                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
        ) {
            Text(text = "Save")
        }

        Text(
            text = "Created At: ${formatDateTime(member?.createdAt)}",
            fontSize = 8.sp
        )
    }
}
*/

/*
@Composable
fun ParentInformation1(member: ThreeGen?, viewModel: NewThreeGenViewModel, onImageClick: (String) -> Unit) {
    HorizontalDivider(thickness = 1.dp, color = Color.Gray)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "No Profile Image",
                        modifier = Modifier.size(56.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Parent: ${parent.firstName} ${parent.middleName} ${parent.lastName}")
                    Text(text = "Town: ${parent.town}")
                }
            }
        }
    }
}
*/

/*
@Composable
fun SpouseInformation1(member: ThreeGen?, viewModel: NewThreeGenViewModel, onImageClick: (String) -> Unit) {
    HorizontalDivider(thickness = 1.dp, color = Color.Gray)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        member?.spouseID?.let { spouseId ->
            val spouseMember by viewModel.getMemberById(spouseId).observeAsState()
            spouseMember?.let { spouse ->
                val imageUri = spouse.imageUri // Local variable to hold imageUri
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(56.dp)
                            .clickable { onImageClick(imageUri) }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "No Profile Image",
                        modifier = Modifier.size(56.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Spouse: ${spouse.firstName} ${spouse.middleName} ${spouse.lastName}")
                    Text(text = "Town: ${spouse.town}")
                }
            }
        }
    }
}
*/

/*
@Composable
fun Buttons1(navController: NavHostController, viewModel: ThreeGenViewModel, member: ThreeGen?, modifier: Modifier = Modifier, memberId: Int) {
    HorizontalDivider(thickness = 1.dp, color = Color.Gray) // Add a divider here

    Spacer(modifier = Modifier.height(16.dp))

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // First Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    member?.let { viewModel.deleteThreeGen(it) }
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = "Delete")
            }

            Button(
                onClick = { navController.navigate(MemberTree(id = memberId)) },
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
*/
 */


/*
@Preview(showBackground = true)
@Composable
fun MemberDetailScreenPreview() {
    MemberDetailScreen(
        memberId = 1,
        navController = rememberNavController(),
        viewModel = remember { NewThreeGenViewModel(Application()) }
    )
}
*/


 */