package com.pulsehealth.app.data

/**
 * Represents an authenticated or guest user profile in Pulse Health.
 */
data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String? = null,
    val isAnonymous: Boolean = false,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

/**
 * High-level authentication and session states.
 */
sealed interface AuthState {
    data object Loading : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
    data object Guest : AuthState
    data object Unauthenticated : AuthState
}

/**
 * Result of authentication operations.
 */
sealed interface AuthResult {
    data class Success(val user: AuthUser) : AuthResult
    data class Error(val message: String, val cause: Throwable? = null) : AuthResult
}
