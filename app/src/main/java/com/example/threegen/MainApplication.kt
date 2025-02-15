package com.example.threegen

import android.app.Application
import androidx.room.Room
import com.example.threegen.data.ThreeGenDatabase

class MainApplication : Application() {

    companion object {
        lateinit var threeGenDatabase: ThreeGenDatabase
    }

    override fun onCreate() {
        super.onCreate()
        threeGenDatabase = Room.databaseBuilder(
            applicationContext,
            ThreeGenDatabase::class.java,
            ThreeGenDatabase.NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}

