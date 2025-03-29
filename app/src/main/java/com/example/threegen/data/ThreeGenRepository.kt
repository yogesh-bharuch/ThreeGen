package com.example.threegen.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.threegen.util.SyncPreferences
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ThreeGenRepository(private val threeGenDao: ThreeGenDao) {
    // Singleton instance
    companion object {
        @Volatile
        private var instance: ThreeGenRepository? = null

        // ✅ Add getInstance() to provide repository singleton access
        fun getInstance(context: Context): ThreeGenRepository {
            return instance ?: synchronized(this) {
                instance ?: ThreeGenRepository(
                    ThreeGenDatabase.getInstance(context).getThreeGenDao()
                ).also { instance = it }
            }
        }
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val collectionRef = firestore.collection("ThreeGenMembers")

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
     * 🔥 Fetches only (modified members since the last sync + not created by the same user) and maps them to `ThreeGen`.
     * Sets `deleted = false` by default since Firestore does not have this field.
     */
    suspend fun syncFirestoreToRoom(lastSyncTime: Long, currentUserId: String): List<ThreeGen> {
        return try {
            val query = if (lastSyncTime > 0) { collectionRef
                .whereGreaterThan("updatedAt", lastSyncTime)
                .whereNotEqualTo("createdBy", currentUserId)
            } else {
                collectionRef  // First-time sync: fetch all members
            }

            val snapshot = query.get().await()
            //Log.d("FirestoreSync", "✅ From Repository.syncFirestoreToRoom Query snapshot size: ${snapshot.size()}")

            if (!snapshot.isEmpty) {
                val members = snapshot.documents.map { doc ->
                    ThreeGen(
                        id = doc.getString("id") ?: "",
                        firstName = doc.getString("firstName") ?: "",
                        middleName = doc.getString("middleName") ?: "",
                        lastName = doc.getString("lastName") ?: "",
                        town = doc.getString("town") ?: "",
                        shortName = doc.getString("shortName") ?: "",
                        isAlive = doc.getBoolean("isAlive") ?: true,
                        childNumber = doc.getLong("childNumber")?.toInt(),
                        comment = doc.getString("comment"),
                        imageUri = doc.getString("imageUri"),
                        syncStatus = SyncStatus.SYNCED,
                        deleted = false,  // ✅ Firestore does NOT have this field → set to `false`
                        parentID = doc.getString("parentID"),
                        spouseID = doc.getString("spouseID"),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        createdBy = doc.getString("createdBy") ?: "Unknown"
                    )
                }
                Log.d("Repository.syncFirestoreToRoom", "✅ From Repository.syncFirestoreToRoom Query members size: ${members.size}")
                //returns
                members
            } else {
                Log.d("Repository.syncFirestoreToRoom", "✅ From Repository.syncFirestoreToRoom No modified members found since last sync")
                //returns
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("Repository.syncFirestoreToRoom", "❌ From Repository.syncFirestoreToRoom Sync failed: ${e.message}", e)
            emptyList()
        }
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

    suspend fun getUnsyncedMembers(): List<ThreeGen> {
        return threeGenDao.getUnsyncedMembers()
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
    //private val firestore = FirebaseFirestore.getInstance()

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

    /**
     * ✅ Sync local data to Firestore
     * - Handles:
     *   - Syncing unsynced members
     *   - Deleting soft-deleted members
     *   - Cleaning up references to deleted members
     */
    suspend fun syncLocalDataToFirestore(callback: (String) -> Unit) {
        Log.d("SyncFlow", "🔥 From Repository: syncLocalDataToFirestore called")

        val messages = mutableListOf<String>()

        try {
            // ✅ Fetch all members mark for deleted members will not fetch
            val unsyncedMembers = withContext(Dispatchers.IO) {
                getUnsyncedMembers()
            }

            // ✅ Fetch soft-deleted members
            val deletedMembers = withContext(Dispatchers.IO) {
                getMarkAsDeletedMembers()
            }

            // ✅ Return if no members to sync
            if (unsyncedMembers.isEmpty() && deletedMembers.isEmpty()) {
                val noSyncMessage = "No members to sync"
                Log.d("SyncFlow", "🔥 $noSyncMessage")
                callback(noSyncMessage)
                return
            }

            // ✅ Delete members marked as deleted
            val deleteTasks = coroutineScope {
                if (deletedMembers.isNotEmpty()) {
                    deletedMembers.map { member ->
                        async { deleteFirestoreMember(member, messages) }
                    }
                } else emptyList()
            }

            // ✅ Remove references to deleted members (parent/spouse links)
            val cleanUpTasks = coroutineScope {
                if (deletedMembers.isNotEmpty()) {
                    deletedMembers.map { member ->
                        async { removeReferencesToDeletedMember(member.id, messages) }
                    }
                } else emptyList()
            }

            // ✅ Sync unsynced members to Firestore
            val syncTasks = coroutineScope {
                if (unsyncedMembers.isNotEmpty()) {
                    unsyncedMembers.map { member ->
                        async { updateFirestore(member, messages) }
                    }
                } else emptyList()
            }

            // ✅ Await all operations
            deleteTasks.awaitAll()
            cleanUpTasks.awaitAll()
            syncTasks.awaitAll()

            // ✅ Prepare final result message
            val resultMessage = messages.joinToString("\n").ifEmpty { "No members to sync" }
            Log.d("SyncFlow", "🔥 Final Sync Message: $resultMessage")

            // ✅ Trigger the callback with the result
            callback(resultMessage)

        } catch (e: Exception) {
            Log.e("SyncFlow", "❌ Error during sync operation: ${e.message}", e)
            callback("❌ Error during sync: ${e.message}")
        }
    }

    /**
     * ✅ Syncs a `ThreeGen` member to Firestore, including all fields.
     * Updates the local Room sync status and `updatedAt` timestamp upon successful Firestore sync.
     */
    private suspend fun updateFirestore(threeGen: ThreeGen, messages: MutableList<String>) {
        val currentTime = System.currentTimeMillis()
        Log.d("FirestoreRepo", "🔥 Syncing to Firestore: ${threeGen.firstName}")

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
            "spouseID" to threeGen.spouseID,
            "isAlive" to threeGen.isAlive,
            "updatedAt" to currentTime
        )

        try {
            // ✅ Sync data to Firestore
            collectionRef.document(threeGen.id)
                .set(data)
                .await()

            Log.d("FirestoreRepo", "✅ Updated in Firestore: ${threeGen.firstName}")
            messages.add("✅ Synced: ${threeGen.firstName} ${threeGen.lastName}")

            // ✅ Update local Room sync status and timestamp
            withContext(Dispatchers.IO) {
                val updatedMember = threeGen.copy(
                    syncStatus = SyncStatus.SYNCED,
                    updatedAt = currentTime
                )
                updateThreeGen(updatedMember)
            }

        } catch (e: Exception) {
            Log.e("FirestoreRepo", "❌ Error syncing: ${e.message}", e)
            messages.add("❌ Failed: ${threeGen.firstName} ${threeGen.lastName}")
        }
    }

    /**
     * ✅ Deletes a Firestore member and Room reference
     */
    private suspend fun deleteFirestoreMember(member: ThreeGen, messages: MutableList<String>) {
        try {
            // ✅ Firestore delete
            collectionRef.document(member.id).delete().await()

            // ✅ Local Room delete
            threeGenDao.deleteThreeGen(member.id)

            Log.d("FirestoreRepo", "🔥 Deleted: ${member.firstName}")
            messages.add("🔥 Deleted: ${member.firstName} ${member.lastName}")

        } catch (e: Exception) {
            Log.e("FirestoreRepo", "❌ Failed to delete: ${e.message}", e)
            messages.add("❌ Failed: ${member.firstName}")
        }
    }

    /**
     * ✅ Removes references to deleted members
     */
    private suspend fun removeReferencesToDeletedMember(deletedMemberId: String, messages: MutableList<String>) {
        try {
            Log.d("FirestoreRepo", "🔥 Removing references for ID: $deletedMemberId")

            val batch = firestore.batch()

            // ✅ Clear parentID references
            val parentQuery = collectionRef.whereEqualTo("parentID", deletedMemberId).get().await()
            parentQuery.documents.forEach { doc ->
                batch.update(doc.reference, "parentID", null)
            }

            // ✅ Clear spouseID references
            val spouseQuery = collectionRef.whereEqualTo("spouseID", deletedMemberId).get().await()
            spouseQuery.documents.forEach { doc ->
                batch.update(doc.reference, "spouseID", null)
            }

            // ✅ Commit batch updates
            batch.commit().await()
            messages.add("✅ References removed for ID: $deletedMemberId")

        } catch (e: Exception) {
            Log.e("FirestoreRepo", "❌ Failed to remove references: ${e.message}", e)
            messages.add("❌ Failed: $deletedMemberId")
        }
    }

}

