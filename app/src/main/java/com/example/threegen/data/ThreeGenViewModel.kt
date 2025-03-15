package com.example.threegen.data

import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.threegen.MainApplication
import com.example.threegen.util.MemberState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThreeGenViewModel(
    private val dao: ThreeGenDao,
    private val firestore: FirebaseFirestore
) : ViewModel()
{
    private val repository: ThreeGenRepository
    //private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
   // private val firestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val collectionRef = firestore.collection("ThreeGenMembers")

    init {
        val threeGenDao = MainApplication.threeGenDatabase.getThreeGenDao()
        repository = ThreeGenRepository(threeGenDao)
    }

    // ✅ Holds the full list of members from the database
    private val _threeGenList = MutableStateFlow<List<ThreeGen>>(emptyList())
    val threeGenList: StateFlow<List<ThreeGen>> = _threeGenList
        // ✅ Prevent unnecessary updates
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    //--------------------------
    // Convert Flow to LiveData for Compose UI
    val allMembersLiveData: LiveData<List<ThreeGen>> = threeGenList.asLiveData()
    /*// Function to fetch members and update LiveData
    fun fetchThreeGenList() {
        viewModelScope.launch {
            try {
                val members = repository.getAllMembers()
                _threeGenList.value = members
            } catch (e: Exception) {
                Log.e("MemberViewModel", "Error fetching members: ${e.message}")
            }
        }
    }*/
    //--------------------------

    // ✅ UI State management for the list
    private val _memberState = MutableStateFlow<MemberState>(MemberState.Loading)
    val memberState: StateFlow<MemberState> = _memberState
        // ✅ Prevent redundant updates
        .stateIn(viewModelScope, SharingStarted.Lazily, MemberState.Loading)

    // ✅ Search query for filtering members
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ✅ Ensure filtering is applied correctly
    @OptIn(FlowPreview::class)
    val filteredMembers: StateFlow<List<ThreeGen>> = _searchQuery
        .debounce(300) // Prevents excessive updates while typing
        .combine(_threeGenList) { query, list ->
            if (query.isBlank()) list else list.filter { it.shortName.contains(query, ignoreCase = true) }
        }
        .distinctUntilChanged() // ✅ Avoid recompositions if the result is the same
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ✅ Fetches members and updates UI state
    fun fetchMembers() {
        viewModelScope.launch {
            _memberState.value = MemberState.Loading
            try {
                val members = repository.getAllMembers()
                if (_threeGenList.value != members) { // ✅ Update only if the data has changed
                    _threeGenList.value = members
                }
                _memberState.value = if (members.isEmpty()) MemberState.Empty else MemberState.SuccessList(members)
            } catch (e: Exception) {
                _memberState.value = MemberState.Error(e.message ?: "Unknown error")
                Log.e("ThreeGenViewModel", "Error fetching members: ${e.message}")
            }
        }
    }

    // ✅ Updates search query for filtering
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // ✅ Fetches a single member's state
    fun getMemberState(memberId: String): StateFlow<MemberState> {
        val stateFlow = MutableStateFlow<MemberState>(MemberState.Loading)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val member = repository.getMemberByIdSync(memberId)
                val parent = member?.parentID?.let { repository.getMemberByIdSync(it) }
                val spouse = member?.spouseID?.let { repository.getMemberByIdSync(it) }
                withContext(Dispatchers.Main) {
                    //stateFlow.value = member?.let { MemberState.Success(it) } ?: MemberState.Empty
                    if (member != null) {
                        stateFlow.value = MemberState.Success(member, parent, spouse)
                    } else {
                        stateFlow.value = MemberState.Empty
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    stateFlow.value = MemberState.Error(e.message ?: "Unknown error")
                }
            }
        }

        return stateFlow.asStateFlow()
    }

    //private val fetchedMemberIds = mutableSetOf<String>()
    // private val fetchedMemberIds = mutableSetOf<String>()
    fun fetchMemberDetails(memberId: String) {
        viewModelScope.launch {
            _memberState.value = MemberState.Loading
            Log.d("MemberDetailViewModel", "State updated: Loading") // ✅ Log when loading starts

            try {
                val member = repository.getMemberById(memberId).asFlow().firstOrNull()
                val parent = member?.parentID?.let { repository.getMemberById(it).asFlow().firstOrNull() }
                val spouse = member?.spouseID?.let { repository.getMemberById(it).asFlow().firstOrNull() }

                _memberState.value = if (member != null) {
                    Log.d("MemberDetailViewModel", "State updated: Success") // ✅ Log success state
                    MemberState.Success(member, parent, spouse)
                } else {
                    Log.d("MemberDetailViewModel", "State updated: Empty") // ✅ Log empty state
                    MemberState.Empty
                }
            } catch (e: Exception) {
                Log.d("MemberDetailViewModel", "State updated: Error") // ✅ Log error state
                _memberState.value = MemberState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ✅ Adds a new member to the database
    fun addThreeGen(firstName: String, middleName: String, lastName: String, town: String, imageUri: String?, parentID: String?, spouseID: String?, childNumber: Int? = 1, comment: String? = null) {
        if (firstName.isBlank() || middleName.isBlank() || lastName.isBlank() || town.isBlank()) {
            Log.e("ThreeGenViewModel", "Validation failed: All name fields and town are required")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val formattedFirstName = formatName(firstName)
            val formattedMiddleName = formatName(middleName)
            val formattedLastName = formatName(lastName)
            val formattedTown = formatName(town)
            val uniqueShortName = generateUniqueShortName(formattedFirstName, formattedMiddleName, formattedLastName, formattedTown)
            // ✅ Get the current user ID for the createdBy field
            val currentUserId = auth.currentUser?.uid
            //val currentUserId1 = firebase.auth().currentUser.uid;

            // ✅ Create a new ThreeGen object to insert in local database with formated values and createdby field with current user
            val newMember = ThreeGen(firstName = formattedFirstName, middleName = formattedMiddleName, lastName = formattedLastName, town = formattedTown, shortName = uniqueShortName, imageUri = imageUri, parentID = parentID, spouseID = spouseID, createdAt = System.currentTimeMillis(), syncStatus = SyncStatus.NOT_SYNCED, childNumber = childNumber, comment = comment, createdBy = currentUserId)
            // created field with currentfirebaseuser
            //val currentUserId = auth.currentUser?.uid

            repository.addThreeGen(newMember)
            fetchMembers() // ✅ Refresh list after adding a member
        }
    }

    // new update function
    fun updateMember(memberId: String, firstName: String, middleName: String, lastName: String, town: String, parentID: String?, spouseID: String?, imageUri: String?, childNumber: Int?, comment: String?) {
        if (firstName.isBlank() || middleName.isBlank() || lastName.isBlank() || town.isBlank()) {
            Log.e("ThreeGenViewModel", "Validation failed: All name fields and town are required")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val member = repository.getMemberByIdSync(memberId) ?: return@launch
            val formattedFirstName = formatName(firstName)
            val formattedMiddleName = formatName(middleName)
            val formattedLastName = formatName(lastName)
            val formattedTown = formatName(town)
            val uniqueShortName = generateUniqueShortName(formattedFirstName, formattedMiddleName, formattedLastName, formattedTown)
            val updatedMember = member.copy(firstName = formattedFirstName, middleName = formattedMiddleName, lastName = formattedLastName, town = formattedTown, parentID = parentID, spouseID = spouseID, shortName = uniqueShortName, imageUri = imageUri, childNumber = childNumber, comment = comment, syncStatus = SyncStatus.UPDATED)
            Log.d("yogesh", "Updating member with ID from viewmodel before repo call : $memberId to $updatedMember")
            repository.updateThreeGen(updatedMember)

            withContext(Dispatchers.Main) {
                _memberState.value = MemberState.Loading // 🔥 Force UI to refresh

                // 🔥 Fetch the updated member from the database after saving
                val freshMember = repository.getMemberByIdSync(memberId)
                val parent = freshMember?.parentID?.let { repository.getMemberByIdSync(it) }
                val spouse = freshMember?.spouseID?.let { repository.getMemberByIdSync(it) }

                Log.d("yogesh", "Freshly fetched member from viewmodel after repo update call: $freshMember")
                _memberState.value = freshMember?.let {
                    MemberState.Success(it, parent, spouse) //where it is the freshMember passed from the database
                } ?: MemberState.Empty

                Log.d("ViewModel", "State updated to : ${_memberState.value}")
            }

            //fetchMembers() // ✅ Refresh list after updating
        }
    }

    // ✅ Marks a member as deleted
    fun deleteMember(memberId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val member = repository.getMemberByIdSync(memberId)
            if (member != null) {
                repository.deleteThreeGen(member)
                fetchMembers() // ✅ Refresh list after deleting a member
            } else {
                Log.e("ThreeGenViewModel", "Member not found: $memberId")
            }
        }
    }

    // ✅ Generates a unique short name
    private suspend fun generateUniqueShortName(firstName: String, middleName: String, lastName: String, town: String): String {
        val initials = "${firstName.first()}${middleName.first()}${lastName.first()}${town.first()}".uppercase()
        var uniqueShortName = initials
        var count = 1

        while (repository.getShortNameCount(uniqueShortName) > 0) {
            uniqueShortName = "$initials$count"
            count++
        }

        return uniqueShortName
    }

    // ✅ Converts names to Proper Case (First letter uppercase, rest lowercase)
    private fun formatName(name: String): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }

    // Method to sync local data to Firestore with a callback for the result message
    fun syncLocalDataToFirestore(callback: (String) -> Unit) {

        val currentUserId = auth.currentUser?.uid
        //val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUserId == null) {
            Log.d("FirestoreViewModel", "Not authenticated user cannot perform sync action")
            //Toast.makeText(context, "Not authenticated user cannot perform sync action", Toast.LENGTH_LONG).show()
            return
        }
        Log.d("FirestoreViewModel", "Syncing local data to Firestore by: $currentUserId")
        val messages = mutableListOf<String>()
        viewModelScope.launch(Dispatchers.IO) {
            val allMembers = repository.getAllMembers()
            Log.d("FirestoreViewModel", "All members: ${allMembers.size}")
            // update only syncstatus is !SYNCED
            allMembers.forEach { member ->
                if (member.syncStatus != SyncStatus.SYNCED) {
                    Log.d("FirestoreViewModel", "Updating member in Firestore: ${member.firstName}")
                    updateFirestore(member, messages)
                }
            }

            // Return the messages after syncing
            withContext(Dispatchers.Main) {
                callback(messages.joinToString("\n"))
            }
        }
    }

    // called from above syncLocalDataToFirestore
    // Method to update Firestore with member data and collect update messages
    private fun updateFirestore(threeGen: ThreeGen, messages: MutableList<String>) {
        Log.d("FirestoreViewModel", "Updating member in Firestore from updateFirestre function: ${threeGen.firstName}")
        val data = mapOf(
            "id" to threeGen.id,
            "firstName" to threeGen.firstName,
            "middleName" to threeGen.middleName,
            "lastName" to threeGen.lastName,
            "town" to threeGen.town,
            "shortName" to threeGen.shortName,
            "imageUri" to threeGen.imageUri,
            "childNumber" to threeGen.childNumber,
            "comment" to threeGen.comment,
            "createdAt" to threeGen.createdAt,
            //"syncStatus" to threeGen.syncStatus.name,
            //"deleted" to threeGen.deleted,
            "createdBy" to threeGen.createdBy,
            "parentID" to threeGen.parentID,
            "spouseID" to threeGen.spouseID
            //"ownerId" to currentUser?.uid // ✅ Store the owner's UID
        )
        Log.d("FirestoreViewModel", "Data to update: $data")
        val documentPath = firestore.collection("ThreeGenMembers").document(threeGen.id).path
        Log.d("FirestoreViewModel", "Firestore Document Path: $documentPath")


        firestore.collection("ThreeGenMembers")
            .document(threeGen.id)
            .set(data)
            .addOnSuccessListener {
                Log.d("FirestoreViewModel", "DocumentSnapshot successfully updated! $data")
                // Add success message
                messages.add("Updated member: ${threeGen.firstName} ${threeGen.lastName}")
                viewModelScope.launch(Dispatchers.IO) {
                    val updatedMember = threeGen.copy(syncStatus = SyncStatus.SYNCED)
                    repository.updateThreeGen(updatedMember)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreViewModel", "Error updating document: ${e.message}", e)
                messages.add("Failed to update member: ${threeGen.firstName} ${threeGen.lastName} - Error: ${e.localizedMessage}")
            }
    }


    // Method to sync deletions to Firestore
    /*
    fun syncDeletionsToFirestore() {
        viewModelScope.launch(Dispatchers.IO) {
            val deletedMembers = repository.getDeletedMembers()
            deletedMembers.forEach { member ->
                updateFirestore(member)
            }
        }
    }*/

}













