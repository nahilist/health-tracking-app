package com.pulsehealth.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.data.AuthResult
import com.pulsehealth.app.data.AuthState
import com.pulsehealth.app.data.SupabaseAuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AuthRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var repository: SupabaseAuthRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = SupabaseAuthRepository(
            context = context,
            supabaseUrl = SupabaseAuthRepository.DEFAULT_SUPABASE_URL,
            supabaseKey = SupabaseAuthRepository.DEFAULT_SUPABASE_KEY,
            ioDispatcher = testDispatcher,
            repositoryScope = testScope
        )
    }

    @Test
    fun initialSessionDefaultsToUnauthenticated() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertEquals(AuthState.Unauthenticated, repository.authState.value)
        assertNull(repository.currentUser.value)
    }

    @Test
    fun signInAsGuestEmitsGuestStateAndAnonymousUser() = runTest(testDispatcher) {
        advanceUntilIdle()
        val result = repository.signInAsGuest()
        assertTrue(result is AuthResult.Success)

        val user = (result as AuthResult.Success).user
        assertTrue(user.isAnonymous)
        assertNull(user.email)
        assertTrue(user.id.startsWith("guest_"))

        assertEquals(AuthState.Guest, repository.authState.value)
        assertEquals(user, repository.currentUser.value)
    }

    @Test
    fun invalidEmailFormatReturnsError() = runTest(testDispatcher) {
        advanceUntilIdle()
        val result = repository.signInWithEmail("not-an-email", "secret123")
        assertTrue(result is AuthResult.Error)
        assertEquals("Please enter a valid email address.", (result as AuthResult.Error).message)
    }

    @Test
    fun shortPasswordReturnsError() = runTest(testDispatcher) {
        advanceUntilIdle()
        val result = repository.signInWithEmail("valid@example.com", "123")
        assertTrue(result is AuthResult.Error)
        assertEquals("Password must be at least 6 characters.", (result as AuthResult.Error).message)
    }

    @Test
    fun successfulEmailSignInUpdatesStateAndCreatesUser() = runTest(testDispatcher) {
        advanceUntilIdle()
        val result = repository.signInWithEmail("test@pulsehealth.app", "password123")
        assertTrue(result is AuthResult.Success)

        val user = (result as AuthResult.Success).user
        assertFalse(user.isAnonymous)
        assertEquals("test@pulsehealth.app", user.email)
        assertEquals("test", user.displayName)
        assertNotNull(user.id)

        assertEquals(AuthState.Authenticated(user), repository.authState.value)
        assertEquals(user, repository.currentUser.value)
    }

    @Test
    fun successfulSignUpUpdatesState() = runTest(testDispatcher) {
        advanceUntilIdle()
        val result = repository.signUpWithEmail("newuser@pulsehealth.app", "securePass1")
        assertTrue(result is AuthResult.Success)

        val user = (result as AuthResult.Success).user
        assertEquals("newuser@pulsehealth.app", user.email)
        assertEquals(AuthState.Authenticated(user), repository.authState.value)
    }

    @Test
    fun signOutClearsSessionAndRevertsToUnauthenticated() = runTest(testDispatcher) {
        advanceUntilIdle()
        repository.signInWithEmail("alice@example.com", "mypassword")
        assertEquals(AuthState.Authenticated::class, repository.authState.value::class)

        val signOutResult = repository.signOut()
        assertTrue(signOutResult.isSuccess)
        assertEquals(AuthState.Unauthenticated, repository.authState.value)
        assertNull(repository.currentUser.value)
    }

    @Test
    fun sanitizeAuthErrorStripsApiKeysUrlsAndHeaders() {
        val technicalLeakedException = RuntimeException(
            """
            invalid_credentials (Invalid login credentials: invalid_credentials)
            URL: https://nomvvyfcmcoitjtswlgy.supabase.co/auth/v1/token?grant_type=password
            Headers: [Authorization=[Bearer sb_publishable_5WybkEPBGc5nhjOIPVu11g_PHVNAiTz], apikey=[sb_publishable_5WybkEPBGc5nhjOIPVu11g_PHVNAiTz]]
            Http Method: POST
            """.trimIndent()
        )

        val cleanMessage = SupabaseAuthRepository.sanitizeAuthError(technicalLeakedException)
        assertEquals("Invalid email or password. Please verify your credentials and try again.", cleanMessage)
        assertFalse(cleanMessage.contains("apikey"))
        assertFalse(cleanMessage.contains("Bearer"))
        assertFalse(cleanMessage.contains("http"))
        assertFalse(cleanMessage.contains("sb_publishable"))
    }

    @Test
    fun sanitizeAuthErrorHandlesExistingUserAndNetworkFailures() {
        val userExistsException = RuntimeException("User already registered with this email")
        assertEquals(
            "An account with this email address already exists. Please sign in instead.",
            SupabaseAuthRepository.sanitizeAuthError(userExistsException)
        )

        val networkException = RuntimeException("java.net.ConnectException: Failed to connect to host")
        assertEquals(
            "Network error. Please check your internet connection.",
            SupabaseAuthRepository.sanitizeAuthError(networkException)
        )
    }
}
