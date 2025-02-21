package com.example.threegen.util

import com.example.threegen.data.ThreeGen

object Constants {
    const val REQUEST_CODE = 1
}

sealed class MemberState {
    object Loading : MemberState()
    data class Success(val member: ThreeGen) : MemberState()
    object Error : MemberState()
}