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
    fun markAsDeletedMember(memberId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val member = repository.getMemberByIdSync(memberId)
            if (member != null) {
                repository.markAsdeletedThreeGen(member)
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

        // Launch the coroutine using viewModelScope (if inside ViewModel)
        viewModelScope.launch {
            try {
                // ✅ Fetch all members mark for deleted members will not fetch
                val allMembers = withContext(Dispatchers.IO) {
                    repository.getAllMembers()
                }

                // ✅ Fetch soft-deleted members
                val deletedMembers = withContext(Dispatchers.IO) {
                    repository.getMarkAsDeletedMembers()
                }

                // ✅ Filter only unsynced members
                val unsyncedMembers = allMembers.filter { it.syncStatus != SyncStatus.SYNCED }

                //  ✅ if no unsynced members or no deleted members, return
                if (unsyncedMembers.isEmpty() && deletedMembers.isEmpty()) {
                    val noSyncMessage = "No members to sync"
                    Log.d("FirestoreViewModel", "🔥 $noSyncMessage")
                    callback(noSyncMessage)
                    return@launch
                }

                // ✅ Delete members marked as deleted
                val deleteTasks = if (deletedMembers.isNotEmpty()) {
                    deletedMembers.map { member ->
                        async { deleteFirestoreMember(member, messages) }
                    }
                } else { emptyList() }

                // ✅ Remove references as parent or spouse for deleted members from firestore, local database refrential intigrity takes care
                val cleanUpTasks = if (deletedMembers.isNotEmpty()) {
                    deletedMembers.map { member ->
                        async { removeReferencesToDeletedMember(member.id, messages) }
                    }
                } else { emptyList() }

                // ✅ Sync unsynced members to Firestore updated members
                val syncTasks = if (unsyncedMembers.isNotEmpty()) {
                    unsyncedMembers.map { member ->
                        async { updateFirestore(member, messages) }
                    }
                } else { emptyList() }

                // ✅ Wait for all operations to complete
                deleteTasks.awaitAll()
                cleanUpTasks.awaitAll()
                syncTasks.awaitAll()

                // Prepare final result message
                val resultMessage = messages.joinToString("\n").ifEmpty { "No members to sync" }
                Log.d("FirestoreViewModel", "🔥 Final Sync Message: $resultMessage")

                // Trigger the callback with the final result
                callback(resultMessage)
            } catch (e: Exception) {
                Log.e("FirestoreViewModel", "❌ Error during sync operation: ${e.message}", e)
                callback("❌ Error during sync: ${e.message}")
            }
        }
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

    private suspend fun deleteFirestoreMember(member: ThreeGen, messages: MutableList<String>) {
        try {
            // ✅ Delete the member document in Firestore
            firestore.collection("ThreeGenMembers")
                .document(member.id)
                .delete()
                .await()

            Log.d("FirestoreViewModel", "🔥 Deleted Firestore Member: ${member.firstName}")
            messages.add("🔥 Deleted in Firestore: ${member.firstName} ${member.lastName}")

            // ✅ Remove the member from local Room database markedasdeleted
            withContext(Dispatchers.IO) {
                repository.deleteThreeGen(member)
            }

        } catch (e: Exception) {
            Log.e("FirestoreViewModel", "❌ Error deleting Firestore member: ${e.message}", e)
            messages.add("❌ Failed to delete: ${member.firstName} - ${e.localizedMessage}")
        }
    }

    private suspend fun removeReferencesToDeletedMember(deletedMemberId: String, messages: MutableList<String>) {
        try {
            Log.d("FirestoreViewModel", "🔥 Removing references to deleted member ID: $deletedMemberId")

            val batch = firestore.batch()

            // ✅ Find members referencing the deleted member
            val query = firestore.collection("ThreeGenMembers")
                .whereEqualTo("parentID", deletedMemberId)
                .get()
                .await()

            query.documents.forEach { doc ->
                batch.update(doc.reference, "parentID", null)
                Log.d("FirestoreViewModel", "✅ Cleared parentID reference for: ${doc.id}")
                messages.add("✅ Removed parent reference for: ${doc.id}")
            }

            val spouseQuery = firestore.collection("ThreeGenMembers")
                .whereEqualTo("spouseID", deletedMemberId)
                .get()
                .await()

            spouseQuery.documents.forEach { doc ->
                batch.update(doc.reference, "spouseID", null)
                Log.d("FirestoreViewModel", "✅ Cleared spouseID reference for: ${doc.id}")
                messages.add("✅ Removed spouse reference for: ${doc.id}")
            }

            // ✅ Commit all batch updates
            batch.commit().await()

            Log.d("FirestoreViewModel", "✅ References removed successfully for deleted member: $deletedMemberId")
            messages.add("✅ References removed for member: $deletedMemberId")

        } catch (e: Exception) {
            Log.e("FirestoreViewModel", "❌ Error removing references: ${e.message}", e)
            messages.add("❌ Failed to remove references for: $deletedMemberId - Error: ${e.localizedMessage}")
        }
    }
    // completed Method to sync local data to Firestore---------------------------------------------------------------------------------------------------------------
}

