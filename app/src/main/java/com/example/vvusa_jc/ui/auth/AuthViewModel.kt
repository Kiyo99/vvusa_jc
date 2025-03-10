package com.example.vvusa_jc.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for handling authentication-related operations
 * This follows the MVVM pattern for separation of concerns
 */
class AuthViewModel : ViewModel() {

    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // UI state flows
    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val loginState: StateFlow<AuthUiState> = _loginState

    private val _registerState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val registerState: StateFlow<AuthUiState> = _registerState

    /**
     * Attempt to log in a user with email and password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _loginState.value = AuthUiState.Loading

                // Attempt to sign in with Firebase Auth
                auth.signInWithEmailAndPassword(email, password).await()

                // If we get here, login was successful
                _loginState.value = AuthUiState.Success
            } catch (e: Exception) {
                // Handle specific error types if needed
                val errorMessage = when {
                    e.message?.contains("no user record") == true -> "No account found with this email"
                    e.message?.contains("password is invalid") == true -> "Invalid password"
                    else -> e.message ?: "Login failed"
                }

                _loginState.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    /**
     * Register a new user with email and password
     */
    fun register(name: String, email: String, password: String, studentId: String) {
        viewModelScope.launch {
            try {
                _registerState.value = AuthUiState.Loading

                // Create the user account
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid

                if (userId != null) {
                    // Create a user document in Firestore
                    val userMap = hashMapOf(
                        "id" to userId,
                        "name" to name,
                        "email" to email,
                        "studentId" to studentId,
                        "createdAt" to System.currentTimeMillis()
                    )

                    // Add the user document to Firestore
                    firestore.collection("users").document(userId).set(userMap).await()

                    _registerState.value = AuthUiState.Success
                } else {
                    _registerState.value = AuthUiState.Error("Registration failed")
                }
            } catch (e: Exception) {
                // Handle specific error types
                val errorMessage = when {
                    e.message?.contains("email address is already in use") == true ->
                        "An account with this email already exists"
                    e.message?.contains("password is invalid") == true ->
                        "Password should be at least 6 characters"
                    else -> e.message ?: "Registration failed"
                }

                _registerState.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    /**
     * Send a password reset email
     */
    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("no user record") == true ->
                        "No account found with this email"
                    else -> e.message ?: "Failed to send reset email"
                }
                onError(errorMessage)
            }
        }
    }

    /**
     * Reset the login state to initial
     */
    fun resetLoginState() {
        _loginState.value = AuthUiState.Initial
    }

    /**
     * Reset the register state to initial
     */
    fun resetRegisterState() {
        _registerState.value = AuthUiState.Initial
    }
}