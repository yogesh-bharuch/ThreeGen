package com.example.threegen.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.threegen.util.SyncPreferences
import com.example.threegen.util.WorkManagerHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

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
     * Fetches Firestore data and maps it to `ThreeGen` entities.
     * @return List of `ThreeGen` members mapped from Firestore.
     */
    suspend fun syncFirestoreToRoom(): List<ThreeGen> {
        val members = mutableListOf<ThreeGen>()

        try {
            val documents = firestore.collection("ThreeGenMembers").get().await()

            Log.d("FirestoreSync", "🔥 Fetched ${documents.size()} documents from Firestore")

            if (documents.isEmpty) {
                Log.e("FirestoreSync", "❌ No documents retrieved from Firestore")
            } else {
                for (doc in documents) {
                    Log.d("FirestoreSync", "✅ Document: ${doc.id} -> ${doc.data}")

                    // ✅ Map Firestore data manually to ThreeGen entity
                    val member = ThreeGen(
                        id = doc.getString("id") ?: UUID.randomUUID().toString(),
                        firstName = doc.getString("firstName") ?: "",
                        middleName = doc.getString("middleName"),
                        lastName = doc.getString("lastName") ?: "",
                        town = doc.getString("town") ?: "",
                        shortName = doc.getString("shortName") ?: "",
                        isAlive = doc.getBoolean("isAlive") ?: true,
                        childNumber = doc.getLong("childNumber")?.toInt(),
                        comment = doc.getString("comment"),
                        imageUri = doc.getString("imageUri"),
                        syncStatus = SyncStatus.SYNCED,          // ✅ Mark as synced
                        deleted = false,                         // ✅ Not deleted during sync
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        createdBy = doc.getString("createdBy"),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                        parentID = doc.getString("parentID"),
                        spouseID = doc.getString("spouseID")
                    )
                    members.add(member)
                }
            }

            Log.d("FirestoreSync", "✅ Mapped ${members.size} documents to ThreeGen objects")

        } catch (e: Exception) {
            Log.e("FirestoreSync", "❌ Failed to fetch Firestore data: ${e.message}", e)
        }

        // ✅ Return the list of fetched members
        return members
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