/*
package com.example.threegen.data

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.threegen.MainApplication
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThreeGenViewModel : ViewModel() {
    private val repository: ThreeGenRepository
    private val firestoreRepository: ThreeGenFirestoreRepository //  = ThreeGenFirestoreRepository()
    private val db = FirebaseFirestore.getInstance()

    //private lateinit var firestore : FirebaseFirestore
    init {
        val threeGenDao = MainApplication.threeGenDatabase.getThreeGenDao()
        repository = ThreeGenRepository(threeGenDao)
        firestoreRepository  = ThreeGenFirestoreRepository()
    }

    val threeGenList: LiveData<List<ThreeGen>> = repository.allThreeGen

    // Live Flow containing all members
    val allMembers = repository.getAllMembers()

    // StateFlow that filters out members who are not used as a parent or spouse
    val unusedMembers: StateFlow<List<ThreeGen>> = allMembers
        .map { members ->
            val usedAsParentOrSpouse = members.flatMap { listOfNotNull(it.parentID, it.spouseID) }.toSet()

            members.filter {
                it.parentID == null && it.spouseID == null && it.id !in usedAsParentOrSpouse
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    // Add a new ThreeGen entry
    fun addThreeGen(firstName: String, middleName: String, lastName: String, town: String, imageUri: String?, parentID: Int?, spouseID: Int?) {
        // Validate inputs: All fields must be non-empty
        if (firstName.isBlank() || middleName.isBlank() || lastName.isBlank() || town.isBlank()) {
            // Handle validation error (e.g., show a message to the user)
            return
        }

        // Save member to local Room database
        viewModelScope.launch(Dispatchers.IO) {
            val uniqueShortName = generateUniqueShortName(firstName, middleName, lastName, town)
            val newMember = ThreeGen(firstName = firstName, middleName = middleName, lastName = lastName, town = town, shortName = uniqueShortName, imageUri = imageUri, parentID = parentID, spouseID = spouseID, createdAt = System.currentTimeMillis())
            // Save member to local Room database
            val memberId = repository.addThreeGen(newMember)
            newMember.id = memberId.toInt()

            // Sync member with Firestore
            val result = firestoreRepository.addMemberToFirestore(newMember)
            if (result.isFailure) {
                Log.e("FirestoreViewModel", "Failed to add member to Firestore: ${result.exceptionOrNull()?.message}")
            } else {
                Log.d("FirestoreViewModel", "Member added to Firestore successfully ${newMember.id}")
            }
        }
    }

    // Update member
    fun updateMember(memberId: Int, firstName: String, middleName: String, lastName: String, town: String, parentID: Int?, spouseID: Int?, shortName: String, imageUri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val member = repository.getMemberByIdSync(memberId)
            val uniqueShortName = generateUniqueShortName(firstName, middleName, lastName, town)

            Log.d("SelectMemberScreen", "Member: ${member?.firstName}")
            val updatedMember = member?.copy(firstName = firstName, middleName = middleName, lastName = lastName, town = town, parentID = parentID, spouseID = spouseID, shortName = uniqueShortName, imageUri = imageUri)
            Log.d("SelectMemberScreen", "From view model Updating member with ID: $memberId to $updatedMember")
            if (updatedMember != null) {
                repository.updateThreeGen(updatedMember)
                // 🔹 Firestore Sync (Convert ID to String for Firestore)
                Log.d("SelectMemberScreen", "From view model Updating member with ID spouse delete: $memberId to $updatedMember")
                val updates = mutableMapOf<String, Any?>(
                    "firstName" to updatedMember.firstName,
                    "middleName" to updatedMember.middleName,
                    "lastName" to updatedMember.lastName,
                    "town" to updatedMember.town,
                    "shortName" to updatedMember.shortName,
                    "imageUri" to updatedMember.imageUri
                )
                updates.put("parentID", updatedMember.parentID ?: FieldValue.delete())   // 🔥 Fix for Firestore null issue
                updates.put("spouseID", updatedMember.spouseID ?: FieldValue.delete())  // 🔥 Fix for Firestore null issue
                // 🔹 Force `spouseID` update by setting it to `null` explicitly

                val result = firestoreRepository.updateMemberInFirestore(memberId.toString(), updates)
                if (result.isFailure) {
                    Log.e("SelectMemberScreen", "Failed to update member in Firestore: ${result.exceptionOrNull()?.message}")
                } else {
                    Log.d("SelectMemberScreen", "Successfully updated member in Firestore")
                }
            }
        }
    }

    // Generate shortName from the initials of the four fields
    private suspend fun generateUniqueShortName(firstName: String, middleName: String, lastName: String, town: String): String {
        val initials = StringBuilder()
        initials.append(firstName.first().uppercaseChar())
        initials.append(middleName.first().uppercaseChar())
        initials.append(lastName.first().uppercaseChar())
        initials.append(town.first().uppercaseChar())

        var baseShortName = initials.toString()
        var uniqueShortName = baseShortName
        var count = 1

        while (repository.getShortNameCount(uniqueShortName) > 0) {
            uniqueShortName = "$baseShortName$count"
            count++
        }

        return uniqueShortName
    }

    fun getMemberById(id: Int): LiveData<ThreeGen?> { return repository.getMemberById(id) }

    fun deleteThreeGen(threeGen: ThreeGen) {
        viewModelScope.launch(Dispatchers.IO) {
            // Step 1: Find related members
            val spouse = threeGen.spouseID?.let { repository.getMemberByIdSync(it) } // fetches the spouse's details from the Room database.
            Log.d("MyFirestoreViewModel", "Spouse: ${spouse?.firstName}")

            // Step 2: Update Firestore for related members
            if (spouse != null) {
                val spouseUpdate = mapOf("spouseID" to FieldValue.delete()) // 🔹 Remove field instead of setting null
                Log.d("MyFirestoreViewModel", "Updating spouse in Firestore: ${spouse}")
                firestoreRepository.updateMemberInFirestore(spouse.id.toString(), spouseUpdate)
                Log.d("MyFirestoreViewModel", "Spouse updated in Firestore: ${spouse}")
            }
            // Fetch children
            val children = repository.getChildrenByParentId(threeGen.id)
            for (child in children) {
                val childUpdate = mapOf("parentID" to FieldValue.delete()) // 🔹 Remove field instead of setting null
                firestoreRepository.updateMemberInFirestore(child.id.toString(), childUpdate)
            }

            // Step 3: Delete member from Room
            repository.deleteThreeGen(threeGen)

            // Step 4: Delete member from Firestore
            val result = firestoreRepository.deleteMemberFromFirestore(threeGen.id.toString())
            if (result.isFailure) {
                Log.e("FirestoreViewModel", "Failed to delete member from Firestore: ${result.exceptionOrNull()?.message}")
            } else {
                Log.d("FirestoreViewModel", "Member deleted from Firestore successfully: ${threeGen.firstName}")
            }
        }
    }

    /*
    // Delete a ThreeGen entry
    fun deleteThreeGen(threeGen: ThreeGen) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteThreeGen(threeGen)
            val result = firestoreRepository.deleteMemberFromFirestore(threeGen.id.toString())
            if (result.isFailure) {
                Log.e("FirestoreViewModel", "Failed to delete member to Firestore: ${result.exceptionOrNull()?.message}")
            } else {
                Log.d("FirestoreViewModel", "Member deleted from Firestore successfully ${threeGen.firstName}")
            }
        }
    }*/

    // Update the parentId of a member
    fun updateParentId(memberId: Int, parentId: Int) {
        Log.d("SelectMemberScreen", "From view model Updating parent ID for member with ID: $memberId to $parentId")
        viewModelScope.launch(Dispatchers.IO) {
            val member1 = repository.getMemberByIdSync(memberId)
            Log.d("SelectMemberScreen", "Member: $member1.id")
            member1?.let {
                it.parentID = parentId
                repository.updateThreeGen(it)
            }
        }
    }

    // Update the ImageUri of a member
    fun updateImageUri(memberId: Int, imageUri: String) {
        Log.d("SelectMemberScreen", "From view model Updating parent ID for member with ID: $memberId to $imageUri")
        viewModelScope.launch(Dispatchers.IO) {
            val member1 = repository.getMemberByIdSync(memberId)
            Log.d("SelectMemberScreen", "Member: $member1.id")
            member1?.let {
                it.imageUri = imageUri
                repository.updateThreeGen(it)
            }
        }
    }

    // Update the spouse of a member
    fun updateSpouseId(memberId: Int, spouseId: Int) {
        Log.d("SelectMemberScreen", "From view model Updating spouse ID for member with ID: $memberId to $spouseId")
        viewModelScope.launch(Dispatchers.IO) {
            val member1 = repository.getMemberByIdSync(memberId)
            Log.d("SelectMemberScreen", "Member: $member1.id")
            member1?.let {
                it.spouseID = spouseId
                repository.updateThreeGen(it)
            }
        }
    }

    // Retrieve member and spouse data
    private val _memberAndSpouseData = MutableLiveData<MemberAndSpouseData?>()
    val memberAndSpouseData: LiveData<MemberAndSpouseData?> get() = _memberAndSpouseData

    fun getMemberAndSpouseData(memberId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getMemberAndSpouseData(memberId)
            _memberAndSpouseData.postValue(data)
        }
    }

    // Retrieve siblings data
    private val _siblingsData = MutableLiveData<List<ThreeGen>>()
    val siblingsData: LiveData<List<ThreeGen>> get() = _siblingsData

    fun getSiblings(memberId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getSiblings(memberId) // memberId
            Log.d("ThreeGenViewModel", "Retrieved ${data.value?.size} siblings: ${data.value}")
            _siblingsData.postValue(data.value)
        }
    }

    fun addMemberToFirestore(memberId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val member = repository.getMemberByIdSync(memberId)
            if (member != null) {
                val result = firestoreRepository.addMemberToFirestore(member)
                if (result.isSuccess) {
                    Log.d("Firestore", "Successfully added member with ID: $memberId to Firestore")
                } else {
                    Log.e("Firestore", "Failed to add member with ID: $memberId to Firestore: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }
    /*
    fun updateFirestoreWithRoomData(memberId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val member = repository.getMemberByIdSync(memberId)
            Log.d("Firestore", "Member: ${member?.firstName}")
            member?.let {
                val updates: Map<String, Any> = mapOf(
                    "firstName" to it.firstName,
                    "middleName" to it.middleName,
                    "lastName" to it.lastName,
                    "town" to it.town,
                    "parentID" to (it.parentID ?: "") as Any,
                    "spouseID" to (it.spouseID ?: "") as Any,
                    "shortName" to it.shortName,
                    "imageUri" to (it.imageUri ?: "") as Any
                )

                firestoreRepository.updateMemberInFirestore(it.id.toString(), updates)
            }
        }
    }*/

    fun copyDataToFirestore() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val members: List<ThreeGen> = repository.getAllThreeGenAsObject()
                Log.d("Firestore", "threeGenList: ${members.size}")

                for (member in members) {
                    val memberData = hashMapOf(
                        "id" to member.id,
                        "firstName" to member.firstName,
                        "middleName" to member.middleName,
                        "lastName" to member.lastName,
                        "town" to member.town,
                        "parentID" to member.parentID,
                        "spouseID" to member.spouseID,
                        "shortName" to member.shortName
                    )

                    db.collection("Members").document("member${member.id}").set(memberData)
                        .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully written!") }
                        .addOnFailureListener { e -> Log.w("Firestore", "Error writing document", e) }
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error migrating data: ${e.message}")
            }
        }
    }
}


 */