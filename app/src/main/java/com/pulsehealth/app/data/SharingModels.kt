package com.pulsehealth.app.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class ShareRelationship(val label: String) {
    FAMILY("Family member"),
    DOCTOR("Healthcare provider"),
    PARTNER("Partner"),
    COACH("Fitness coach"),
    FRIEND("Friend")
}

@Serializable
enum class ShareDuration(val label: String, val days: Int?) {
    ONE_WEEK("7 Days (Review)", 7),
    ONE_MONTH("30 Days", 30),
    INDEFINITE("Until revoked", null)
}

@Serializable
enum class ShareStatus {
    ACTIVE,
    PENDING,
    EXPIRED,
    REVOKED
}

@Serializable
data class TrustedContact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val relationship: ShareRelationship,
    val contactIdentifier: String = "",
    val sharedMetrics: Set<String> = emptySet(),
    val duration: ShareDuration = ShareDuration.ONE_MONTH,
    val status: ShareStatus = ShareStatus.ACTIVE,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val expiresAtEpochMillis: Long? = null
) {
    val isExpired: Boolean
        get() = expiresAtEpochMillis != null && System.currentTimeMillis() > expiresAtEpochMillis

    val isEffectivelyActive: Boolean
        get() = status == ShareStatus.ACTIVE && !isExpired

    companion object {
        fun create(
            name: String,
            relationship: ShareRelationship,
            contactIdentifier: String,
            sharedMetrics: Set<String>,
            duration: ShareDuration,
            clockMillis: Long = System.currentTimeMillis()
        ): TrustedContact {
            val expiresAt = duration.days?.let { days ->
                clockMillis + (days.toLong() * 24 * 60 * 60 * 1000)
            }
            return TrustedContact(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                relationship = relationship,
                contactIdentifier = contactIdentifier.trim(),
                sharedMetrics = sharedMetrics,
                duration = duration,
                status = ShareStatus.ACTIVE,
                createdAtEpochMillis = clockMillis,
                expiresAtEpochMillis = expiresAt
            )
        }
    }
}

@Serializable
data class ExportedHealthPackage(
    val exportVersion: String = "1.0.0",
    val generatedAtEpochMillis: Long = System.currentTimeMillis(),
    val app: String = "Pulse Health",
    val periodRange: String,
    val authorizedRecipient: String? = null,
    val sharedMetrics: Map<String, String>,
    val notes: String = "Self-reported and Health Connect aggregated wellness data. Not a medical diagnosis or treatment recommendation."
)
