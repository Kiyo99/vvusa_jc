package com.example.vvusa_jc.ui.auth

/**
 * Represents the different states of the Authentication UI
 * This follows the UI State pattern in MVVM architecture
 */
sealed class AuthUiState {
    /** Initial state when the screen is first displayed */
    object Initial : AuthUiState()

    /** Loading state while authentication is in progress */
    object Loading : AuthUiState()

    /** Success state when authentication is successful */
    object Success : AuthUiState()

    /** Error state with a message explaining what went wrong */
    data class Error(val message: String) : AuthUiState()
}