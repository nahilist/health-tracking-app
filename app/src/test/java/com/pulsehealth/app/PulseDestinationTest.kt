package com.pulsehealth.app

import com.pulsehealth.app.ui.navigation.PulseDestination
import com.pulsehealth.app.ui.navigation.TopLevelDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PulseDestinationTest {

    @Test
    fun routesMatchExpectedPathNames() {
        assertEquals("summary", PulseDestination.Summary.route)
        assertEquals("sharing", PulseDestination.Sharing.route)
        assertEquals("browse", PulseDestination.Browse.route)
        assertEquals("profile", PulseDestination.Profile.route)
        assertEquals("onboarding", PulseDestination.Onboarding.route)
        assertEquals("detail/{metricType}", PulseDestination.Detail.route)
        assertEquals("detail/Steps", PulseDestination.Detail.createRoute("Steps"))
        assertEquals("detail/Hydration", PulseDestination.Detail.createRoute("Hydration"))
    }

    @Test
    fun topLevelDestinationsHaveValidIconsAndRoutes() {
        assertEquals(3, TopLevelDestination.entries.size)
        TopLevelDestination.entries.forEach { destination ->
            assertNotNull(destination.route)
            assertNotNull(destination.label)
            assertNotNull(destination.icon)
        }
    }
}
