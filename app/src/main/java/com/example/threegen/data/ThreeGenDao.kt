package com.example.threegen.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ThreeGenDao {

    @Query("SELECT * FROM three_gen_table ORDER BY createdAt DESC")
    fun getAllThreeGenAsObject(): List<ThreeGen>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(threeGen: ThreeGen): Long

    @Upsert
    suspend fun addThreeGen(threeGen: ThreeGen): Long

    @Update
    suspend fun updateThreeGen(threeGen: ThreeGen)

    @Update
    suspend fun updateMember(member: ThreeGen)


    @Delete
    suspend fun deleteThreeGen(threeGen: ThreeGen)

    @Query("SELECT * FROM three_gen_table ORDER BY createdAt DESC")
    fun getAllThreeGen(): LiveData<List<ThreeGen>>

    @Query("SELECT * FROM three_gen_table WHERE id = :id")
    fun getMemberById(id: Int): LiveData<ThreeGen?>

    @Query("SELECT COUNT(*) FROM three_gen_table WHERE shortName = :shortName")
    suspend fun getShortNameCount(shortName: String): Int

    // New method to get a member by ID synchronously
    @Query("SELECT * FROM three_gen_table WHERE id = :id")
    fun getMemberByIdSync(id: Int): ThreeGen?

    // New method to get member and spouse data
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
    fun getMemberAndSpouseData(memberId: Int): MemberAndSpouseData?

    // New method to get siblings
    @Query("""SELECT * FROM three_gen_table
                WHERE three_gen_table.parentID = (SELECT  three_gen_table.parentID FROM three_gen_table WHERE three_gen_table.id = :memberId)
                AND three_gen_table.id != :memberId
            """)
    fun getSiblings(memberId: Int):  LiveData<List<ThreeGen>>

    // Optional methods for getting children and spouse (if needed)
    @Query("SELECT * FROM three_gen_table WHERE parentID = :parentId")
    fun getChildren(parentId: Int): LiveData<List<ThreeGen>>

    @Query("SELECT * FROM three_gen_table WHERE spouseID = :spouseId")
    fun getSpouse(spouseId: Int): LiveData<ThreeGen?>

    //fun getSiblings1(memberId: Int): List<ThreeGen>  :memberId
    //@Query("""SELECT * FROM three_gen_table
    //            WHERE three_gen_table.parentID = (SELECT  three_gen_table.parentID FROM three_gen_table WHERE three_gen_table.id = :memberId)
    //            AND three_gen_table.id != :memberId
    //        """)
}


/*
 // New method to get member and spouse data
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
    fun getMemberAndSpouseData(memberId: Int): MemberAndSpouseData?


 */