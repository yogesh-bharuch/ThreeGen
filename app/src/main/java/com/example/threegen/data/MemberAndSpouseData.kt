package com.example.threegen.data

data class MemberAndSpouseData(
    val memberFullName: String,
    val memberTown: String,
    val memberImageUri: String?,
    val spouseFullName: String?,
    val spouseTown: String?,
    val spouseImageUri: String?
)