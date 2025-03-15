package com.example.threegen.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Sealed class representing authentication state.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * ViewModel to manage Firebase Authentication logic.
 */
class AuthViewModel(private val auth: FirebaseAuth) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Current Firebase user
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Manually refresh the user and update the state.
     */
    fun refreshUser() {
        currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful && currentUser != null) {
                _authState.value = AuthState.Success(currentUser!!.uid)
            } else {
                _authState.value = AuthState.Idle
            }
        }
    }

    /**
     * Check if the user is logged in.
     */
    fun isUserLoggedIn(): Boolean = currentUser != null

    /**
     * Login with email and password.
     */
    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = currentUser?.uid ?: ""
                    _authState.value = AuthState.Success(userId)
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login failed.")
                }
            }
    }

    /**
     * Register a new user with email and password.
     */
    fun register(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Reload the user to ensure currentUser is updated properly
                    currentUser?.reload()?.addOnSuccessListener {
                        val userId = currentUser?.uid ?: ""
                        _authState.value = AuthState.Success(userId)
                    }?.addOnFailureListener {
                        _authState.value = AuthState.Error("User registered but failed to retrieve user ID.")
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Registration failed.")
                }
            }
    }

    /**
     * Logout the current user.
     */
    fun logout() {
        auth.signOut()
        // Refresh the user state after sign out
        _authState.value = AuthState.Idle
    }

    /**
     * Check the current user status on app start.
     */
    fun checkCurrentUser() {
        refreshUser()
    }
}
