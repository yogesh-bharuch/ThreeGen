package com.example.threegen.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun AddMemberScreen(
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier
) {
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var townName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    val parentID by remember { mutableStateOf<String?>(null) }
    val spouseID by remember { mutableStateOf<String?>(null) }
    var childNumber by remember { mutableStateOf<Int?>(null) }
    var comment by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showImageOverlay by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri?.toString()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Add New Member",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // OutlinedTextField Modifier with Normal Height & Larger Font Size
        val textFieldModifier = Modifier.fillMaxWidth()
        val textStyle = TextStyle(fontSize = 16.sp)

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name", fontSize = 10.sp) }, // Label at 10.sp
            textStyle = textStyle,
            isError = showError && firstName.isBlank(),
            modifier = textFieldModifier
        )

       // Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = middleName,
            onValueChange = { middleName = it },
            label = { Text("Middle Name", fontSize = 10.sp) },
            textStyle = textStyle,
            isError = showError && middleName.isBlank(),
            modifier = textFieldModifier
        )

        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name", fontSize = 10.sp) },
            textStyle = textStyle,
            isError = showError && lastName.isBlank(),
            modifier = textFieldModifier
        )

       // Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = townName,
            onValueChange = { townName = it },
            label = { Text("Town", fontSize = 10.sp) },
            textStyle = textStyle,
            isError = showError && townName.isBlank(),
            modifier = textFieldModifier
        )

      //  Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = childNumber?.toString() ?: "",
            onValueChange = { childNumber = it.toIntOrNull() },
            label = { Text("Child Number of his parent", fontSize = 10.sp) },
            textStyle = textStyle,
            modifier = textFieldModifier
        )

       // Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comment (Optional)", fontSize = 10.sp) },
            textStyle = textStyle,
            modifier = textFieldModifier
        )

      //  Spacer(modifier = Modifier.height(16.dp))

        // Profile Image (Circular & Clickable for Full-Screen Overlay)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.2f))
                .clickable { showImageOverlay = true }
        ) {
            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } ?: Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default Profile",
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Select Profile Image Button
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Select Profile Image")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save Button
        Button(
            onClick = {
                if (firstName.isBlank() || middleName.isBlank() || lastName.isBlank() || townName.isBlank()) {
                    showError = true
                } else {
                    showError = false
                    viewModel.addThreeGen(
                        firstName = firstName.trim(),
                        middleName = middleName.trim(),
                        lastName = lastName.trim(),
                        town = townName.trim(),
                        imageUri = imageUri,
                        parentID = parentID,
                        spouseID = spouseID,
                        childNumber = childNumber,
                        comment = comment.takeIf { it.isNotBlank() }
                    )
                    Toast.makeText(context, "Member added successfully!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save Member")
        }
    }

    // Full-Screen Image Overlay (Click to Close)
    if (showImageOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { showImageOverlay = false },
            contentAlignment = Alignment.Center
        ) {
            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Full Screen Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp), // Prevents image from touching screen edges
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}




/*
package com.example.threegen.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.threegen.SelectParent
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun AddMemberScreen(
    navController: NavHostController,
    viewModel: ThreeGenViewModel,
    modifier: Modifier = Modifier

) {
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var townName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var parentID by remember { mutableStateOf<Int?>(null) }
    var spouseID by remember { mutableStateOf<Int?>(null) }
    var showError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    //var isMemberSaved by remember { mutableStateOf(false) }
    //var newMemberID by remember { mutableStateOf(Int) }


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
        Text(
            text = "Add New Member",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // First Name Field
        TextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            isError = showError && firstName.isBlank(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth()
        )

        // Middle Name Field
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = middleName,
            onValueChange = { middleName = it },
            label = { Text("Middle Name") },
            isError = showError && middleName.isBlank(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth()
        )

        // Last Name Field
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            isError = showError && lastName.isBlank(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth()
        )

        // Town Name Field
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = townName,
            onValueChange = { townName = it },
            label = { Text("Town") },
            isError = showError && townName.isBlank(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth()
        )

        // Image Picker Button
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Select Profile Image")
        }

        // Display Selected Image
        imageUri?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Profile Image",
                modifier = Modifier.size(128.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Save member button
        Button(
            onClick = {
                if (firstName.isBlank() || middleName.isBlank() || lastName.isBlank() || townName.isBlank()) {
                    showError = true
                } else {
                    showError = false
                    viewModel.addThreeGen(
                        firstName = firstName.trim(),
                        middleName = middleName.trim(),
                        lastName = lastName.trim(),
                        town = townName.trim(),
                        imageUri = imageUri,
                        parentID = parentID,
                        spouseID = spouseID
                    )
                    //isMemberSaved = true
                    Toast.makeText(context, "Member added successfully!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save Member")
        }
    }
}

*/

/*
// Parent and Spouse Selection
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    horizontalArrangement = Arrangement.SpaceEvenly
) {
    // Select Parent Button
    Button(
        onClick = { /*navController.navigate(SelectParent(id = memberId))*/ },
        modifier = Modifier
            .weight(1f)
            .padding(8.dp),
        enabled = isMemberSaved
    ) {
        Text(text = "Add Parent")
    }
    // Select Spouse Button
    Button(
        onClick = { /*navController.navigate("selectSpouse")*/ },
        modifier = Modifier
            .weight(1f)
            .padding(8.dp),
        enabled = isMemberSaved
    ) {
        Text(text = "Add Spouse")
    }
}
Spacer(modifier = Modifier.height(16.dp))
*/


 */