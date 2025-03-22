package com.example.threegen.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.threegen.MainApplication
import com.example.threegen.util.MemberState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ThreeGenViewModel(
    private val dao: ThreeGenDao,
    private val firestore: FirebaseFirestore
) : ViewModel()
{
    private val repository: ThreeGenRepository
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        val threeGenDao = MainApplication.threeGenDatabase.getThreeGenDao()
        repository = ThreeGenRepository(threeGenDao)
    }

    // ✅ Singleton instance for consistent ViewModel reference
    companion object {
        @Volatile
        private var instance: ThreeGenViewModel? = null

        fun getInstance(applicationContext: Context): ThreeGenViewModel {
            return instance ?: synchronized(this) {
                instance ?: createViewModel().also { instance = it }
            }
        }

        private fun createViewModel(): ThreeGenViewModel {
            val dao = MainApplication.threeGenDatabase.getThreeGenDao()
            val firestore = FirebaseFirestore.getInstance()
            return ThreeGenViewModel(dao, firestore)
        }
    }

    // ✅ Holds the full list of members from the database
    private val _threeGenList = MutableStateFlow<List<ThreeGen>>(emptyList())
    val threeGenList: StateFlow<List<ThreeGen>> = _threeGenList
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList()) // ✅ Prevent unnecessary updates

    // ✅ UI State management for the list
    private val _memberState = MutableStateFlow<MemberState>(MemberState.Loading)
    val memberState: StateFlow<MemberState> = _memberState
        .stateIn(viewModelScope, SharingStarted.Lazily, MemberState.Loading) // ✅ Prevent redundant updates

    // ✅ Search query for filtering members
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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
      fun fetchMemberDetails(memberId: String?) {
        viewModelScope.launch {
            Log.d("MemberDetailViewModel", "fetchMemberDetails called with memberId: $memberId") // ✅ Log memberId
            _memberState.value = MemberState.Loading
            Log.d("MemberDetailViewModel", "State updated: Loading") // ✅ Log when loading starts

            try {
                if (memberId.isNullOrEmpty()) {
                    // Return a blank member object for new entries
                    val blankMember = ThreeGen(
                        firstName = "",
                        middleName = null,
                        lastName = "",
                        town = "",
                        shortName = "", // Will be generated later in ViewModel
                        childNumber = null,
                        comment = null,
                        imageUri = null,
                        parentID = null,
                        spouseID = null,
                        createdBy = ""
                    )
                    _memberState.value = MemberState.Success(blankMember, null, null)
                    Log.d("MemberDetailViewModel", "State updated: Success with blank member") // ✅ Log blank member success
                } else {
                    // Fetch existing member for editing
                    val member = repository.getMemberById(memberId).asFlow().firstOrNull()
                    val parent = member?.parentID?.let { repository.getMemberById(it).asFlow().firstOrNull() }
                    val spouse = member?.spouseID?.let { repository.getMemberById(it).asFlow().firstOrNull() }

                    _memberState.value = if (member != null) {
                        Log.d("MemberDetailViewModel", "State updated: Success with existing member") // ✅ Log success
                        MemberState.Success(member, parent, spouse)
                    } else {
                        Log.d("MemberDetailViewModel", "State updated: Empty") // ✅ Log empty state
                        MemberState.Empty
                    }
                }
            } catch (e: Exception) {
                Log.d("MemberDetailViewModel", "State updated: Error - ${e.message}") // ✅ Log error details
                _memberState.value = MemberState.Error(e.message ?: "Unknown error")
            }
        }
    }

    //-----------------------------------
    private val _editableParent = MutableLiveData<ThreeGen?>()
    val editableParent: LiveData<ThreeGen?> get() = _editableParent

    // Function to update the editable parent  used while navigating to select spouse
    fun setEditableParent(membersParent: ThreeGen) {
        _editableParent.value = membersParent
    }

    // Function to clear the editable Spouse (optional) used while navigating back after select
    fun clearEditableParent() {
        _editableParent.value = null
    }

    private val _editableSpouse = MutableLiveData<ThreeGen?>()
    val editableSpouse: LiveData<ThreeGen?> get() = _editableSpouse

    // Function to update the editable spouse used while navigating to select spouse
    fun setEditableSpouse(membersSpouse: ThreeGen) {
        _editableSpouse.value = membersSpouse
    }

    // Function to clear the editable parent (optional)  used while navigating back after select spouse
    fun clearEditableSpouse() {
        _editableSpouse.value = null
    }
    //-----------------------------------

    // ✅ Adds a new member to the database
    fun addThreeGen(firstName: String, middleName: String, lastName: String, town: String, imageUri: String?, parentID: String?, spouseID: String?, childNumber: Int? = 1, comment: String? = null, onResult: (Int) -> Unit) {
        if (firstName.isBlank() || middleName.isBlank() || lastName.isBlank() || town.isBlank()) {
            Log.e("ThreeGenViewModel", "Validation failed: All name fields and town are required")
            onResult(0) // Signal failure
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

            // ✅ Create a new ThreeGen object to insert in local database with formated values and createdby field with current user
            val newMember = ThreeGen(firstName = formattedFirstName, middleName = formattedMiddleName, lastName = formattedLastName, town = formattedTown, shortName = uniqueShortName, imageUri = imageUri, parentID = parentID, spouseID = spouseID, createdAt = System.currentTimeMillis(), syncStatus = SyncStatus.NOT_SYNCED, childNumber = childNumber, comment = comment, createdBy = currentUserId)
            val insertedRows = repository.addThreeGen(newMember)

            withContext(Dispatchers.Main) {
                onResult(insertedRows) // Notify the number of rows inserted
            }
        }
    }

    // new update function
    fun updateMember(memberId: String, firstName: String, middleName: String, lastName: String, town: String, parentID: String?, spouseID: String?, imageUri: String?, childNumber: Int?, comment: String?, onResult: (Int) -> Unit) {
        if (firstName.isBlank() || middleName.isBlank() || lastName.isBlank() || town.isBlank()) {
            Log.e("ThreeGenViewModel", "Validation failed: All name fields and town are required")
            onResult(0)
            return
        }
        Log.d("update member", "updating member from viewmodel before Repo: $memberId")

        viewModelScope.launch(Dispatchers.IO) {
            val member = repository.getMemberByIdSync(memberId) /// ?: return@launch
            if (member == null) {
                onResult(0) // Return 0 if no member is found
                return@launch
            }
            val formattedFirstName = formatName(firstName)
            val formattedMiddleName = formatName(middleName)
            val formattedLastName = formatName(lastName)
            val formattedTown = formatName(town)
            val uniqueShortName = generateUniqueShortName(formattedFirstName, formattedMiddleName, formattedLastName, formattedTown)
            val updatedMember = member.copy(firstName = formattedFirstName, middleName = formattedMiddleName, lastName = formattedLastName, town = formattedTown, parentID = parentID, spouseID = spouseID, shortName = uniqueShortName, imageUri = imageUri, childNumber = childNumber, comment = comment, syncStatus = SyncStatus.UPDATED)
            val updatedRows = repository.updateThreeGen(updatedMember)
            //onResult(updatedRows) // Return the number of updated rows
            // ✅ Switch back to the main thread to show the Toast
            withContext(Dispatchers.Main) {
                onResult(updatedRows)
            }
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

    // ✅  Method to sync local data to Firestore---------------------------------------------------------------------------------------------------------------
    //  with a callback for the result message
    private val _syncMessage = MutableStateFlow("")
    val syncMessage: StateFlow<String> = _syncMessage

    fun syncLocalDataToFirestore(callback: (String) -> Unit) {
        Log.d("FirestoreViewModel", "🔥 From ViewModel: syncLocalDataToFirestore called")

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            val message = "Not authenticated. Sync action skipped."
            callback(message)
            return
        }

        val messages = mutableListOf<String>()

        // ✅ Use runBlocking to wait for all updates before triggering the callback
        runBlocking {
            val allMembers = repository.getAllMembers()

            // ✅ Filter only unsynced members
            val unsyncedMembers = allMembers.filter { it.syncStatus != SyncStatus.SYNCED }

            if (unsyncedMembers.isEmpty()) {
                val noSyncMessage = "No members to sync"
                Log.d("FirestoreViewModel", "🔥 $noSyncMessage")
                callback(noSyncMessage)
                return@runBlocking
            }

            // ✅ Concurrent Firestore updates
            val tasks = unsyncedMembers.map { member ->
                async {
                    updateFirestore(member, messages)
                }
            }

            // ✅ Wait for all Firestore operations to finish
            tasks.awaitAll()
        }

        val resultMessage = messages.joinToString("\n").ifEmpty { "No members to sync" }
        Log.d("FirestoreViewModel", "🔥 Final Sync Message: $resultMessage")

        // ✅ Trigger the callback with the final result
        callback(resultMessage)
    }

    private suspend fun updateFirestore(threeGen: ThreeGen, messages: MutableList<String>) {
        Log.d("FirestoreViewModel", "🔥 Updating member in Firestore: ${threeGen.firstName}")

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
            "createdBy" to threeGen.createdBy,
            "parentID" to threeGen.parentID,
            "spouseID" to threeGen.spouseID
        )

        try {
            // ✅ Sync data to Firestore
            firestore.collection("ThreeGenMembers")
                .document(threeGen.id)
                .set(data)
                .await()

            Log.d("FirestoreViewModel", "🔥 Document successfully updated: ${threeGen.firstName}")
            messages.add("✅ Updated in Firestore: ${threeGen.firstName} ${threeGen.lastName}")

            // ✅ Update local Room sync status
            withContext(Dispatchers.IO) {
                val updatedMember = threeGen.copy(syncStatus = SyncStatus.SYNCED)
                repository.updateThreeGen(updatedMember)
            }

        } catch (e: Exception) {
            Log.e("FirestoreViewModel", "❌ Error updating document: ${e.message}", e)
            messages.add("❌ Failed to update: ${threeGen.firstName} ${threeGen.lastName} - Error: ${e.localizedMessage}")
        }
    }


    //------------------------------------------------------------------------------------------------------------------

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