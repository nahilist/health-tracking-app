package com.pulsehealth.app.notifications

/**
 * Configuration for local opt-in hydration reminders.
 *
 * @param enabled Whether recurring reminders are scheduled.
 * @param intervalHours Recurrence frequency in hours (e.g. every 1, 2, or 3 hours).
 * @param quietHoursStartHour Start of quiet hours (24-hour format, e.g. 22 for 10 PM).
 * @param quietHoursEndHour End of quiet hours (24-hour format, e.g. 8 for 8 AM).
 */
data class ReminderSettings(
    val enabled: Boolean = false,
    val intervalHours: Int = 2,
    val quietHoursStartHour: Int = 22,
    val quietHoursEndHour: Int = 8
) {
    /**
     * Checks whether the given hour falls inside the configured quiet hours window.
     */
    fun isQuietTime(currentHour: Int): Boolean {
        require(currentHour in 0..23) { "Hour must be between 0 and 23" }
        return if (quietHoursStartHour > quietHoursEndHour) {
            // Over midnight: e.g. 22:00 to 08:00
            currentHour >= quietHoursStartHour || currentHour < quietHoursEndHour
        } else if (quietHoursStartHour < quietHoursEndHour) {
            // Same day window: e.g. 13:00 to 15:00
            currentHour in quietHoursStartHour until quietHoursEndHour
        } else {
            // Start == End means no quiet window
            false
        }
    }
}
