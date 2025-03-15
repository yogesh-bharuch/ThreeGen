package com.example.threegen

import android.app.Application
import androidx.room.Room
import com.example.threegen.data.ThreeGenDatabase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MainApplication : Application() {

    companion object {
        lateinit var instance: MainApplication
            private set

        lateinit var threeGenDatabase: ThreeGenDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Room database
        threeGenDatabase = Room.databaseBuilder(
            applicationContext,
            ThreeGenDatabase::class.java,
            "three_gen_database"
        )
            .fallbackToDestructiveMigration()
            .build()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Enable Firestore offline persistence
        val firestore = FirebaseFirestore.getInstance()

        // customize settings (like cache size), you can do so like this:
        /*
        val settings = FirebaseFirestore.getInstance().firestoreSettings.toBuilder()
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        firestore.firestoreSettings = settings

          */
    }
}
























/*

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



 */