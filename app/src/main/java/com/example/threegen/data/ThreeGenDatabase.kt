package com.example.threegen.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ThreeGen::class], version = 1)
abstract class ThreeGenDatabase : RoomDatabase() {
    abstract fun getThreeGenDao(): ThreeGenDao

    companion object {
        const val NAME = "three_gen_database"
    }
}

