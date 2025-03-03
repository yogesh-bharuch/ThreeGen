package com.example.threegen.data

import android.util.Log
import android.widget.Toast
import com.example.threegen.MainApplication
import com.example.threegen.data.ThreeGen
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ThreeGenFirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collectionRef = firestore.collection("Members")

    /**
     * Adds a new member to Firestore or updates an existing one if the document ID already exists.
     *
     * @param member The `ThreeGen` member object to be added to Firestore.
     * @return A `Result<Unit>` indicating success or failure of the operation.
     */
    suspend fun addMemberToFirestore(member: ThreeGen): Result<Unit> {
        return try {
            // Convert the member's ID to a string and use it as the Firestore document ID
            // If the document with this ID already exists, it will be replaced with the new data.
            collectionRef.document(member.id.toString()).set(member).await()

            // Log a success message for debugging and tracking purposes
            Log.d("FirestoreViewModelRepo", "Member added: ${member.id}")

            // Return success if the operation was completed successfully
            Result.success(Unit)
        } catch (e: Exception) {
            // Log an error message if the operation fails
            Log.e("FirestoreViewModelRepo", "Error adding member", e)

            // Return failure with the exception details, so the caller can handle it
            Result.failure(e)
        }
    }

    /**
     * Updates specific fields of a member in Firestore.
     * @param memberId The ID of the member to update.
     * @param updates A map of fields to update.
     * @return Result indicating success or failure.
     */
    suspend fun updateMemberInFirestore(memberId: String, updates: Map<String, Any?>): Result<Unit> {
        return try {
            // Filter out null values from the update map to avoid overwriting existing Firestore fields with null
            val filteredUpdates = updates.filterValues { it != null }

            // Proceed only if there are valid updates to apply
            if (filteredUpdates.isNotEmpty()) {
                // Update only the specified fields in Firestore without replacing the entire document
                collectionRef.document(memberId).update(filteredUpdates).await()

                // Log the successful update along with the updated fields
                Log.d("Firestore", "Member updated: $memberId with $filteredUpdates")
            } else {
                // Log a warning if no valid updates were provided (prevents unnecessary Firestore operations)
                Log.w("Firestore", "No valid updates provided for $memberId")
            }

            // Return success if everything went well
            Result.success(Unit)
        } catch (e: Exception) {
            // Log an error message in case of failure
            Log.e("Firestore", "Error updating member", e)

            // Return failure along with the exception details
            Result.failure(e)
        }
    }

    /**
     * Deletes a member document from Firestore.
     *
     * @param memberId The Firestore document ID of the member to be deleted.
     * @return A Result<Unit> indicating success or failure.
     */
    suspend fun deleteMemberFromFirestore(memberId: String): Result<Unit> {
        return try {
            // Attempt to delete the document with the given member ID from Firestore
            collectionRef.document(memberId).delete().await()

            // Log a success message for debugging and tracking purposes
            Log.d("Firestore", "Member deleted: $memberId")

            // Return success if the deletion was successful
            Result.success(Unit)
        } catch (e: Exception) {
            // Log an error message in case the deletion fails
            Log.e("Firestore", "Error deleting member", e)

            // Return failure with the exception details, so the caller can handle it
            Result.failure(e)
        }
    }

    /**
     * Fetches a member from Firestore.
     * @param memberId The ID of the member to fetch.
     * @return Result containing the member object or null if not found.
     */
    suspend fun getMemberFromFirestore(memberId: String): Result<ThreeGen?> {
        return try {
            val document = collectionRef.document(memberId).get().await()
            if (document.exists()) {
                val member = document.toObject(ThreeGen::class.java)
                Log.d("Firestore", "Fetched member: $memberId")
                Result.success(member)
            } else {
                Log.w("Firestore", "Member not found: $memberId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching member", e)
            Result.failure(e)
        }
    }
}
