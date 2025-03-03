package com.example.threegen

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.threegen.data.ThreeGenDatabase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainApplication : Application() {

    companion object {
        lateinit var threeGenDatabase: ThreeGenDatabase
        lateinit var firestore: FirebaseFirestore
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        threeGenDatabase = Room.databaseBuilder(
            applicationContext,
            ThreeGenDatabase::class.java,
            ThreeGenDatabase.NAME
        )
            .fallbackToDestructiveMigration()
            .build()
        FirebaseApp.initializeApp(this)
    }

}

