package com.pulsehealth.app.data

import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for authentication and session management in Pulse Health.
 * Decouples presentation logic from concrete cloud auth providers.
 */
interface AuthRepository {
    val authState: StateFlow<AuthState>
    val currentUser: StateFlow<AuthUser?>

    /**
     * Signs in an existing user using email and password.
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResult

    /**
     * Registers a new user with email and password.
     */
    suspend fun signUpWithEmail(email: String, password: String): AuthResult

    /**
     * Enters local guest mode without an external account.
     * All health data remains strictly local on device.
     */
    suspend fun signInAsGuest(): AuthResult

    /**
     * Ends the active session and clears cached credentials.
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Refreshes the active session token if possible.
     */
    suspend fun refreshSession(): AuthResult
}

/**
 * In-memory test and preview implementation of [AuthRepository].
 */
class NoOpAuthRepository(
    initialState: AuthState = AuthState.Guest
) : AuthRepository {
    private val _authState = kotlinx.coroutines.flow.MutableStateFlow(initialState)
    override val authState: StateFlow<AuthState> = _authState

    private val _currentUser = kotlinx.coroutines.flow.MutableStateFlow<AuthUser?>(
        when (initialState) {
            is AuthState.Authenticated -> initialState.user
            is AuthState.Guest -> AuthUser(
                id = "guest_local",
                email = null,
                displayName = "Guest User",
                isAnonymous = true
            )
            else -> null
        }
    )
    override val currentUser: StateFlow<AuthUser?> = _currentUser

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        val user = AuthUser(
            id = "mock_user_${email.hashCode()}",
            email = email,
            displayName = email.substringBefore("@"),
            isAnonymous = false
        )
        _currentUser.value = user
        _authState.value = AuthState.Authenticated(user)
        return AuthResult.Success(user)
    }

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        return signInWithEmail(email, password)
    }

    override suspend fun signInAsGuest(): AuthResult {
        val user = AuthUser(
            id = "guest_local",
            email = null,
            displayName = "Guest User",
            isAnonymous = true
        )
        _currentUser.value = user
        _authState.value = AuthState.Guest
        return AuthResult.Success(user)
    }

    override suspend fun signOut(): Result<Unit> {
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
        return Result.success(Unit)
    }

    override suspend fun refreshSession(): AuthResult {
        val user = _currentUser.value ?: return AuthResult.Error("No active user session to refresh.")
        return AuthResult.Success(user)
    }
}
