package com.example.threegen.util

import com.example.threegen.data.ThreeGen

/**
 * Object to store constant values used throughout the application.
 * This helps in avoiding magic numbers and makes future modifications easier.
 */
object Constants {
    /**
     * Request code for picking an image from the gallery.
     * Used in startActivityForResult() or ActivityResultLauncher.
     */
    const val IMAGE_PICK_REQUEST_CODE = 1

    /**
     * Firestore collection name for storing family members' data.
     * Keeping collection names as constants helps avoid typos in Firestore queries.
     */
    const val FIRESTORE_COLLECTION_MEMBERS = "threegen_members"
}

/**
 * Sealed class representing different UI states for member-related operations.
 * This is useful for handling UI state changes in a structured and type-safe manner.
 */
sealed class MemberState {
    /**
     * Represents a loading state when data is being fetched.
     * UI can show a progress indicator when this state is active.
     */
    object Loading : MemberState()

    /**
     * Represents a successful response when a single member is retrieved.
     * @param member - The ThreeGen member object returned from Room/Firestore.
     */
    data class Success(val member: ThreeGen, val parent: ThreeGen?, val spouse: ThreeGen?) : MemberState()

    /**
     * Represents a successful response when multiple members are retrieved.
     * @param members - List of ThreeGen members.
     */
    data class SuccessList(val members: List<ThreeGen>) : MemberState()

    /**
     * Represents an empty state when no data is available.
     * UI can show a "No Data Found" message when this state is active.
     */
    object Empty : MemberState()

    /**
     * Represents an error state with a message describing the issue.
     * UI can display this message in a Snackbar, Toast, or error dialog.
     * @param message - The error message detailing what went wrong.
     */
    data class Error(val message: String) : MemberState()
}









/*
package com.example.threegen.util

import com.example.threegen.data.ThreeGen

object Constants {
    const val REQUEST_CODE = 1
}

sealed class MemberState {
    object Loading : MemberState()
    data class Success(val member: ThreeGen) : MemberState()
    data class SuccessList(val members: List<ThreeGen>) : MemberState()
    object Empty : MemberState()
    data class Error(val message: String) : MemberState()
}

 */




/*   about implimentation of pregress loading data from room and firestore
how to use in in viewmodel for a single member-----------------
class ThreeGenViewModel : ViewModel() {

    private val _memberState = MutableStateFlow<MemberState>(MemberState.Loading)
    val memberState: StateFlow<MemberState> get() = _memberState

    fun getMemberById(memberId: String) {
        viewModelScope.launch {
            _memberState.value = MemberState.Loading
            try {
                val member = repository.getMemberById(memberId) // Fetch from Room/Firestore
                if (member != null) {
                    _memberState.value = MemberState.Success(member)
                } else {
                    _memberState.value = MemberState.Empty
                }
            } catch (e: Exception) {
                _memberState.value = MemberState.Error(e.message ?: "Unknown error")
            }
        }
    }
}


how to use in in UI Screen for single member----------------------------------------
@Composable
fun MemberScreen(viewModel: ThreeGenViewModel) {
    val memberState by viewModel.memberState.collectAsState() // Collect StateFlow

    when (memberState) {
        is MemberState.Loading -> CircularProgressIndicator()
        is MemberState.Empty -> Text("No member found.")
        is MemberState.Error -> Text("Error: ${(memberState as MemberState.Error).message}", color = Color.Red)
        is MemberState.Success -> Text("Member: ${(memberState as MemberState.Success).member.fullName}")
    }
}


how to use in in viewmodel for list of members-------------------------------------------------------------------------------------
class ThreeGenViewModel : ViewModel() {

    private val _memberState = MutableStateFlow<MemberState>(MemberState.Loading)
    val memberState: StateFlow<MemberState> get() = _memberState

    fun fetchAllMembers() {
        viewModelScope.launch {
            _memberState.value = MemberState.Loading
            try {
                val members = repository.getAllMembers() // Fetch list from Room/Firestore
                if (members.isNotEmpty()) {
                    _memberState.value = MemberState.SuccessList(members)
                } else {
                    _memberState.value = MemberState.Empty
                }
            } catch (e: Exception) {
                _memberState.value = MemberState.Error
            }
        }
    }
}

how to use in UI Screen for list--------------------------------------------
@Composable
fun MemberListScreen(viewModel: ThreeGenViewModel) {
    val memberState by viewModel.memberState.collectAsState() // Collect StateFlow

    when (memberState) {
        is MemberState.Loading -> {
            CircularProgressIndicator()
        }
        is MemberState.SuccessList -> {
            // extracts the list of members from the SuccessList state
            val members = (memberState as MemberState.SuccessList).members
            // displays the list of members using the MemberList composable
            MemberList(members)
        }
        is MemberState.Empty -> {
            Text("No members found.", color = Color.Gray, fontSize = 18.sp)
        }
        is MemberState.Error -> {
            Text("An error occurred!", color = Color.Red)
        }
    }
}

@Composable
fun MemberList(members: List<ThreeGen>) {
    LazyColumn {
        items(members) { member ->
            MemberItem(member)
        }
    }
}

@Composable
fun MemberItem(member: ThreeGen) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Name: ${member.fullName}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Age: ${member.age}")
        }
    }
}

 */




/*

How to use filterdata in ViewModel - (Filtered StateFlow)
class ThreeGenViewModel : ViewModel() {
    private val _allMembers = MutableStateFlow<List<ThreeGen>>(emptyList())
    val allMembers: StateFlow<List<ThreeGen>> get() = _allMembers

    private val _filteredMembers = MutableStateFlow<List<ThreeGen>>(emptyList())
    val filteredMembers: StateFlow<List<ThreeGen>> get() = _filteredMembers

    fun fetchAllMembers() {
        viewModelScope.launch {
            val members = repository.getAllMembers()
            _allMembers.value = members
            _filteredMembers.value = members // Default to all
        }
    }

    fun filterMembers(minAge: Int) {
        _filteredMembers.value = _allMembers.value.filter { it.age >= minAge }
    }
}

How to use filterdata in UI Screen - (Filtered List)
Composable - Observing Filtered List

@Composable
fun MemberListScreen(viewModel: ThreeGenViewModel) {
    val filteredMembers by viewModel.filteredMembers.collectAsState()

    Column {
        Button(onClick = { viewModel.filterMembers(18) }) {
            Text("Show Adults Only")
        }
        MemberList(filteredMembers)
    }
}

//Best for: UI-driven filtering without needing a ViewModel update for every small change.
Approach: Remember + Derived State (Best for UI-driven Filtering)

@Composable
fun MemberListScreen(members: List<ThreeGen>) {
    var minAge by remember { mutableStateOf(0) }

    val filteredMembers by remember(minAge, members) {
        derivedStateOf { members.filter { it.age >= minAge } }
    }

    Column {
        Slider(value = minAge.toFloat(), onValueChange = { minAge = it.toInt() }, valueRange = 0f..100f)
        MemberList(filteredMembers)
    }
}

 */