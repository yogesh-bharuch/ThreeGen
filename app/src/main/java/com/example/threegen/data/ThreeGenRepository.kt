package com.example.threegen.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
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
    suspend fun addThreeGen(member: ThreeGen) {
        Log.d("ThreeGenViewModel", "Adding member to repo: $member")
        threeGenDao.insert(member)
    }

    /**
     * Updates an existing member in the local database.
     */
    suspend fun updateThreeGen(member: ThreeGen) {
        /*val oldValue = threeGenDao.getMemberByIdSync(member.id) // Fetch the old value
        if (oldValue != null) {
            Log.d("yogesh", "Updating member in repo before update firstname is : ${oldValue.firstName}")
        }*/
        Log.d("yogesh", "Updating member in repo before update firstname is : $member")
        val returnedRows = threeGenDao.updateThreeGen(member) // Update the member in the database
        Log.d("yogesh", "Returned updated rows from dao: $returnedRows")

        /*        val freshMember = getMemberByIdSync(member.id) // Fetch the updated member from the database
        if (freshMember != null) {
            Log.d("yogesh", "Updated member in repo after update firstname is : ${freshMember.firstName}")
        }*/
    }

    /**
     * Marks a specific member as deleted in the database.
     */
    suspend fun deleteThreeGen(member: ThreeGen) {
        threeGenDao.markAsDeleted(member.id)
        // after delete in firestore delete logic to be developed
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
     * Fetch members matching search query.
     */
    suspend fun searchMembers(query: String): List<ThreeGen> {
        return threeGenDao.searchMembers(query)
    }
}




























/*
package com.example.threegen.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow

class ThreeGenRepository(private val threeGenDao: ThreeGenDao) {

    // Retrieves all members from the Room database as LiveData.
    // This allows UI to automatically update when data changes.

    val allThreeGen: LiveData<List<ThreeGen>> = threeGenDao.getAllThreeGen().asLiveData()

     // Inserts a new member or updates an existing one.
    suspend fun addThreeGen(member: ThreeGen) {
        threeGenDao.insert(member)
    }


     // Updates an existing member in the local database.
    suspend fun updateThreeGen(member: ThreeGen) {
        val oldValue = threeGenDao.getMemberByIdSync(member.id) // Fetch the old value
         if (oldValue != null) {
             Log.d("ThreeGenViewModel", "Updating member in repo before update firstname is : ${oldValue.firstName}")
         }

        val returnedRows = threeGenDao.updateThreeGen(member) // Update the member in the database
         Log.d("ThreeGenViewModel", "Returned updated rows from dao: $returnedRows")

         val freshMember = getMemberByIdSync(member.id) // Fetch the updated member from the database
         if (freshMember != null) {
             Log.d("ThreeGenViewModel", "Updated member in repo after update firstname is : ${freshMember.firstName}")
         }
    }

     // Deletes a specific member from the database.
    suspend fun deleteThreeGen(member: ThreeGen) {
        threeGenDao.deleteThreeGen(member)
    }

     // Retrieves a member by ID as LiveData, ensuring UI updates reactively.
    fun getMemberById(id: String): LiveData<ThreeGen?> {
        return threeGenDao.getMemberById(id).asLiveData()
    }

    /**
     * Retrieves a specific member **synchronously** (used when coroutines are not available).
     */
    suspend fun getMemberByIdSync(id: String): ThreeGen? {
        return threeGenDao.getMemberByIdSync(id)
    }

    /**
     * Retrieves the count of a `shortName` to ensure uniqueness.
     */
    suspend fun getShortNameCount(shortName: String): Int {
        return threeGenDao.getShortNameCount(shortName)
    }

    /**
     * Retrieves all **siblings** of a given member.
     */
    fun getSiblings(memberId: String): Flow<List<ThreeGen>> {
        return threeGenDao.getSiblings(memberId)
    }

    /**
     * Retrieves **children** of a specific parent as Flow.
     */
    fun getChildren(parentId: String): Flow<List<ThreeGen>> {
        return threeGenDao.getChildren(parentId)
    }

    /**
     * Retrieves **children** synchronously (useful for background tasks).
     */
    suspend fun getChildrenByParentId(parentId: String): List<ThreeGen> {
        return threeGenDao.getChildrenByParentId(parentId)
    }

    /**
     * Retrieves **spouse** of a given member.
     */
    fun getSpouse(spouseId: String): Flow<ThreeGen?> {
        return threeGenDao.getSpouse(spouseId)
    }

    // Fetch all members
    suspend fun getAllMembers(): List<ThreeGen> {
        return threeGenDao.getAllMembers()
    }

    // Fetch members matching search query
    suspend fun searchMembers(query: String): List<ThreeGen> {
        return threeGenDao.searchMembers(query)
    }
}
*/





/*
package com.example.threegen.data


import androidx.lifecycle.LiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class ThreeGenRepository(private val threeGenDao: ThreeGenDao) {

    val allThreeGen: LiveData<List<ThreeGen>> = threeGenDao.getAllThreeGen()

    fun getAllMembers(): Flow<List<ThreeGen>> {
        return threeGenDao.getAllThreeGenAsFlow()
    }

    suspend fun getAllThreeGenAsObject(): List<ThreeGen> {
        return threeGenDao.getAllThreeGenAsObject()
    }
    suspend fun insert(threeGen: ThreeGen) {
        threeGenDao.insert(threeGen)
    }

    suspend fun addThreeGen(threeGen: ThreeGen): Long {
        return threeGenDao.addThreeGen(threeGen)
    }

    suspend fun updateThreeGen(threeGen: ThreeGen) {
        threeGenDao.updateThreeGen(threeGen)
    }

    suspend fun updateMember(threeGen: ThreeGen) {
        threeGenDao.updateMember(threeGen)
    }

    suspend fun deleteThreeGen(threeGen: ThreeGen) {
        threeGenDao.deleteThreeGen(threeGen)
    }

    fun getMemberById(id: Int): LiveData<ThreeGen?> {
        return threeGenDao.getMemberById(id)
    }

    suspend fun getShortNameCount(shortName: String): Int {
        return threeGenDao.getShortNameCount(shortName)
    }

    fun getMemberByIdSync(id: Int): ThreeGen? {
        return threeGenDao.getMemberByIdSync(id)
    }

    fun getMemberAndSpouseData(memberId: Int): MemberAndSpouseData? {
        return threeGenDao.getMemberAndSpouseData(memberId)
    }

    fun getSiblings(memberId: Int): LiveData<List<ThreeGen>> {
        return threeGenDao.getSiblings(memberId)
    }

    // Optional methods
    fun getChildren(parentId: Int): LiveData<List<ThreeGen>> {
        return threeGenDao.getChildren(parentId)
    }
    suspend fun getChildrenByParentId(parentId: Int): List<ThreeGen> {
        return threeGenDao.getChildrenByParentId(parentId)
    }

    fun getSpouse(spouseId: Int): LiveData<ThreeGen?> {
        return threeGenDao.getSpouse(spouseId)
    }

}



 */