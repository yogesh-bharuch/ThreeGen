package com.example.threegen.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

/**
 * Room Database class for the ThreeGen family tree application.
 * - Contains a single entity: `ThreeGen`
 * - Supports database versioning with migration logic.
 */
@Database(entities = [ThreeGen::class], version = 3, exportSchema = false)  // 🔥 Updated to version 3
abstract class ThreeGenDatabase : RoomDatabase() {

    // DAO to interact with ThreeGen table
    abstract fun getThreeGenDao(): ThreeGenDao

    companion object {
        private const val NAME = "three_gen_database"

        @Volatile
        private var INSTANCE: ThreeGenDatabase? = null

        /**
         * Provides a singleton instance of the database.
         * Includes migration logic from versions 1 → 3.
         */
        fun getInstance(context: Context): ThreeGenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThreeGenDatabase::class.java,
                    NAME
                )
                    // ✅ Add migration strategies
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)  // Apply both migrations
                    .fallbackToDestructiveMigration()             // Use for development
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Migration logic from version 1 → version 2:
         * - Adds `isAlive` field with default value `1` (true).
         * - Adds `createdBy` field as nullable TEXT.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // ✅ Add new fields with proper default values
                db.execSQL("ALTER TABLE three_gen_table ADD COLUMN isAlive INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE three_gen_table ADD COLUMN createdBy TEXT")
            }
        }

        /**
         * Migration logic from version 2 → version 3:
         * - Adds `updatedAt` field with default value `0` (timestamp).
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // ✅ Add `updatedAt` field with default value `0`
                db.execSQL("ALTER TABLE three_gen_table ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
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

@Database(entities = [ThreeGen::class], version = 1) // Start fresh from version 1
abstract class ThreeGenDatabase : RoomDatabase() {
    abstract fun getThreeGenDao(): ThreeGenDao

    companion object {
        private const val NAME = "three_gen_database"

        @Volatile
        private var INSTANCE: ThreeGenDatabase? = null

        */
