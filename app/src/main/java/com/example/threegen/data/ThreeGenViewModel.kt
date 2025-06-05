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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

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

    var isSyncedInSession = false // is used in main activity to handle sync once only during app start

    //var refreshMembersList = false // is used in ListMembers screen to refresh list after Sync press
    private val _refreshMembersList = MutableStateFlow(false)
    val refreshMembersList: StateFlow<Boolean> get() = _refreshMembersList

    // Function to toggle refreshTrigger
    fun refreshMembersList() {
        _refreshMembersList.value = !_refreshMembersList.value
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
    fun addThreeGen(firstName: String, middleName: String, lastName: String, town: String, imageUri: String?, parentID: String?, spouseID: String?, childNumber: Int? = 1, comment: String? = null, onResult: (String, Int) -> Unit) {
        if (firstName.isBlank() || middleName.isBlank() || lastName.isBlank() || town.isBlank()) {
            Log.e("ThreeGenViewModel", "Validation failed: All name fields and town are required")
            onResult("No members added",0) // Signal failure
            return
        }
        var spouseMessage = "No spouse to update"      // Declare outside coroutine
        var spouseUpdatedRows = 0

        viewModelScope.launch(Dispatchers.IO) {
            val newUUID = UUID.randomUUID().toString()
            val formattedFirstName = formatName(firstName)
            val formattedMiddleName = formatName(middleName)
            val formattedLastName = formatName(lastName)
            val formattedTown = formatName(town)
            val uniqueShortName = generateUniqueShortName(formattedFirstName, formattedMiddleName, formattedLastName, formattedTown)
            // ✅ Get the current user ID for the createdBy field
            val currentUserId = auth.currentUser?.uid

            // ✅ Create a new ThreeGen object to insert in local database with formated values and createdby field with current user
            val newMember = ThreeGen(id = newUUID, firstName = formattedFirstName, middleName = formattedMiddleName, lastName = formattedLastName, town = formattedTown, shortName = uniqueShortName, imageUri = imageUri, parentID = parentID, spouseID = spouseID, createdAt = System.currentTimeMillis(), syncStatus = SyncStatus.NOT_SYNCED, childNumber = childNumber, comment = comment, createdBy = currentUserId)
            val insertedRows = repository.addThreeGen(newMember)

            //TODO sync to firestore

            // ✅ If spouseID exists, update the spouse's record with the new member's ID
            if (!spouseID.isNullOrBlank()) {
                val spouse = repository.getMemberByIdSync(spouseID)
                if (spouse != null) {
                    val updatedSpouse = spouse.copy(
                        spouseID = newMember.id,           // ✅ Link new member as the spouse
                        syncStatus = SyncStatus.UPDATED    // ✅ Mark spouse for Firestore sync
                    )
                    spouseUpdatedRows = repository.updateThreeGen(updatedSpouse)
                    // ✅ Include spouse update message
                    spouseMessage = if (spouseUpdatedRows > 0) {
                        "✅ Spouse updated: ${spouse.firstName} ${spouse.lastName}"
                    } else { "❌ No Spouse or Spouse update failed" }

                    Log.d("ThreeGenViewModel", spouseMessage)
                }
            }
            // ✅ Prepare final result message
            val finalMessage = if (insertedRows > 0) {
                "✅ Member added: $firstName $lastName\n$spouseMessage"
            } else {
                "❌ Failed to add member"
            }
            withContext(Dispatchers.Main) {
                onResult(finalMessage, insertedRows) // Notify the number of rows inserted
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
            // ✅ If member has a spouse, update the spouse's `spouseID`
            var spouseUpdatedRows = 0
            if (!spouseID.isNullOrBlank()) {
                val spouse = repository.getMemberByIdSync(spouseID)
                spouse?.let {
                    // Update the spouse's `spouseID` to point back to this member
                    val updatedSpouse = it.copy(
                        spouseID = memberId,               // Point back to the current member
                        syncStatus = SyncStatus.UPDATED     // Mark the spouse as updated
                    )
                    spouseUpdatedRows = repository.updateThreeGen(updatedSpouse)
                    Log.d("update member", "Spouse updated: ${updatedSpouse.firstName}")
                }
            }

            // ✅ Switch back to the main thread to show the Toast
            withContext(Dispatchers.Main) {
                onResult(updatedRows + spouseUpdatedRows)
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

    suspend fun removeParentRef(threeGenId: String) {
        Log.d("ThreeGenViewModel", "Removing parent with ID: $threeGenId")
        repository.removeParentRef(threeGenId)
    }
    suspend fun removeSpouseRef(threeGenId: String) {
        Log.d("ThreeGenViewModel", "Removing Spouse with ID: $threeGenId")
        repository.removeSpouseRef(threeGenId)
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
        Log.d("SyncFlow", "🔥 Triggering sync from ViewModel")

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            val message = "Not authenticated. Sync action skipped."
            callback(message)
            return
        }

        viewModelScope.launch {
            repository.syncLocalDataToFirestore { result ->
                callback(result)
            }
        }
    }

    fun syncFirestoreToRoom(lastSyncTime: Long, isFirstRun: Boolean = false, callback: (String) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            val message = "Not authenticated. Sync action skipped."
            callback(message)
            return
        }

        viewModelScope.launch {
            repository.syncFirestoreToRoom(lastSyncTime, isFirstRun, currentUserId) { result ->
                callback(result)
            }
        }
    }
}

