
package com.example.threegen.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Database(entities = [ThreeGen::class], version = 1) // Start fresh from version 1
abstract class ThreeGenDatabase : RoomDatabase() {
    abstract fun getThreeGenDao(): ThreeGenDao

    companion object {
        private const val NAME = "three_gen_database"

        @Volatile
        private var INSTANCE: ThreeGenDatabase? = null

        /**
         * Provides an instance of the database, ensuring a single instance exists (Singleton Pattern).
         */
        fun getInstance(context: Context): ThreeGenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThreeGenDatabase::class.java,
                    NAME
                )
                    .fallbackToDestructiveMigration() // Use this for development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}













/*
package com.example.threegen.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration



@Database(entities = [ThreeGen::class], version = 2) // Increment version for new changes
abstract class ThreeGenDatabase : RoomDatabase() {
    abstract fun getThreeGenDao(): ThreeGenDao

    companion object {
        private const val NAME = "three_gen_database"

        @Volatile
        private var INSTANCE: ThreeGenDatabase? = null

        /**
         * Provides an instance of the database, ensuring a single instance exists (Singleton Pattern).
         * Uses Room.databaseBuilder for migration support.
         */
        fun getInstance(context: Context): ThreeGenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThreeGenDatabase::class.java,
                    NAME
                )
                    .addMigrations(MIGRATION_1_2) // Add future migrations here
                    .fallbackToDestructiveMigration() // WARNING: Use only for debugging, removes all data on failure
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Migration from version 1 to version 2.
         * Adds new fields `childNumber` and `comment` to the `ThreeGen` table.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE three_gen ADD COLUMN childNumber INTEGER")
                db.execSQL("ALTER TABLE three_gen ADD COLUMN comment TEXT")
            }
        }
    }
}

*/