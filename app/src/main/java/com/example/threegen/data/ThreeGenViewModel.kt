
package com.example.threegen.data

import android.util.Log
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
    //private lateinit var firestore : FirebaseFirestore
    init {
        val threeGenDao = MainApplication.threeGenDatabase.getThreeGenDao()
        repository = ThreeGenRepository(threeGenDao)
    }

    val threeGenList: LiveData<List<ThreeGen>> = repository.allThreeGen

    // Add a new ThreeGen entry
    fun addThreeGen(
        firstName: String,
        middleName: String,
        lastName: String,
        town: String,
        imageUri: String?,
        parentID: Int?,
        spouseID: Int?
    ) {
        // Validate inputs: All fields must be non-empty
        if (firstName.isBlank() || middleName.isBlank() || lastName.isBlank() || town.isBlank()) {
            // Handle validation error (e.g., show a message to the user)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val uniqueShortName = generateUniqueShortName(firstName, middleName, lastName, town)
            val newMember = ThreeGen(
                firstName = firstName,
                middleName = middleName,
                lastName = lastName,
                town = town,
                shortName = uniqueShortName,
                imageUri = imageUri,
                parentID = parentID,
                spouseID = spouseID,
                createdAt = System.currentTimeMillis()
            )
            repository.addThreeGen(newMember)
        }
    }

    // Generate shortName from the initials of the four fields
    private suspend fun generateUniqueShortName(
        firstName: String,
        middleName: String,
        lastName: String,
        town: String
    ): String {
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

    fun getMemberById(id: Int): LiveData<ThreeGen?> {
        return repository.getMemberById(id)
    }

    // Delete a ThreeGen entry
    fun deleteThreeGen(threeGen: ThreeGen) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteThreeGen(threeGen)
        }
    }

    // Update member
    fun updateMember(
        memberId: Int,
        firstName: String,
        middleName: String,
        lastName: String,
        town: String,
        parentID: Int?,
        spouseID: Int?,
        shortName: String,
        imageUri: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val member = repository.getMemberByIdSync(memberId)
            val uniqueShortName = generateUniqueShortName(firstName, middleName, lastName, town)

            Log.d("SelectMemberScreen", "Member: ${member?.firstName}")
            val updatedMember = member?.copy(
                firstName = firstName,
                middleName = middleName,
                lastName = lastName,
                town = town,
                parentID = parentID,
                spouseID = spouseID,
                shortName = uniqueShortName,
                imageUri = imageUri
            )
            Log.d("SelectMemberScreen", "From view model Updating member with ID: $memberId to $updatedMember")
            if (updatedMember != null) {
                repository.updateThreeGen(updatedMember)
            }
        }
    }

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
}




















