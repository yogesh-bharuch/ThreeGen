package com.example.threegen.data

import androidx.room.*

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
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var parentID: Int? = null, // Parent's ID (nullable for root members)
    var spouseID: Int? = null, // Spouse's ID (nullable if no spouse)
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val shortName: String,
    val town: String,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)


