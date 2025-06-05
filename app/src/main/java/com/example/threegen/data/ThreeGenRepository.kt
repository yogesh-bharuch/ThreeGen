package com.example.threegen.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.threegen.util.DebugTags
import com.example.threegen.util.SyncPreferences
import com.example.threegen.util.isInternetAvailable
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
     * Fetches modified members from Firestore since the last sync time.
     *
     * @param lastSyncTime The last sync timestamp.
     * @param currentUserId The ID of the current user to avoid syncing self-created records.
     * @return A list of modified `ThreeGen` members.
     */
    private suspend fun syncFirestoreToRoom(
        lastSyncTime: Long,
        currentUserId: String
    ): List<ThreeGen> = withContext(Dispatchers.IO) {
        val members = mutableListOf<ThreeGen>()
        try {
            val query = firestore.collection("ThreeGenMembers")
                .whereGreaterThan("updatedAt", lastSyncTime)
                .whereNotEqualTo("createdBy", currentUserId)
                .get()
                .await()

            for (document in query.documents) {
                val member = document.toObject(ThreeGen::class.java)
                if (member != null) {
                    members.add(member)
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "❌ Failed to fetch modified Firestore members: ${e.message}", e)
        }
        return@withContext members
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

    suspend fun removeParentRef(threeGenId: String) {
        Log.d("ThreeGenViewModel", "Removing parent with from Repository ID : $threeGenId")
        threeGenDao.removeParentRef(threeGenId)
    }
    suspend fun removeSpouseRef(threeGenId: String) {
        Log.d("ThreeGenViewModel", "Removing spouse with from Repository ID : $threeGenId")
        threeGenDao.removeSpouseRef(threeGenId)
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

 /*   suspend fun uploadImageToFirebaseStorage(
        context: Context,
        uri: Uri,
        memberId: String
    ): String = withContext(Dispatchers.IO) {
        val storage = Firebase.storage
        val storageRef = storage.reference.child("profile_images/${memberId}.jpg")

        val uploadTask = storageRef.putFile(uri).await()
        storageRef.downloadUrl.await().toString()
    }
*/
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
            Log.d("FirestoreRepo", "✅ References removed for ID: $deletedMemberId")

        } catch (e: Exception) {
            Log.e("FirestoreRepo", "❌ Failed to remove references: ${e.message}", e)
            messages.add("❌ Failed: $deletedMemberId")
        }
    }


    //-------- Firestore-->Room starts
    /**
     * Syncs Firestore data to the local Room database.
     *
     * @param lastSyncTime The last sync timestamp to fetch modified members.
     * @param isFirstRun Boolean flag indicating if it is the first app run.
     * @param currentUserId The ID of the current Firebase user.
     * @param callback A callback function to return the result message.
     */
    suspend fun syncFirestoreToRoom(
        lastSyncTime: Long,
        isFirstRun: Boolean = false,
        currentUserId: String,
        callback: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        //Log.d("Repository", "🔥 From Repository.syncFirestoreToRoom: Syncing Firestore → Room...")
        try {
            //Log.d("Repository", "🔥 From Repository.syncFirestoreToRoom: Syncing Firestore → Room...")

            // ✅ Fetch modified members from Firestore
            val members = fetchModifiedFirestoreMembers(isFirstRun, lastSyncTime, currentUserId)
            Log.d("Repository", "✅ From Repository.syncFirestoreToRoom: total members: ${members.size} fetched")

            if (members.isNotEmpty()) {
                if (isFirstRun) {
                    // 🔥 First run → Clear Room DB
                    threeGenDao.clearAll()
                    Log.d("Repository", "✅ From Repository.syncFirestoreToRoom isFirstRun = true: Cleared all the data from local Room DB")
                }

                // 🔥 Step 1: Insert all members WITHOUT relationships
                val membersWithoutRelationships = members.map { member ->
                    val imageUriRaw = member.imageUri ?: ""
                    val correctedGsUri = imageUriRaw.replace("firebasestorage.app", "appspot.com")
                    Log.d("Repository", "imageUriRaw: $imageUriRaw")
                    val fixedImageUri = if (correctedGsUri.startsWith("gs://"))
                    {
                        try {
                            convertGsUriToDownloadUrl(imageUriRaw)
                        } catch (e: Exception) {
                            Log.e("Repository", "⚠️ Failed to convert imageUri: ${e.message}")
                            ""
                        }
                    } else imageUriRaw
                    Log.d("Repository", "fixedImageUri: $fixedImageUri")
                    member.copy(parentID = null, spouseID = null, imageUri = fixedImageUri)  // Temporarily remove relationships
                }
                //threeGenDao.insertOrUpdateMembers(membersWithoutRelationships)
                val insertedRowIds = threeGenDao.insertOrUpdateMembers(membersWithoutRelationships)
                Log.d("Repository", "✅ From syncFirestoreToRoom: Inserted ${insertedRowIds.size} members Without Relationships inserted into the database")

                //Log.d("Repository", "✅ From Repository.syncFirestoreToRoom: Inserted ${members.size} members without relationships")
                delay(2000)
                // 🔥 Step 2: Update relationships in Room
                updateRelationshipsInRoom(members)

                callback("✅  Repository.syncFirestoreToRoom: ${members.size} members synced from Firestore to Room successfully")
            } else {
                Log.d("Repository", "✅ From Repository.syncFirestoreToRoom: No modified members found")
                callback("✅  Repository.syncFirestoreToRoom: No members to sync")
            }

        } catch (e: Exception) {
            Log.e("Repository", "❌ From Repository.syncFirestoreToRoom: Sync failed: ${e.message}", e)
            callback("❌ Sync failed: ${e.message}")
        }
    }

    /**
     * 🔥 Fetches only (modified members since the last sync + not created by the same user) and maps them to `ThreeGen`.
     * Sets `deleted = false` by default since Firestore does not have this field.
     */
    private suspend fun fetchModifiedFirestoreMembers(isFirstRun: Boolean =false, lastSyncTime: Long, currentUserId: String): List<ThreeGen> {
        return try {
         /*   ✅ add this condition once app is released
         val query = if (!isFirstRun) {
                collectionRef
                .whereGreaterThan("updatedAt", lastSyncTime)
                .whereNotEqualTo("createdBy", currentUserId)
            } else {
                collectionRef  // First-time sync: fetch all members
            }*/
            val lastSyncTimeRetrived = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(lastSyncTime))

            Log.d("Repository", "✅ From Repository.syncFirestoreToRoom: isFirstRun = $isFirstRun lastSyncTime: $lastSyncTimeRetrived")
            val query = if (!isFirstRun || lastSyncTime > 0) {
                collectionRef
                    .whereGreaterThan("updatedAt", lastSyncTime)

            } else {
                Log.d("FirestoreSync", "✅ From Repository.syncFirestoreToRoom: isFirstRun = true or Manual Sync")
                collectionRef  // First-time sync: fetch all members
            }

            val snapshot = query.get().await()
            //Log.d("Repository", "✅ From Repository.syncFirestoreToRoom Query snapshot size: ${snapshot.size()}")

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
                //Log.d("Repository.syncFirestoreToRoom", "✅ From Repository.fetchModifiedFirestoreMembers members: ${members.size} fetched")
                members
            } else {
                Log.d("Repository.syncFirestoreToRoom", "✅ From Repository.syncFirestoreToRoom No modified members found since last sync")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("Repository.syncFirestoreToRoom", "❌ From Repository.syncFirestoreToRoom Sync failed: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Updates parent and spouse relationships in Room.
     *
     * @param members List of `ThreeGen` members.
     */
    private suspend fun updateRelationshipsInRoom(members: List<ThreeGen>) = withContext(Dispatchers.IO) {
        try {
           // Log.d("FirestoreSyncupdate.RelationshipsInRoom", "🔥 From Repository.updateRelationshipsInRoom: Updating relationships...")
            var updatedCount = 0

            members.forEach { member ->
                //if (member.parentID != null || member.spouseID != null) {

                    // ✅ Find the corresponding Room member by ID
                    val localMember = threeGenDao.getMemberByIdSync(member.id)

                    if (localMember != null) {
                        // ✅ Update relationships in Room
                        threeGenDao.updateRelationships(
                            id = member.id,
                            parentID = member.parentID,
                            spouseID = member.spouseID
                        )
                        updatedCount++
                        Log.d("FirestoreSync", "✅ From Repository.updateRelationshipsInRoom: $updatedCount Members Updated for relationships i.e.: ${member.firstName} ${member.middleName} ${member.lastName}")
                    }
                //}
            }

            //Log.d("FirestoreSync", "✅ Successfully updated $updatedCount relationships in Room")

        } catch (e: Exception) {
            Log.e("Repository", "❌ From Repository.updateRelationshipsInRoom: Relationship update failed: ${e.message}", e)
        }
    }
    private suspend fun convertGsUriToDownloadUrl(gsUri: String): String {
        val storage = FirebaseStorage.getInstance()
        val path = gsUri.removePrefix("gs://threegen-b5677.appspot.com/")
        val ref = storage.reference.child(path)
        return ref.downloadUrl.await().toString()
    }

    //-------- Firestore-->Room ends

    /*suspend fun syncRoomToFirestore(context: Context){
        if (!isInternetAvailable(context)) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "❌ No internet connection. Sync aborted.", Toast.LENGTH_SHORT).show()
            }
            Log.d(DebugTags.SYNC_FIRESTORE, "❌ No internet connection. Sync aborted. From UserRepository.syncRoomToFirestore")
            return
        }

        // 🔹 Run database queries in IO dispatcher to avoid blocking the main thread
        val deletedRoomUsers = withContext(Dispatchers.IO) { //userDao.getRoomDeletedUsers() }
        val unsyncedRoomUsers = withContext(Dispatchers.IO) { //userDao.getRoomUnsyncedUsers() }

        // 🔹 Now, logging will only execute once both queries have completed
        //Log.d(DebugTags.SYNC_FIRESTORE, "📦 Local->Firestore Found ${deletedRoomUsers.size} deleted user(s) to sync.  From UserRepository.syncRoomUnsyncedUsers")
        //Log.d(DebugTags.SYNC_FIRESTORE, "📦 Local->Firestore Found ${unsyncedRoomUsers.size} unsynced user(s) to sync.  From UserRepository.syncRoomUnsyncedUsers")

        //----Syncing Room->Firestore deleted users-------------
        *//*for (user in deletedRoomUsers) {
            val success = firestoreService.markFirestoreUserAsDeleted(user) // Then sync deletion to Firestore

            if (success){
                userDao.deleteRoomUser(user.id) // ✅ Permanently deleted locally
                Log.d(DebugTags.SYNC_FIRESTORE, "🗑️ User ${user.firstName} deleted Permanently in Room. From UserRepository.syncRoomToFirestore")
            } else {
                Toast.makeText(context, "Error deleting user from Firestore", Toast.LENGTH_SHORT).show()
            }
        }
        //----Syncing Room->Firestore deleted users-------------

        //----Syncing Room->Firestore updated/inserted users-------------
        for (user in unsyncedRoomUsers) {
            val updatedUser = uploadImageToFireStore(user)
            syncRoomUserToFirestore(updatedUser)
        }*//*
        //----Syncing Room->Firestore updated/inserted users-------------

        // ✅ Delete all local files once sync is complete from internal storage /data/data/com.yourapp/files/profile_images folder
        // as not required after syn. These files were store temporarily to display from Room before uploading to Firestore
        deleteAllLocalFiles(context)
    }

    // 🔹 Resize & upload image only if profileImageUri exists and is NOT already a valid URL
    private suspend fun uploadImageToFireStore(user: User): User {
        return if (!user.profileImageUri.isNullOrEmpty() && !user.profileImageUri.contains("https")) {
            val imageUri = Uri.parse(user.profileImageUri)
            val resizedImageUrl = firestoreService.uploadResizedImageAndGetUrl(imageUri, user.id, 500, 500)

            if (resizedImageUrl.isEmpty()) {
                Log.d(DebugTags.SYNC_FIRESTORE, "❌ Image not available to upload for user ${user.id}. Proceeding with Firestore sync anyway.")
                user
            } else {
                user.copy(profileImageUri = resizedImageUrl)
            }
        } else {
            user
        }
    }*/
}
