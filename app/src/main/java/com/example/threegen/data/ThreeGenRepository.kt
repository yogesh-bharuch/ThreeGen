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