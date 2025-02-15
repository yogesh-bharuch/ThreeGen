package com.example.threegen.data

import kotlinx.coroutines.delay
import java.lang.reflect.Member

class MemberRepository{
   suspend fun fetchMemberData(): List<Member> {
        delay(2000)
        return emptyList()
    }
}