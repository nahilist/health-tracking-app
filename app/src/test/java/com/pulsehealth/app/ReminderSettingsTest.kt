package com.pulsehealth.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.notifications.NoOpReminderScheduler
import com.pulsehealth.app.notifications.ReminderSettings
import com.pulsehealth.app.notifications.WorkManagerReminderScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ReminderSettingsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun quietHoursOverMidnightDetectsNightAndDayCorrectly() {
        // Quiet from 22:00 (10 PM) to 08:00 (8 AM)
        val settings = ReminderSettings(
            enabled = true,
            intervalHours = 2,
            quietHoursStartHour = 22,
            quietHoursEndHour = 8
        )

        // Inside quiet hours
        assertTrue(settings.isQuietTime(22))
        assertTrue(settings.isQuietTime(23))
        assertTrue(settings.isQuietTime(0))
        assertTrue(settings.isQuietTime(3))
        assertTrue(settings.isQuietTime(7))

        // Outside quiet hours
        assertFalse(settings.isQuietTime(8))
        assertFalse(settings.isQuietTime(9))
        assertFalse(settings.isQuietTime(12))
        assertFalse(settings.isQuietTime(18))
        assertFalse(settings.isQuietTime(21))
    }

    @Test
    fun sameDayQuietHoursWindowDetectsCorrectly() {
        // Quiet from 13:00 to 16:00
        val settings = ReminderSettings(
            enabled = true,
            quietHoursStartHour = 13,
            quietHoursEndHour = 16
        )

        assertFalse(settings.isQuietTime(12))
        assertTrue(settings.isQuietTime(13))
        assertTrue(settings.isQuietTime(14))
        assertTrue(settings.isQuietTime(15))
        assertFalse(settings.isQuietTime(16))
        assertFalse(settings.isQuietTime(20))
    }

    @Test
    fun noQuietWindowWhenStartEqualsEnd() {
        val settings = ReminderSettings(
            enabled = true,
            quietHoursStartHour = 0,
            quietHoursEndHour = 0
        )

        assertFalse(settings.isQuietTime(0))
        assertFalse(settings.isQuietTime(12))
        assertFalse(settings.isQuietTime(23))
    }

    @Test
    fun noOpReminderSchedulerTracksState() {
        val scheduler = NoOpReminderScheduler()
        assertFalse(scheduler.isCancelled)
        assertEquals(null, scheduler.scheduledInterval)

        scheduler.scheduleReminders(3)
        assertEquals(3, scheduler.scheduledInterval)
        assertFalse(scheduler.isCancelled)

        scheduler.cancelReminders()
        assertTrue(scheduler.isCancelled)
        assertEquals(null, scheduler.scheduledInterval)
    }

    @Test
    fun workManagerReminderSchedulerExecutesSafely() {
        val scheduler = WorkManagerReminderScheduler(context)
        // Ensure calls complete gracefully without crash
        scheduler.scheduleReminders(2)
        scheduler.cancelReminders()
    }
}
