package com.example.threegen.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import java.util.UUID

/**
 * Room Entity representing a member in the family tree.
 * - Supports Firestore sync with `updatedAt` and `deleted`
 * - Parcelable for passing between screens
 */
@Entity(
    tableName = "three_gen_table",
    foreignKeys = [
        ForeignKey(
            entity = ThreeGen::class,
            parentColumns = ["id"],
            childColumns = ["parentID"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ThreeGen::class,
            parentColumns = ["id"],
            childColumns = ["spouseID"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["shortName"], unique = true),    // Unique short name
        Index(value = ["parentID"]),                    // Index for faster lookups
        Index(value = ["spouseID"]),                    // Index for faster lookups
        Index(value = ["deleted"])                      // Index for deleted records
    ]
)
data class ThreeGen(
    // ✅ Core member details
    val firstName: String,                      // Member's first name
    val middleName: String?,                    // Middle name (nullable)
    val lastName: String,                       // Last name
    val town: String,                           // Town
    val shortName: String,                      // Generated short name
    var isAlive: Boolean = true,                // New field: true by default
    val childNumber: Int?,                      // Child number (nullable)
    val comment: String?,                       // Comment (nullable)
    var imageUri: String? = null,               // Image URI (nullable)

    // ✅ Sync and metadata fields
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,  // Sync tracking status
    var deleted: Boolean = false,               // Local delete flag
    val createdAt: Long = System.currentTimeMillis(),    // Creation timestamp
    var createdBy: String? = null,              // Creator ID (nullable)
    var updatedAt: Long = 0L,                   // 🔥 New field: For Firestore → Room sync

    // ✅ ID fields at the end (Firestore consistency)
    @PrimaryKey val id: String = UUID.randomUUID().toString(),  // Unique ID
    var parentID: String? = null,               // Parent ID (nullable)
    var spouseID: String? = null                // Spouse ID (nullable)
) : Parcelable {

    // ✅ Constructor to read all fields from Parcel
    constructor(parcel: Parcel) : this(
        firstName = parcel.readString() ?: "",
        middleName = parcel.readString(),
        lastName = parcel.readString() ?: "",
        town = parcel.readString() ?: "",
        shortName = parcel.readString() ?: "",
        isAlive = parcel.readByte() != 0.toByte(),   // Read Boolean as byte
        childNumber = parcel.readValue(Int::class.java.classLoader) as? Int,
        comment = parcel.readString(),
        imageUri = parcel.readString(),
        syncStatus = SyncStatus.valueOf(parcel.readString() ?: "NOT_SYNCED"),
        deleted = parcel.readByte() != 0.toByte(),
        createdAt = parcel.readLong(),
        createdBy = parcel.readString(),
        updatedAt = parcel.readLong(),               // ✅ Read `updatedAt`
        id = parcel.readString() ?: UUID.randomUUID().toString(),
        parentID = parcel.readString(),
        spouseID = parcel.readString()
    )

    // ✅ Write all fields to Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        // ✅ Core member details
        parcel.writeString(firstName)
        parcel.writeString(middleName)
        parcel.writeString(lastName)
        parcel.writeString(town)
        parcel.writeString(shortName)
        parcel.writeByte(if (isAlive) 1 else 0)           // Write Boolean as byte
        parcel.writeValue(childNumber)
        parcel.writeString(comment)
        parcel.writeString(imageUri)

        // ✅ Sync and metadata fields
        parcel.writeString(syncStatus.name)                // Enum as String
        parcel.writeByte(if (deleted) 1 else 0)            // Boolean as byte
        parcel.writeLong(createdAt)
        parcel.writeString(createdBy)
        parcel.writeLong(updatedAt)                        // ✅ Write `updatedAt`

        // ✅ ID fields
        parcel.writeString(id)
        parcel.writeString(parentID)
        parcel.writeString(spouseID)
    }

    // ✅ Describe contents (required for Parcelable)
    override fun describeContents(): Int = 0

    // ✅ Parcelable Creator
    companion object CREATOR : Parcelable.Creator<ThreeGen> {
        override fun createFromParcel(parcel: Parcel): ThreeGen = ThreeGen(parcel)
        override fun newArray(size: Int): Array<ThreeGen?> = arrayOfNulls(size)
    }
}

/**
 * Enum to track sync status between Room and Firestore.
 */
enum class SyncStatus {
    NOT_SYNCED, UPDATED, SYNCED
}

