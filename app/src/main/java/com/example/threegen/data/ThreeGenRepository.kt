package com.example.threegen.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.threegen.util.SyncPreferences
import com.example.threegen.util.WorkManagerHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

class ThreeGenRepository(private val threeGenDao: ThreeGenDao) {

    /**
     * Retrieves all members from the Room database as LiveData.
     * This allows UI to automatically update when data changes.
     */
    val allThreeGen: LiveData<List<ThreeGen>> = threeGenDao.getAllThreeGen().asLiveData()

    /**
     * Inserts a new member or updates an existing one.
     */
    suspend fun addThreeGen(member: ThreeGen): Int {
        return threeGenDao.insert(member).toInt()
    }

    /**
     * Updates an existing member in the local database.
     */
    suspend fun updateThreeGen(member: ThreeGen): Int {
        val returnedRows = threeGenDao.updateThreeGen(member) // Update the member in the database
        return returnedRows
    }

    /**
     * Marks a specific member as deleted in the database.
     */
    suspend fun markAsdeletedThreeGen(member: ThreeGen) {
        threeGenDao.markAsDeleted(member.id)
        // after delete in firestore delete logic to be developed
        //WorkManagerHelper.scheduleImmediateSync(context)
    //threeGenDao.deleteThreeGen(member.id)
    }

    suspend fun deleteThreeGen(member: ThreeGen) {
        threeGenDao.deleteThreeGen(member.id)
    }

    /**
     * Retrieves a member by ID as LiveData, ensuring UI updates reactively.
     */
    fun getMemberById(id: String): LiveData<ThreeGen?> {
        return threeGenDao.getMemberById(id).asLiveData()
    }

    /**
     * Retrieves a specific member synchronously (used when coroutines are not available).
     */
    suspend fun getMemberByIdSync(id: String): ThreeGen? {
        return threeGenDao.getMemberByIdSync(id)
    }

    suspend fun getMarkedAsDeletedMemberByIdSync(id: String): ThreeGen? {
        return threeGenDao.getMarkedAsDeletedMemberByIdSync(id)
    }


    /**
     * Retrieves the count of a shortName to ensure uniqueness.
     */
    suspend fun getShortNameCount(shortName: String): Int {
        return threeGenDao.getShortNameCount(shortName)
    }

    /**
     * Retrieves all siblings of a given member.
     */
    fun getSiblings(memberId: String): Flow<List<ThreeGen>> {
        return threeGenDao.getSiblings(memberId)
    }

    /**
     * Retrieves children of a specific parent as Flow.
     */
    fun getChildren(parentId: String): Flow<List<ThreeGen>> {
        return threeGenDao.getChildren(parentId)
    }

    /**
     * Retrieves children synchronously (useful for background tasks).
     */
    suspend fun getChildrenByParentId(parentId: String): List<ThreeGen> {
        return threeGenDao.getChildrenByParentId(parentId)
    }

    /**
     * Retrieves spouse of a given member.
     */
    fun getSpouse(spouseId: String): Flow<ThreeGen?> {
        return threeGenDao.getSpouse(spouseId)
    }

    /**
     * Fetch all members.
     */
    suspend fun getAllMembers(): List<ThreeGen> {
        return threeGenDao.getAllMembers()
    }

    /**
     * Fetch all members.
     */
    suspend fun getMarkAsDeletedMembers(): List<ThreeGen> {
        return threeGenDao.getMarkAsDeletedMembers()
    }

    suspend fun getMembersReferencing(memberId: String): List<ThreeGen> {
        return threeGenDao.getMembersReferencing(memberId)
    }
    /**
     * Fetch members matching search query.
     */
    suspend fun searchMembers(query: String): List<ThreeGen> {
        return threeGenDao.searchMembers(query)
    }

    //-----------Firebase -->  local sync start ----------
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * ✅ Retrieve the last sync timestamp
     * @param context - Application context
     * @return The last sync timestamp
     */
    suspend fun getLastSyncTimestamp(context: Context): Long {
        return SyncPreferences.getLastSyncTimestamp(context)
    }

    /**
     * ✅ Save the new last sync timestamp
     * @param context - Application context
     * @param timestamp - New timestamp to store
     */
    suspend fun setLastSyncTimestamp(context: Context, timestamp: Long) {
        SyncPreferences.setLastSyncTimestamp(context, timestamp)
    }
    //-----------Firebase -->  local sync complete----------

}

