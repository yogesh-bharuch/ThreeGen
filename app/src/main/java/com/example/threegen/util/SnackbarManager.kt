
package com.example.threegen.util

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SnackbarManager {
    private val snackbarHostState = SnackbarHostState()

    // Public accessor for the SnackbarHostState
    fun getSnackbarHostState(): SnackbarHostState = snackbarHostState

    // Function to show Snackbar with message, action label, and duration
    fun showMessage(message: String, actionLabel: String? = "OK") {
        CoroutineScope(Dispatchers.Main).launch {
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
        }
    }
}
