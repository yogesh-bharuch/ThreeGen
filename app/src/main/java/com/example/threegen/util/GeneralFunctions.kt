package com.example.threegen.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Copies an image from the given URI to the app's internal storage and returns its new URI as a string.
 * This ensures the image remains accessible even after restarting the app.
 *
 * @param context The application context used to access content resolver and internal storage.
 * @param uri The URI of the selected image, typically obtained from an image picker.
 * @return A string representing the new URI of the copied image, or null if an error occurs.
 */
fun copyToInternalStorage(context: Context, uri: Uri, id: String, maxWidth: Int, maxHeight: Int): String? {
    return try {
        // ✅ Create a dedicated folder for profile images
        val directory = File(context.filesDir, "profile_images").apply { mkdirs() }
        //TODO resize image
        // 🔹 Convert URI to Bitmap
        val originalBitmap = convertUriToBitmap(uri, context)

        // 🔹 Resize Bitmap while preserving aspect ratio
        val resizedBitmap = resizeBitmap(originalBitmap, maxWidth, maxHeight)

        // 🔹 Convert resized Bitmap to InputStream
        val resizedInputStream = convertBitmapToInputStream(resizedBitmap)


        // ✅ Generate a unique filename using the current timestamp
        val fileName = "$id.jpg"
        //val fileName = "profile_image_${System.currentTimeMillis()}.jpg"
        val file = File(directory, fileName)

        file.outputStream().use { outputStream ->
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        }

        // ✅ Convert the newly created file to a URI and return it as a string
        file.toUri().toString()

    } catch (e: Exception) {
        e.printStackTrace()
        null // ❌ Return null if any error occurs
    }
}

fun formatTimeStampToDateTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp)) // ✅ Convert milliseconds to formatted date-time string
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
/*

    fun deleteAllLocalFiles(context: Context) {
        // ✅ Delete all local files once sync is complete from internal storage /data/data/com.yourapp/files/profile_images
        val directory = File(context.filesDir, "profile_images") // Get local folder

        if (directory.exists() && directory.isDirectory) {
            //Log.d("ImageStorage", "Deleting files in: ${directory.absolutePath}")
            directory.listFiles()?.forEach { file ->
                if (file.exists()) {
                    val deleted = file.delete()
                    Log.d(DebugTags.SYNC_FIRESTORE, "Deleted: ${file.name}, Success: $deleted")
                }
            }
        } else {
            Log.d(DebugTags.SYNC_FIRESTORE, "⚠️ File not found: $directory - Skipping operation.")
        }
    }*/