/*

package com.example.threegen.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.threegen.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ThreeGenViewModel : ViewModel() {
   private val threeGenDao = MainApplication.threeGenDatabase.getThreeGenDao()
   val threeGenList: LiveData<List<ThreeGen>> = threeGenDao.getAllThreeGen()
    //private val threeGenRepository = ThreeGenRepository(MainApplication.threeGenDatabase.getThreeGenDao())
    //val threeGenList: LiveData<List<ThreeGen>> = threeGenRepository.allThreeGen

    // Add a new SevenGen entry
    fun addThreeGen(
        firstName: String,
        middleName: String,
        lastName: String,
        town: String,
        imageUri: String?,
        parentID: Int?,
        spouseID: Int?
    ) {
        // Validate inputs: All fields must be non-empty
        if (firstName.isBlank() || middleName.isBlank() || lastName.isBlank() || town.isBlank()) {
            // Handle validation error (e.g., show a message to the user)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val uniqueShortName = generateUniqueShortName(firstName, middleName, lastName, town)
            val newMember = ThreeGen(
                firstName = firstName,
                middleName = middleName,
                lastName = lastName,
                town = town,
                shortName = uniqueShortName,
                imageUri = imageUri,
                parentID = parentID,
                spouseID = spouseID,
                createdAt = System.currentTimeMillis()
            )
            threeGenDao.addThreeGen(newMember)
        }
    }

    // Generate shortName from the initials of the four fields
    private suspend fun generateUniqueShortName(
        firstName: String,
        middleName: String,
        lastName: String,
        town: String
    ): String {
        val initials = StringBuilder()
        initials.append(firstName.first().uppercaseChar())
        initials.append(middleName.first().uppercaseChar())
        initials.append(lastName.first().uppercaseChar())
        initials.append(town.first().uppercaseChar())

        var baseShortName = initials.toString()
        var uniqueShortName = baseShortName
        var count = 1

        while (threeGenDao.getShortNameCount(uniqueShortName) > 0) {
            uniqueShortName = "$baseShortName$count"
            count++
        }

        return uniqueShortName
    }
    fun getMemberById(id: Int): LiveData<ThreeGen?> {
        return threeGenDao.getMemberById(id)
    }

    // Delete a SevenGen entry
    fun deleteThreeGen(threeGen: ThreeGen) {
        viewModelScope.launch(Dispatchers.IO) {
            threeGenDao.deleteThreeGen(threeGen)
        }
    }

    // Update  member
    fun updateMember(
        memberId: Int,
        firstName: String,
        middleName: String,
        lastName: String,
        town: String,
        shortName: String,
        imageUri: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val member = threeGenDao.getMemberByIdSync(memberId)
            val uniqueShortName = generateUniqueShortName(firstName, middleName, lastName, town)

            Log.d("SelectMemberScreen", "Member: ${member?.firstName}")
            val updatedMember = member?.copy(
                firstName = firstName,
                middleName = middleName,
                lastName = lastName,
                town = town,
                shortName = uniqueShortName,
                imageUri = imageUri
            )
            Log.d("SelectMemberScreen", "From view model Updating member with ID: $memberId to $updatedMember")
            if (updatedMember != null) {
                threeGenDao.updateThreeGen(updatedMember)
            }
        }
    }


    // Update the parentId of a member
    fun updateParentId(memberId: Int, parentId: Int) {
        Log.d("SelectMemberScreen", "From view model Updating parent ID for member with ID: $memberId to $parentId")
        viewModelScope.launch(Dispatchers.IO) {
            val member1 = threeGenDao.getMemberByIdSync(memberId)
            Log.d("SelectMemberScreen", "Member: $member1.id")
            member1?.let {
                it.parentID = parentId
                threeGenDao.updateThreeGen(it)
            }
        }
    }

    // Update the ImageUri of a member
    fun updateImageUri(memberId: Int, imageUri: String) {
        Log.d("SelectMemberScreen", "From view model Updating parent ID for member with ID: $memberId to $imageUri")
        viewModelScope.launch(Dispatchers.IO) {
            val member1 = threeGenDao.getMemberByIdSync(memberId)
            Log.d("SelectMemberScreen", "Member: $member1.id")
            member1?.let {
                it.imageUri = imageUri
                threeGenDao.updateThreeGen(it)
            }
        }
    }

    // Update the spouse of a member
    fun updateSpouseId(memberId: Int, spouseId: Int) {
        Log.d("SelectMemberScreen", "From view model Updating spouse ID for member with ID: $memberId to $spouseId")
        viewModelScope.launch(Dispatchers.IO) {
            val member1 = threeGenDao.getMemberByIdSync(memberId)
            Log.d("SelectMemberScreen", "Member: $member1.id")
            member1?.let {
                it.spouseID = spouseId
                threeGenDao.updateThreeGen(it)
            }
        }
    }

    // Retrieve member and spouse data
    private val _memberAndSpouseData = MutableLiveData<MemberAndSpouseData?>()
    val memberAndSpouseData: LiveData<MemberAndSpouseData?> get() = _memberAndSpouseData

    fun getMemberAndSpouseData(memberId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = threeGenDao.getMemberAndSpouseData(memberId)
            _memberAndSpouseData.postValue(data)
        }
    }

    // Retrieve siblings data
    private val _siblingsData = MutableLiveData<List<ThreeGen>>()
    val siblingsData: LiveData<List<ThreeGen>> get() = _siblingsData

    fun getSiblings(memberId: Int) {

        viewModelScope.launch(Dispatchers.IO) {
            val data = threeGenDao.getSiblings(memberId) //memberId
            Log.d("ThreeGenViewModel", "Retrieved ${data.value?.size} siblings: ${data.value}")
            _siblingsData.postValue(data.value)
        }
    }
}
*/