package com.example.vvusa_jc.domain.model

/**
 * Represents the result of authentication operations in the domain layer
 * This is a generic class that can wrap any type of data
 */
sealed class AuthResult<out T> {
    /**
     * Represents a successful operation with the data
     */
    data class Success<T>(val data: T) : AuthResult<T>()

    /**
     * Represents a failed operation with an error message
     */
    data class Error(val message: String) : AuthResult<Nothing>()

    /**
     * Represents an operation in progress
     */
    object Loading : AuthResult<Nothing>()
}