package com.pulsehealth.app.data

import java.util.Locale

/**
 * Canonical identifiers for customizable favorite metric cards on the Summary screen.
 * Decouples persistent configuration from user-facing UI labels to support localization
 * and safe schema migrations.
 */
enum class FavoriteCardId(
    val canonicalId: String,
    val legacyAlias: String,
    val metricRegistryId: String,
    val defaultTitle: String
) {
    STEPS("steps", "Steps", "Steps", "Steps"),
    HEART_RATE("heart_rate", "Heart rate", "Heart rate", "Heart rate"),
    SLEEP("sleep", "Sleep", "Sleep", "Sleep"),
    WATER("water", "Water", "Water", "Water");

    companion object {
        val ALL: List<FavoriteCardId> = entries

        /**
         * Resolves a raw string (canonical, legacy English alias, or case-variant)
         * to its canonical identifier.
         */
        fun normalize(raw: String): String {
            val trimmed = raw.trim()
            val match = entries.firstOrNull {
                it.canonicalId.equals(trimmed, ignoreCase = true) ||
                it.legacyAlias.equals(trimmed, ignoreCase = true) ||
                it.metricRegistryId.equals(trimmed, ignoreCase = true)
            }
            return match?.canonicalId ?: trimmed.lowercase(Locale.ROOT)
        }

        /**
         * Finds the [FavoriteCardId] matching the raw string, or null if unknown.
         */
        fun fromString(raw: String): FavoriteCardId? {
            val normalized = normalize(raw)
            return entries.firstOrNull { it.canonicalId == normalized }
        }
    }
}
