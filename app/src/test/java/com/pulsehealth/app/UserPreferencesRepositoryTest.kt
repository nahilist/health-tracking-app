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

        repository.toggleCard("steps")
        val withSteps = repository.hiddenCards.first()
        assertTrue(withSteps.contains("steps"))

        repository.toggleCard("steps")
        val withoutSteps = repository.hiddenCards.first()
        assertFalse(withoutSteps.contains("steps"))
    }

    @Test
    fun togglingLegacyAliasNormalizesToCanonicalId() = runTest(testDispatcher) {
        repository.toggleCard("Heart rate")
        val withHeart = repository.hiddenCards.first()
        assertTrue(withHeart.contains("heart_rate"))

        // Toggling with canonical ID removes it
        repository.toggleCard("heart_rate")
        val withoutHeart = repository.hiddenCards.first()
        assertFalse(withoutHeart.contains("heart_rate"))
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

    @Test
    fun reminderPreferencesCanBeUpdatedAndPersisted() = runTest(testDispatcher) {
        assertFalse(repository.remindersEnabled.first())
        assertEquals(2, repository.reminderIntervalHours.first())
        assertEquals(22, repository.quietHoursStartHour.first())
        assertEquals(8, repository.quietHoursEndHour.first())

        repository.setRemindersEnabled(true)
        repository.setReminderIntervalHours(3)
        repository.setQuietHours(23, 7)

        assertTrue(repository.remindersEnabled.first())
        assertEquals(3, repository.reminderIntervalHours.first())
        assertEquals(23, repository.quietHoursStartHour.first())
        assertEquals(7, repository.quietHoursEndHour.first())
    }
}
