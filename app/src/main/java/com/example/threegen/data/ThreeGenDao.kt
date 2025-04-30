
package com.example.threegen.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreeGenDao {

    /**
     * Retrieves all members sorted by creation date (newest first).
     * Returns a **Flow**, making it reactive and Compose-friendly.
     */
    @Query("SELECT * FROM three_gen_table WHERE deleted = 0 ORDER BY createdAt DESC")
    fun getAllThreeGen(): Flow<List<ThreeGen>>

    /**
     * Inserts a new member. If it exists, replaces the old entry.
     * Returns the row ID of the inserted item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(threeGen: ThreeGen): Long

    /**
     * Inserts a list of members into Room.
     * Uses `REPLACE` strategy to avoid duplication issues during Firestore sync.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMembers(members: List<ThreeGen>): List<Long>

    //val rowIds =  insertOrUpdateMembers(members: List<ThreeGen>)

    /**
     * Clears all local data. (For full sync restoration)
     */
    @Query("DELETE FROM three_gen_table")
    suspend fun clearAll()

    /**
     * Updates the parent and spouse references for a given member.
     *
     * @param id The member ID.
     * @param parentID The parent ID (nullable).
     * @param spouseID The spouse ID (nullable).
     */
    @Query("""
    UPDATE three_gen_table 
    SET parentID = :parentID, spouseID = :spouseID 
    WHERE id = :id
""")
    suspend fun updateRelationships(id: String, parentID: String?, spouseID: String?)

    @Query("UPDATE three_gen_table SET parentid = null WHERE parentid = :threeGenId")
    suspend fun removeParentRef(threeGenId: String)
    @Query("UPDATE three_gen_table SET spouseid = null WHERE spouseid = :threeGenId")
    suspend fun removeSpouseRef(threeGenId: String)

    /**
     * **Upserts** (Insert or Update) a member into the database.
     * If the member already exists, it updates the entry.
     */
    @Upsert
    suspend fun addThreeGen(threeGen: ThreeGen): Long

    /**
     * Updates an existing member.
     */
    @Update
    suspend fun updateThreeGen(member: ThreeGen): Int

    /**
     * Marks a specific member as deleted.
     */
    @Query("UPDATE three_gen_table SET deleted = 1, syncStatus = 'UPDATED' WHERE id = :threeGenId")
    suspend fun markAsDeleted(threeGenId: String)

    @Query("SELECT * FROM three_gen_table WHERE syncStatus != :syncStatus AND deleted = 0")
    suspend fun getUnsyncedMembers(syncStatus: SyncStatus = SyncStatus.SYNCED): List<ThreeGen>

    /**
     * delete member by id.
     */
    @Query("DELETE FROM three_gen_table WHERE id = :threeGenId")
    suspend fun deleteThreeGen(threeGenId: String)

    // used in firestore update logic
    @Query("SELECT * FROM three_gen_table WHERE parentID = :memberId OR spouseID = :memberId")
    suspend fun getMembersReferencing(memberId: String): List<ThreeGen>

    /**
     * Retrieves a **specific member** by their ID as Flow.
     * Emits updates whenever the data changes.
     */
    @Query("SELECT * FROM three_gen_table WHERE id = :id AND deleted = 0")
    fun getMemberById(id: String): Flow<ThreeGen?>

    /**
     * Retrieves a **specific member** by their ID synchronously.
     * Used for cases where a **coroutine** is not needed.
     */
    @Query("SELECT * FROM three_gen_table WHERE id = :id AND deleted = 0")
    suspend fun getMemberByIdSync(id: String): ThreeGen?

    @Query("SELECT * FROM three_gen_table WHERE id = :id")
    suspend fun getMarkedAsDeletedMemberByIdSync(id: String): ThreeGen?

    /**
     * Retrieves the count of members with a given `shortName`.
     * Helps ensure uniqueness.
     */
    @Query("SELECT COUNT(*) FROM three_gen_table WHERE shortName = :shortName AND deleted = 0")
    suspend fun getShortNameCount(shortName: String): Int

    /*
    /**
     * Retrieves a **member and their spouse's details** (full names, town, and image).
     * Uses a **LEFT JOIN** to get spouse details if available.
     */
    @Query("""
        SELECT
            member.firstName || ' ' || member.middleName || ' ' || member.lastName AS memberFullName,
            member.town AS memberTown,
            member.imageUri AS memberImageUri,
            spouse.firstName || ' ' || spouse.middleName || ' ' || spouse.lastName AS spouseFullName,
            spouse.town AS spouseTown,
            spouse.imageUri AS spouseImageUri
        FROM three_gen_table AS member
        LEFT JOIN three_gen_table AS spouse ON member.spouseID = spouse.id
        WHERE member.id = :memberId
    """)
    fun getMemberAndSpouseData(memberId: String): Flow<MemberAndSpouseData?>
    */

    /**
     * Retrieves **siblings** of a given member.
     * Excludes the given member itself.
     */
    @Query("""
        SELECT * FROM three_gen_table
        WHERE parentID = (SELECT parentID FROM three_gen_table WHERE id = :memberId)
        AND id != :memberId AND deleted = 0
    """)
    fun getSiblings(memberId: String): Flow<List<ThreeGen>>

    /**
     * Retrieves **children** of a specific parent.
     * Uses Flow for reactivity.
     */
    @Query("SELECT * FROM three_gen_table WHERE parentID = :parentId AND deleted = 0")
    fun getChildren(parentId: String): Flow<List<ThreeGen>>

    /**
     * Retrieves **children** synchronously for background tasks.
     */
    @Query("SELECT * FROM three_gen_table WHERE parentID = :parentId AND deleted = 0")
    suspend fun getChildrenByParentId(parentId: String): List<ThreeGen>

    /**
     * Retrieves **spouse** of a given member.
     */
    @Query("SELECT * FROM three_gen_table WHERE spouseID = :spouseId AND deleted = 0")
    fun getSpouse(spouseId: String): Flow<ThreeGen?>

    @Query("SELECT * FROM three_gen_table WHERE deleted = 0 ORDER BY createdAt DESC")
    suspend fun getAllMembers(): List<ThreeGen>


    @Query("SELECT * FROM three_gen_table WHERE deleted = 1")
    suspend fun getMarkAsDeletedMembers(): List<ThreeGen>


    @Query("SELECT * FROM three_gen_table WHERE shortName LIKE '%' || :query || '%' AND deleted = 0")
    suspend fun searchMembers(query: String): List<ThreeGen>
}
