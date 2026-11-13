package com.pulsehealth.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class SupabaseAuthRepository(
    private val context: Context,
    private val supabaseUrl: String = DEFAULT_SUPABASE_URL,
    private val supabaseKey: String = DEFAULT_SUPABASE_KEY,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val repositoryScope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)
) : AuthRepository {

    private val prefs: SharedPreferences by lazy {
        createSecurePreferences(context)
    }

    private val supabaseClient: SupabaseClient? by lazy {
        if (supabaseUrl.isNotBlank() && supabaseKey.isNotBlank() && supabaseUrl != DEFAULT_SUPABASE_URL) {
            try {
                createSupabaseClient(
                    supabaseUrl = supabaseUrl,
                    supabaseKey = supabaseKey
                ) {
                    install(Auth)
                    install(Postgrest)
                }
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        repositoryScope.launch {
            loadInitialSession()
        }
    }

    private suspend fun loadInitialSession() = withContext(ioDispatcher) {
        val mode = prefs.getString(KEY_AUTH_MODE, null)
        when (mode) {
            MODE_GUEST -> {
                val guestId = prefs.getString(KEY_USER_ID, null) ?: ("guest_" + UUID.randomUUID().toString().take(8))
                val guestUser = AuthUser(id = guestId, email = null, displayName = "Guest User", isAnonymous = true)
                _currentUser.value = guestUser
                _authState.value = AuthState.Guest
            }
            MODE_AUTHENTICATED -> {
                val userId = prefs.getString(KEY_USER_ID, null)
                val userEmail = prefs.getString(KEY_USER_EMAIL, null)
                if (userId != null && userEmail != null) {
                    val user = AuthUser(id = userId, email = userEmail, displayName = userEmail.substringBefore("@"), isAnonymous = false)
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated(user)
                } else {
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                }
            }
            else -> {
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthResult = withContext(ioDispatcher) {
        val trimmedEmail = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }
        if (password.length < 6) {
            return@withContext AuthResult.Error("Password must be at least 6 characters.")
        }

        val client = supabaseClient
        if (client != null) {
            try {
                client.auth.signInWith(Email) {
                    this.email = trimmedEmail
                    this.password = password
                }
                val remoteUser = client.auth.currentUserOrNull()
                val userId = remoteUser?.id ?: UUID.randomUUID().toString()
                val user = AuthUser(
                    id = userId,
                    email = trimmedEmail,
                    displayName = trimmedEmail.substringBefore("@"),
                    isAnonymous = false
                )
                persistSession(MODE_AUTHENTICATED, user)
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
                AuthResult.Success(user)
            } catch (e: Exception) {
                AuthResult.Error(e.message ?: "Authentication failed. Please check your credentials.", e)
            }
        } else {
            // Local sandbox fallback when running offline or without remote credentials
            val user = AuthUser(
                id = UUID.nameUUIDFromBytes(trimmedEmail.toByteArray()).toString(),
                email = trimmedEmail,
                displayName = trimmedEmail.substringBefore("@"),
                isAnonymous = false
            )
            persistSession(MODE_AUTHENTICATED, user)
            _currentUser.value = user
            _authState.value = AuthState.Authenticated(user)
            AuthResult.Success(user)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult = withContext(ioDispatcher) {
        val trimmedEmail = email.trim()
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }
        if (password.length < 6) {
            return@withContext AuthResult.Error("Password must be at least 6 characters.")
        }

        val client = supabaseClient
        if (client != null) {
            try {
                client.auth.signUpWith(Email) {
                    this.email = trimmedEmail
                    this.password = password
                }
                val remoteUser = client.auth.currentUserOrNull()
                val userId = remoteUser?.id ?: UUID.randomUUID().toString()
                val user = AuthUser(
                    id = userId,
                    email = trimmedEmail,
                    displayName = trimmedEmail.substringBefore("@"),
                    isAnonymous = false
                )
                persistSession(MODE_AUTHENTICATED, user)
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
                AuthResult.Success(user)
            } catch (e: Exception) {
                AuthResult.Error(e.message ?: "Registration failed. Please check your details.", e)
            }
        } else {
            // Local sandbox fallback when running offline or without remote credentials
            val user = AuthUser(
                id = UUID.nameUUIDFromBytes(trimmedEmail.toByteArray()).toString(),
                email = trimmedEmail,
                displayName = trimmedEmail.substringBefore("@"),
                isAnonymous = false
            )
            persistSession(MODE_AUTHENTICATED, user)
            _currentUser.value = user
            _authState.value = AuthState.Authenticated(user)
            AuthResult.Success(user)
        }
    }

    override suspend fun signInAsGuest(): AuthResult = withContext(ioDispatcher) {
        val existingId = prefs.getString(KEY_USER_ID, null)
        val guestId = if (!existingId.isNullOrBlank() && existingId.startsWith("guest_")) {
            existingId
        } else {
            "guest_" + UUID.randomUUID().toString().take(8)
        }
        val guestUser = AuthUser(
            id = guestId,
            email = null,
            displayName = "Guest User",
            isAnonymous = true
        )
        persistSession(MODE_GUEST, guestUser)
        _currentUser.value = guestUser
        _authState.value = AuthState.Guest
        AuthResult.Success(guestUser)
    }

    override suspend fun signOut(): Result<Unit> = withContext(ioDispatcher) {
        try {
            supabaseClient?.auth?.signOut()
        } catch (_: Exception) {
            // Offline sign-out should always succeed locally
        }
        clearSession()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
        Result.success(Unit)
    }

    override suspend fun refreshSession(): AuthResult = withContext(ioDispatcher) {
        val client = supabaseClient
        if (client != null && _authState.value is AuthState.Authenticated) {
            try {
                client.auth.refreshCurrentSession()
                val user = _currentUser.value ?: return@withContext AuthResult.Error("No active user session to refresh.")
                AuthResult.Success(user)
            } catch (e: Exception) {
                AuthResult.Error(e.message ?: "Failed to refresh session.", e)
            }
        } else {
            val user = _currentUser.value
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("No active session.")
            }
        }
    }

    private fun persistSession(mode: String, user: AuthUser) {
        prefs.edit()
            .putString(KEY_AUTH_MODE, mode)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_EMAIL, user.email)
            .apply()
    }

    private fun clearSession() {
        prefs.edit()
            .remove(KEY_AUTH_MODE)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .apply()
    }

    companion object {
        const val DEFAULT_SUPABASE_URL = "https://example.supabase.co"
        const val DEFAULT_SUPABASE_KEY = "placeholder-anon-key"

        private const val PREFS_FILE = "pulse_secure_auth_prefs"
        private const val KEY_AUTH_MODE = "auth_mode"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"

        const val MODE_GUEST = "GUEST"
        const val MODE_AUTHENTICATED = "AUTHENTICATED"

        private fun createSecurePreferences(context: Context): SharedPreferences {
            return try {
                val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
                EncryptedSharedPreferences.create(
                    PREFS_FILE,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (_: Exception) {
                // Fallback for Robolectric or environments where Android Keystore provider is unavailable
                context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
            }
        }
    }
}