fun deleteAllLocalFiles(context: Context) {
    val directory = File(context.filesDir, "profile_images") // Get local folder

    if (directory.exists() && directory.isDirectory) {
        Log.d(DebugTags.SYNC_FIRESTORE, "🗑️ Deleting files in: ${directory.absolutePath}")

        directory.listFiles()?.let { files ->
            files.forEach { file ->
                try {
                    if (file.exists()) {
                        val deleted = file.delete()
                        if (deleted) {
                            Log.d(DebugTags.SYNC_FIRESTORE, "✅ Deleted: ${file.name}")
                        } else {
                            Log.e(DebugTags.SYNC_FIRESTORE, "❌ Failed to delete: ${file.name}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(DebugTags.SYNC_FIRESTORE, "❌ Error deleting file ${file.name}: ${e.message}")
                }
            }
        } ?: Log.d(DebugTags.SYNC_FIRESTORE, "⚠️ No files found in directory - Skipping operation.")
    } else {
        Log.d(DebugTags.SYNC_FIRESTORE, "⚠️ Directory not found: $directory - Skipping operation.")
    }
}

// 🔹 Upload resized image to Firestore Storage
/*

//-------------------image processing and uploading-------
suspend fun uploadResizedImageAndGetUrl(uri: Uri, uuid: String, context: Context, maxWidth: Int, maxHeight: Int): String = withContext(
    Dispatchers.IO) {
    //Log.d(DebugTags.SYNC_FIRESTORE, "📸 Uploading resized image to Firestore Storage. From Fireservice.uploadResizedImage")
    return@withContext try {
        // 🔹 Convert URI to Bitmap
        val originalBitmap = convertUriToBitmap(uri, context)

        // 🔹 Resize Bitmap while preserving aspect ratio
        val resizedBitmap = resizeBitmap(originalBitmap, maxWidth, maxHeight)

        // 🔹 Convert resized Bitmap to InputStream
        val resizedInputStream = convertBitmapToInputStream(resizedBitmap)

        // 🔹 Upload to Firebase Storage and return URL
        uploadImageToFirebase(resizedInputStream, uuid)
    } catch (e: Exception) {
        Log.e("FirestoreTaskService", "❌ Image upload failed: ${e.message} From Fireservice.uploadResizedImage")
        throw e
    }
}
*/

// 🔹 Convert URI to Bitmap called 1️⃣  from uploadResizedImageAndGetUrl
private fun convertUriToBitmap(uri: Uri, context: Context): Bitmap {
    //Log.d(DebugTags.SYNC_FIRESTORE, "🖼 Converting URI to Bitmap. From Fireservice.uploadResizedImage")
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw SecurityException("Cannot open URI, permission required.")

        return BitmapFactory.decodeStream(inputStream)
    } catch (e: SecurityException) {
        Log.e("ImageProcessing", "❌ SecurityException: ${e.message}")
        throw e // Ensure this is handled in calling functions
    }
}

// 🔹 Resize Bitmap while maintaining aspect ratio called 2️⃣ from uploadResizedImageAndGetUrl
private fun resizeBitmap(originalBitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    //Log.d(DebugTags.SYNC_FIRESTORE, "🔍 Resizing Bitmap. From Fireservice.uploadResizedImage")
    val aspectRatio = originalBitmap.width.toDouble() / originalBitmap.height
    val newWidth = if (aspectRatio > 1) maxWidth else (maxHeight * aspectRatio).toInt()
    val newHeight = if (aspectRatio > 1) (maxWidth / aspectRatio).toInt() else maxHeight
    return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
}

// 🔹 Convert Bitmap to InputStream called 3️⃣ from uploadResizedImageAndGetUrl
private fun convertBitmapToInputStream(bitmap: Bitmap): ByteArrayInputStream {
    //Log.d(DebugTags.SYNC_FIRESTORE, "📄 Converting Bitmap to InputStream. From Fireservice.uploadResizedImage")
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
    return ByteArrayInputStream(outputStream.toByteArray())
}

/*

// 🔹 Upload Image to Firebase Storage and Return URL called 4️⃣ from uploadResizedImageAndGetUrl
private suspend fun uploadImageToFirebase(inputStream: ByteArrayInputStream, uuid: String): String {
    return withContext(Dispatchers.IO) { // ✅ Run inside IO dispatcher for better performance
        try {
            //Log.d(DebugTags.SYNC_FIRESTORE, "☁️ Uploading image to Firebase Storage. From Fireservice.uploadImage")
            val imageRef = storage.reference.child("users/$uuid.jpg")

            // 🔹 Upload image using coroutines
            imageRef.putStream(inputStream).await()

            // 🔹 Retrieve download URL after upload
            val downloadUrl = imageRef.downloadUrl.await().toString()

            // ✅ Log the URL for debugging
            //Log.d(DebugTags.SYNC_FIRESTORE, "✅ Image uploaded successfully. Download URL: $downloadUrl From Fireservice.uploadImage")
            return@withContext downloadUrl
        } catch (e: Exception) {
            Log.e("FirestoreTaskService", "❌ Failed to upload image for user $uuid: ${e.message} From Fireservice.uploadImage")
            throw e // ✅ Rethrow exception for handling in repository level
        }
    }
}
//-------------------image processing and uploading-------
*/


object DebugTags {
    const val SYNC_FIRESTORE = "SyncLocalToFireStore"
    const val WORK_MANAGER = "WorkManagerHelper"
    const val DATABASE_SYNC = "DatabaseSync"
}