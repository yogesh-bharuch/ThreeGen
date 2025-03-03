
package com.example.threegen.data

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.threegen.MainApplication
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
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
                val updates = mapOf("firstName" to firstName, "middleName" to middleName, "lastName" to lastName, "town" to town, "parentID" to parentID, "spouseID" to spouseID, "shortName" to uniqueShortName, "imageUri" to imageUri)
                val result = firestoreRepository.updateMemberInFirestore(memberId.toString(), updates)
                if (result.isFailure) {
                    Log.e("Firestore", "Failed to update member in Firestore: ${result.exceptionOrNull()?.message}")
                } else {
                    Log.d("Firestore", "Successfully updated member in Firestore")
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
            val spouse = threeGen.spouseID?.let { repository.getMemberByIdSync(it) }
            val children = repository.getChildrenByParentId(threeGen.id) // Fetch children

            // Step 2: Update Firestore for related members
            if (spouse != null) {
                val spouseUpdate = mapOf("spouseID" to null)
                firestoreRepository.updateMemberInFirestore(spouse.id.toString(), spouseUpdate)
            }

            for (child in children) {
                val childUpdate = mapOf("parentID" to null)
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
