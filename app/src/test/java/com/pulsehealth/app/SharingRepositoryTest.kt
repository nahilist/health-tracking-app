package com.pulsehealth.app

import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.NoOpSharingRepository
import com.pulsehealth.app.data.ShareDuration
import com.pulsehealth.app.data.ShareRelationship
import com.pulsehealth.app.data.ShareStatus
import com.pulsehealth.app.data.TimeRange
import com.pulsehealth.app.data.TrustedContact
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class SharingRepositoryTest {

    @Test
    fun `addContact successfully saves and emits trusted contact`() = runTest {
        val repository = NoOpSharingRepository()
        val contact = TrustedContact.create(
            name = "Dr. Alice Smith",
            relationship = ShareRelationship.DOCTOR,
            contactIdentifier = "alice@clinic.org",
            sharedMetrics = setOf("Steps", "Heart rate"),
            duration = ShareDuration.ONE_MONTH
        )

        repository.addContact(contact)
        val list = repository.contacts.first()

        assertEquals(1, list.size)
        assertEquals("Dr. Alice Smith", list.first().name)
        assertEquals(ShareRelationship.DOCTOR, list.first().relationship)
        assertEquals("alice@clinic.org", list.first().contactIdentifier)
        assertEquals(setOf("Steps", "Heart rate"), list.first().sharedMetrics)
        assertEquals(ShareStatus.ACTIVE, list.first().status)
        assertTrue(list.first().isEffectivelyActive)
    }

    @Test
    fun `updateContactMetrics updates granular authorized metrics set`() = runTest {
        val repository = NoOpSharingRepository()
        val contact = TrustedContact.create(
            name = "Coach Bob",
            relationship = ShareRelationship.COACH,
            contactIdentifier = "bob@trainer.com",
            sharedMetrics = setOf("Steps"),
            duration = ShareDuration.ONE_WEEK
        )
        repository.addContact(contact)

        repository.updateContactMetrics(contact.id, setOf("Steps", "Water", "Activity"))
        val updated = repository.contacts.first().first { it.id == contact.id }

        assertEquals(setOf("Steps", "Water", "Activity"), updated.sharedMetrics)
    }

    @Test
    fun `revokeContact marks status as revoked and ceases active state`() = runTest {
        val repository = NoOpSharingRepository()
        val contact = TrustedContact.create(
            name = "Family Member",
            relationship = ShareRelationship.FAMILY,
            contactIdentifier = "",
            sharedMetrics = setOf("Water"),
            duration = ShareDuration.INDEFINITE
        )
        repository.addContact(contact)

        repository.revokeContact(contact.id)
        val revoked = repository.contacts.first().first { it.id == contact.id }

        assertEquals(ShareStatus.REVOKED, revoked.status)
        assertFalse(revoked.isEffectivelyActive)
    }

    @Test
    fun `deleteContact removes contact from repository`() = runTest {
        val repository = NoOpSharingRepository()
        val contact = TrustedContact.create(
            name = "Friend",
            relationship = ShareRelationship.FRIEND,
            contactIdentifier = "",
            sharedMetrics = setOf("Steps"),
            duration = ShareDuration.ONE_WEEK
        )
        repository.addContact(contact)
        assertEquals(1, repository.contacts.first().size)

        repository.deleteContact(contact.id)
        assertTrue(repository.contacts.first().isEmpty())
    }

    @Test
    fun `contact expiration calculated accurately based on duration`() {
        val now = 1_000_000_000L
        val pastWeek = now - (8L * 24 * 60 * 60 * 1000)

        // Contact created in past whose 7-day period has elapsed
        val expiredContact = TrustedContact.create(
            name = "Past Doctor",
            relationship = ShareRelationship.DOCTOR,
            contactIdentifier = "",
            sharedMetrics = setOf("Steps"),
            duration = ShareDuration.ONE_WEEK,
            clockMillis = pastWeek
        )
        assertTrue(expiredContact.isExpired)
        assertFalse(expiredContact.isEffectivelyActive)

        // Indefinite contact has no expiration
        val indefiniteContact = TrustedContact.create(
            name = "Partner",
            relationship = ShareRelationship.PARTNER,
            contactIdentifier = "",
            sharedMetrics = setOf("Steps", "Heart rate"),
            duration = ShareDuration.INDEFINITE,
            clockMillis = now
        )
        assertNull(indefiniteContact.expiresAtEpochMillis)
        assertFalse(indefiniteContact.isExpired)
        assertTrue(indefiniteContact.isEffectivelyActive)
    }

    @Test
    fun `createExportPackage only includes authorized metrics for privacy`() = runTest {
        val repository = NoOpSharingRepository()
        val metrics = DashboardMetrics.demo()

        val selected = setOf("Steps", "Water")
        val pkg = repository.createExportPackage(
            recipientName = "Dr. Alice",
            selectedMetrics = selected,
            range = TimeRange.WEEK,
            dashboardMetrics = metrics,
            locale = Locale.US
        )

        assertEquals("Week", pkg.periodRange)
        assertEquals("Dr. Alice", pkg.authorizedRecipient)
        assertEquals(2, pkg.sharedMetrics.size)
        assertTrue(pkg.sharedMetrics.containsKey("Steps"))
        assertTrue(pkg.sharedMetrics.containsKey("Water"))
        assertFalse(pkg.sharedMetrics.containsKey("Heart rate"))
        assertFalse(pkg.sharedMetrics.containsKey("Sleep"))
        assertFalse(pkg.sharedMetrics.containsKey("Active calories"))

        val shareText = repository.formatShareText(pkg, Locale.US)
        assertTrue(shareText.contains("PULSE HEALTH · WELLNESS SUMMARY"))
        assertTrue(shareText.contains("Prepared for: Dr. Alice"))
        assertTrue(shareText.contains("Steps: 8,432 steps"))
        assertTrue(shareText.contains("Water: 1.50 L"))
        assertTrue(shareText.contains("Not a medical diagnosis"))
        assertFalse(shareText.contains("bpm"))
    }
}
