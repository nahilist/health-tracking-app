package com.pulsehealth.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.data.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UserPreferencesRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = UserPreferencesRepository(
            context = context,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun togglingCardAddsAndRemovesCardFromHiddenSet() = runTest(testDispatcher) {
        val initial = repository.hiddenCards.first()
        assertTrue(initial.isEmpty())

        repository.toggleCard("Steps")
        val withSteps = repository.hiddenCards.first()
        assertTrue(withSteps.contains("Steps"))

        repository.toggleCard("Steps")
        val withoutSteps = repository.hiddenCards.first()
        assertFalse(withoutSteps.contains("Steps"))
    }

    @Test
    fun settingWaterGoalPersistsNewValue() = runTest(testDispatcher) {
        repository.setWaterGoalLiters(3.5)
        assertEquals(3.5, repository.waterGoalLiters.first(), 0.0)
    }

    @Test
    fun togglingSyncWaterToHealthConnectUpdatesState() = runTest(testDispatcher) {
        assertFalse(repository.syncWaterToHealthConnect.first())
        repository.setSyncWaterToHealthConnect(true)
        assertTrue(repository.syncWaterToHealthConnect.first())
    }

    @Test
    fun onboardingStateCanBeToggledAndCleared() = runTest(testDispatcher) {
        assertFalse(repository.onboardingCompleted.first())
        repository.setOnboardingCompleted(true)
        assertTrue(repository.onboardingCompleted.first())

        repository.clearAllPreferences()
        assertFalse(repository.onboardingCompleted.first())
    }
}
