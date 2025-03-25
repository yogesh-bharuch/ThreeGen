package com.example.threegen.util

import android.content.Context
import android.content.SharedPreferences

/**
 * 🔥 Helper object to store and retrieve the last Firestore → Room sync timestamp.
 * This prevents redundant syncs by only fetching new or modified members.
 * called this from the repository when performing sync operations
 */
object SyncPreferences {

    private const val PREF_NAME = "sync_prefs"                // SharedPreferences file name
    private const val LAST_SYNC_TIMESTAMP = "last_sync_timestamp"  // Key for storing last sync time

    // ✅ Get SharedPreferences instance
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * ✅ Get the last sync timestamp
     * @param context - Application context
     * @return last sync timestamp or 0L if not found
     */
    fun getLastSyncTimestamp(context: Context): Long {
        return getPrefs(context).getLong(LAST_SYNC_TIMESTAMP, 0L)
    }

    /**
     * ✅ Store the new last sync timestamp
     * @param context - Application context
     * @param timestamp - The latest sync timestamp
     */
    fun setLastSyncTimestamp(context: Context, timestamp: Long) {
        getPrefs(context).edit().putLong(LAST_SYNC_TIMESTAMP, timestamp).apply()
    }
}
