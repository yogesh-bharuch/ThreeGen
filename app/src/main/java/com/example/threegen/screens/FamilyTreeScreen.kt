package com.example.threegen.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.threegen.data.MemberAndSpouseData
import com.example.threegen.data.ThreeGen
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun FamilyTreeScreen(memberId: Int,
                     navController: NavHostController,
                     viewModel: ThreeGenViewModel,
                     modifier: Modifier = Modifier) {
    val memberAndSpouseData = viewModel.memberAndSpouseData.observeAsState()
    val siblingsData = viewModel.siblingsData.observeAsState()

    viewModel.getMemberAndSpouseData(memberId)
    viewModel.getSiblings(memberId)

    Column(modifier = modifier.padding(16.dp)) {
        memberAndSpouseData.value?.let { data ->
            FamilyTreeContent(data)
        }

        Spacer(modifier = Modifier.height(16.dp))

        siblingsData.value?.let { siblings ->
            Log.d("FamilyTreeScreen", "Displaying ${siblings.size} siblings")

            Text(text = "Siblings:")
            siblings.forEach { sibling ->
                SiblingItem(sibling)
            }
        }
        Log.d("FamilyTreeScreen", "Displaying size siblings")
    }
}

@Composable
fun FamilyTreeContent(data: MemberAndSpouseData) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            data.memberImageUri?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Member Image",
                    modifier = Modifier
                        .size(128.dp)
                        .padding(end = 16.dp), // Padding between image and text
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Name: ${data.memberFullName}")
                Text(text = "Town: ${data.memberTown}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            data.spouseImageUri?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Spouse Image",
                    modifier = Modifier
                        .size(128.dp)
                        .padding(end = 16.dp), // Padding between image and text
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Name: ${data.spouseFullName}")
                Text(text = "Town: ${data.spouseTown}")
            }
        }
    }
}

@Composable
fun SiblingItem(sibling: ThreeGen) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        sibling.imageUri?.let {
            Image(
                painter = rememberImagePainter(it),
                contentDescription = "Sibling Image",
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Crop
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Name: ${sibling.firstName} ${sibling.middleName} ${sibling.lastName}")
            Text(text = "Town: ${sibling.town}")
        }
    }
}


/*
package com.example.threegen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.threegen.data.MemberAndSpouseData
import com.example.threegen.data.ThreeGenViewModel

@Composable
fun FamilyTreeScreen(memberId: Int,
                     navController: NavHostController,
                     viewModel: ThreeGenViewModel,
                     modifier: Modifier = Modifier) {
    val memberAndSpouseData = viewModel.memberAndSpouseData.observeAsState()

    viewModel.getMemberAndSpouseData(memberId)

    memberAndSpouseData.value?.let { data ->
        FamilyTreeContent(data)
    }
}

@Composable
fun FamilyTreeContent(data: MemberAndSpouseData) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            data.memberImageUri?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Member Image",
                    modifier = Modifier
                        .size(128.dp)
                        .padding(end = 16.dp), // Padding between image and text
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Name: ${data.memberFullName}")
                Text(text = "Town: ${data.memberTown}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            data.spouseImageUri?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Spouse Image",
                    modifier = Modifier
                        .size(128.dp)
                        .padding(end = 16.dp), // Padding between image and text
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Name: ${data.spouseFullName}")
                Text(text = "Town: ${data.spouseTown}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FamilyTreePreview() {
    val sampleData = MemberAndSpouseData(
        memberFullName = "John Doe",
        memberTown = "Springfield",
        memberImageUri = null,
        spouseFullName = "Jane Doe",
        spouseTown = "Springfield",
        spouseImageUri = null
    )
    FamilyTreeContent(data = sampleData)
}


 */