package com.example.threegen.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.threegen.MainApplication
import com.example.threegen.util.MemberState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewThreeGenViewModel : ViewModel() {
    private val threeGenDao = MainApplication.threeGenDatabase.getThreeGenDao()
    val threeGenList: LiveData<List<ThreeGen>> = threeGenDao.getAllThreeGen()

    private val _memberState = MutableLiveData<MemberState>()
    val memberState: LiveData<MemberState> get() = _memberState

    /*init {
        // Assuming you want to fetch data as soon as the ViewModel is created
        threeGenDao.getMemberById(8)
    }*/

    suspend fun getMemberById(memberId: Int) {
        // Simulate loading state
        _memberState.value = MemberState.Loading
        Log.d("NewThreeGenViewModel", "Fetching member with ID: $memberId")

        // Fetch the member from the database
        viewModelScope.launch {
            val communityMember = withContext(Dispatchers.IO) {
                threeGenDao.getMemberByIdSync(memberId)
            }
            delay(1000L)
            if (communityMember != null) {
                _memberState.value = MemberState.Success(communityMember)
            } else {
                _memberState.value = MemberState.Error
            }
        }
    }

    private val _zoomedImageUri = MutableLiveData<String?>(null)
    val zoomedImageUri: LiveData<String?> get() = _zoomedImageUri

    fun setZoomedImageUri(uri: String?) {
        _zoomedImageUri.value = uri
    }

    fun updateMember(member: ThreeGen) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                threeGenDao.updateMember(member)
            }
            _memberState.value = MemberState.Success(member)
        }
    }
}
