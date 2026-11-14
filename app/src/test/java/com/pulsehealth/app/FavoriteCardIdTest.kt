package com.pulsehealth.app

import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.FavoriteCardId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteCardIdTest {

    @Test
    fun allFavoritesContainExpectedCoreMetrics() {
        val all = FavoriteCardId.ALL
        assertEquals(4, all.size)
        assertTrue(all.contains(FavoriteCardId.STEPS))
        assertTrue(all.contains(FavoriteCardId.HEART_RATE))
        assertTrue(all.contains(FavoriteCardId.SLEEP))
        assertTrue(all.contains(FavoriteCardId.WATER))
    }

    @Test
    fun normalizeResolvesCanonicalAndLegacyAliases() {
        assertEquals("steps", FavoriteCardId.normalize("steps"))
        assertEquals("steps", FavoriteCardId.normalize("Steps"))
        assertEquals("steps", FavoriteCardId.normalize("STEPS"))

        assertEquals("heart_rate", FavoriteCardId.normalize("heart_rate"))
        assertEquals("heart_rate", FavoriteCardId.normalize("Heart rate"))
        assertEquals("heart_rate", FavoriteCardId.normalize("HEART_RATE"))

        assertEquals("sleep", FavoriteCardId.normalize("sleep"))
        assertEquals("sleep", FavoriteCardId.normalize("Sleep"))

        assertEquals("water", FavoriteCardId.normalize("water"))
        assertEquals("water", FavoriteCardId.normalize("Water"))
    }

    @Test
    fun fromStringReturnsMatchingEnumOrNull() {
        assertEquals(FavoriteCardId.STEPS, FavoriteCardId.fromString("steps"))
        assertEquals(FavoriteCardId.STEPS, FavoriteCardId.fromString("Steps"))
        assertEquals(FavoriteCardId.HEART_RATE, FavoriteCardId.fromString("Heart rate"))
        assertEquals(FavoriteCardId.SLEEP, FavoriteCardId.fromString("sleep"))
        assertEquals(FavoriteCardId.WATER, FavoriteCardId.fromString("Water"))

        assertNull(FavoriteCardId.fromString("unknown_metric"))
    }

    @Test
    fun dashboardUiStateIsCardHiddenHandlesCanonicalAndLegacyStrings() {
        val stateWithCanonical = DashboardUiState(hiddenCards = setOf("sleep", "heart_rate"))
        assertTrue(stateWithCanonical.isCardHidden(FavoriteCardId.SLEEP))
        assertTrue(stateWithCanonical.isCardHidden("sleep"))
        assertTrue(stateWithCanonical.isCardHidden("Sleep"))
        assertTrue(stateWithCanonical.isCardHidden(FavoriteCardId.HEART_RATE))
        assertFalse(stateWithCanonical.isCardHidden(FavoriteCardId.STEPS))
        assertFalse(stateWithCanonical.isCardHidden(FavoriteCardId.WATER))

        val stateWithLegacy = DashboardUiState(hiddenCards = setOf("Sleep"))
        assertTrue(stateWithLegacy.isCardHidden(FavoriteCardId.SLEEP))
        assertTrue(stateWithLegacy.isCardHidden("sleep"))
        assertTrue(stateWithLegacy.isCardHidden("Sleep"))
        assertFalse(stateWithLegacy.isCardHidden(FavoriteCardId.STEPS))
    }
}
