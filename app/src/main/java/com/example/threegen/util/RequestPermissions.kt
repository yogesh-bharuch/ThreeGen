package com.example.threegen.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RequestPermissions(activity: Activity) {
    val context = LocalContext.current

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            Constants.REQUEST_CODE)
    }
}

/*
// Function to format the date
fun formatDateTime(dateTime: String?): String? {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val date = dateTime?.let { inputFormat.parse(it) }
    return date?.let { outputFormat.format(it) }
}

//Text(text = "Created At: ${formatDateTime(member?.createdAt)}")
*/

// Function to format the date from milliseconds
fun formatDateTime(millis: Long?): String {
    millis?.let {
        val date = Date(it)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }
    return ""
}
