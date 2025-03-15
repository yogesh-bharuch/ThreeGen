
package com.example.threegen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth

/**
 * Factory class to provide AuthViewModel with FirebaseAuth dependency.
 */
class AuthViewModelFactory(
    private val auth: FirebaseAuth
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(auth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

