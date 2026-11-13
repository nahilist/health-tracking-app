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
}
