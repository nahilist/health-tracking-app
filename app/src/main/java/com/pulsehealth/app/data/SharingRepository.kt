package com.pulsehealth.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Context.sharingDataStore by preferencesDataStore(name = "pulse_sharing_preferences")

interface SharingRepository {
    val contacts: Flow<List<TrustedContact>>
    suspend fun addContact(contact: TrustedContact)
    suspend fun updateContactMetrics(contactId: String, metrics: Set<String>)
    suspend fun revokeContact(contactId: String)
    suspend fun deleteContact(contactId: String)
    suspend fun createExportPackage(
        recipientName: String?,
        selectedMetrics: Set<String>,
        range: TimeRange,
        dashboardMetrics: DashboardMetrics,
        locale: Locale = Locale.getDefault()
    ): ExportedHealthPackage
    fun formatShareText(
        pkg: ExportedHealthPackage,
        locale: Locale = Locale.getDefault()
    ): String
}

class DataStoreSharingRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) : SharingRepository {

    private object Keys {
        val contactsJson = stringPreferencesKey("trusted_contacts_json")
    }

    override val contacts: Flow<List<TrustedContact>> =
        context.sharingDataStore.data.map { preferences ->
            val raw = preferences[Keys.contactsJson]
            if (raw.isNullOrBlank()) {
                emptyList()
            } else {
                runCatching {
                    json.decodeFromString<List<TrustedContact>>(raw)
                }.getOrDefault(emptyList())
            }
        }

    override suspend fun addContact(contact: TrustedContact): Unit = withContext(ioDispatcher) {
        context.sharingDataStore.edit { preferences ->
            val current = decodeContacts(preferences[Keys.contactsJson])
            val updated = current + contact
            preferences[Keys.contactsJson] = json.encodeToString(updated)
        }
    }

    override suspend fun updateContactMetrics(contactId: String, metrics: Set<String>): Unit = withContext(ioDispatcher) {
        context.sharingDataStore.edit { preferences ->
            val current = decodeContacts(preferences[Keys.contactsJson])
            val updated = current.map {
                if (it.id == contactId) it.copy(sharedMetrics = metrics) else it
            }
            preferences[Keys.contactsJson] = json.encodeToString(updated)
        }
    }

    override suspend fun revokeContact(contactId: String): Unit = withContext(ioDispatcher) {
        context.sharingDataStore.edit { preferences ->
            val current = decodeContacts(preferences[Keys.contactsJson])
            val updated = current.map {
                if (it.id == contactId) it.copy(status = ShareStatus.REVOKED) else it
            }
            preferences[Keys.contactsJson] = json.encodeToString(updated)
        }
    }

    override suspend fun deleteContact(contactId: String): Unit = withContext(ioDispatcher) {
        context.sharingDataStore.edit { preferences ->
            val current = decodeContacts(preferences[Keys.contactsJson])
            val updated = current.filterNot { it.id == contactId }
            preferences[Keys.contactsJson] = json.encodeToString(updated)
        }
    }

    override suspend fun createExportPackage(
        recipientName: String?,
        selectedMetrics: Set<String>,
        range: TimeRange,
        dashboardMetrics: DashboardMetrics,
        locale: Locale
    ): ExportedHealthPackage = withContext(ioDispatcher) {
        val metricsMap = mutableMapOf<String, String>()
        selectedMetrics.forEach { metricKey ->
            val formatted = MetricRegistry.formatValue(metricKey, dashboardMetrics, locale)
            metricsMap[metricKey] = formatted
        }
        ExportedHealthPackage(
            periodRange = range.label,
            authorizedRecipient = recipientName?.takeIf { it.isNotBlank() },
            sharedMetrics = metricsMap
        )
    }

    override fun formatShareText(pkg: ExportedHealthPackage, locale: Locale): String {
        val dateStr = Instant.ofEpochMilli(pkg.generatedAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", locale))

        val sb = StringBuilder()
        sb.appendLine("PULSE HEALTH · WELLNESS SUMMARY")
        sb.appendLine("Generated: $dateStr")
        sb.appendLine("Period: ${pkg.periodRange}")
        if (!pkg.authorizedRecipient.isNullOrBlank()) {
            sb.appendLine("Prepared for: ${pkg.authorizedRecipient}")
        }
        sb.appendLine("----------------------------------------")
        if (pkg.sharedMetrics.isEmpty()) {
            sb.appendLine("No specific metrics selected.")
        } else {
            pkg.sharedMetrics.forEach { (metric, value) ->
                sb.appendLine("$metric: $value")
            }
        }
        sb.appendLine("----------------------------------------")
        sb.appendLine("Note: ${pkg.notes}")
        return sb.toString().trim()
    }

    private fun decodeContacts(raw: String?): List<TrustedContact> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<TrustedContact>>(raw)
        }.getOrDefault(emptyList())
    }
}

class NoOpSharingRepository : SharingRepository {
    private val contactsFlow = MutableStateFlow<List<TrustedContact>>(emptyList())
    override val contacts: Flow<List<TrustedContact>> = contactsFlow.asStateFlow()

    override suspend fun addContact(contact: TrustedContact) {
        contactsFlow.value = contactsFlow.value + contact
    }

    override suspend fun updateContactMetrics(contactId: String, metrics: Set<String>) {
        contactsFlow.value = contactsFlow.value.map {
            if (it.id == contactId) it.copy(sharedMetrics = metrics) else it
        }
    }

    override suspend fun revokeContact(contactId: String) {
        contactsFlow.value = contactsFlow.value.map {
            if (it.id == contactId) it.copy(status = ShareStatus.REVOKED) else it
        }
    }

    override suspend fun deleteContact(contactId: String) {
        contactsFlow.value = contactsFlow.value.filterNot { it.id == contactId }
    }

    override suspend fun createExportPackage(
        recipientName: String?,
        selectedMetrics: Set<String>,
        range: TimeRange,
        dashboardMetrics: DashboardMetrics,
        locale: Locale
    ): ExportedHealthPackage {
        val map = selectedMetrics.associateWith { MetricRegistry.formatValue(it, dashboardMetrics, locale) }
        return ExportedHealthPackage(
            periodRange = range.label,
            authorizedRecipient = recipientName?.takeIf { it.isNotBlank() },
            sharedMetrics = map
        )
    }

    override fun formatShareText(pkg: ExportedHealthPackage, locale: Locale): String {
        val dateStr = Instant.ofEpochMilli(pkg.generatedAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", locale))

        val sb = StringBuilder()
        sb.appendLine("PULSE HEALTH · WELLNESS SUMMARY")
        sb.appendLine("Generated: $dateStr")
        sb.appendLine("Period: ${pkg.periodRange}")
        if (!pkg.authorizedRecipient.isNullOrBlank()) {
            sb.appendLine("Prepared for: ${pkg.authorizedRecipient}")
        }
        sb.appendLine("----------------------------------------")
        pkg.sharedMetrics.forEach { (metric, value) ->
            sb.appendLine("$metric: $value")
        }
        sb.appendLine("----------------------------------------")
        sb.appendLine("Note: ${pkg.notes}")
        return sb.toString().trim()
    }
}
