
package com.example.threegen.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import java.util.UUID

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
        Index(value = ["shortName"], unique = true),
        Index(value = ["parentID"]),
        Index(value = ["spouseID"]),
        Index(value = ["deleted"]) // Add index on 'deleted' field
    ]
)
data class ThreeGen(
    @PrimaryKey val id: String = UUID.randomUUID().toString(), // Using UUID for consistency
    var parentID: String? = null, // Nullable for root members
    var spouseID: String? = null, // Nullable if no spouse
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val town: String,
    val shortName: String, // Generated in ViewModel
    var imageUri: String? = null, // Nullable, stored in Firebase Storage
    val childNumber: Int?, // Nullable and manually entered
    val comment: String?, // Nullable comment field
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED, // Sync tracking for Firestore
    var deleted: Boolean = false, // New field to track deletions
    var createdBy: String? = null // New field to track the creator
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readLong(),
        SyncStatus.valueOf(parcel.readString() ?: "NOT_SYNCED"),
        parcel.readByte() != 0.toByte(), // Read Boolean as byte
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(parentID)
        parcel.writeString(spouseID)
        parcel.writeString(firstName)
        parcel.writeString(middleName)
        parcel.writeString(lastName)
        parcel.writeString(town)
        parcel.writeString(shortName)
        parcel.writeString(imageUri)
        parcel.writeValue(childNumber)
        parcel.writeString(comment)
        parcel.writeLong(createdAt)
        parcel.writeString(syncStatus.name)
        parcel.writeByte(if (deleted) 1 else 0) // Write Boolean as byte
        parcel.writeString(createdBy)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ThreeGen> {
        override fun createFromParcel(parcel: Parcel): ThreeGen {
            return ThreeGen(parcel)
        }

        override fun newArray(size: Int): Array<ThreeGen?> {
            return arrayOfNulls(size)
        }
    }
}

enum class SyncStatus {
    NOT_SYNCED, UPDATED, SYNCED
}





















/*
package com.example.threegen.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import java.util.UUID

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
        Index(value = ["shortName"], unique = true),
        Index(value = ["parentID"]),
        Index(value = ["spouseID"])
    ]
)
data class ThreeGen(
    @PrimaryKey val id: String = UUID.randomUUID().toString(), // Using UUID for consistency
    var parentID: String? = null, // Nullable for root members
    var spouseID: String? = null, // Nullable if no spouse
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val town: String,
    val shortName: String, // Generated in ViewModel
    var imageUri: String? = null, // Nullable, stored in Firebase Storage
    val childNumber: Int?, // Nullable and manually entered
    val comment: String?, // Nullable comment field
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED // Sync tracking for Firestore
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readLong(),
        SyncStatus.valueOf(parcel.readString() ?: "NOT_SYNCED")
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(parentID)
        parcel.writeString(spouseID)
        parcel.writeString(firstName)
        parcel.writeString(middleName)
        parcel.writeString(lastName)
        parcel.writeString(town)
        parcel.writeString(shortName)
        parcel.writeString(imageUri)
        parcel.writeValue(childNumber)
        parcel.writeString(comment)
        parcel.writeLong(createdAt)
        parcel.writeString(syncStatus.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ThreeGen> {
        override fun createFromParcel(parcel: Parcel): ThreeGen {
            return ThreeGen(parcel)
        }

        override fun newArray(size: Int): Array<ThreeGen?> {
            return arrayOfNulls(size)
        }
    }
}

enum class SyncStatus {
    NOT_SYNCED, UPDATED, SYNCED
}

*/