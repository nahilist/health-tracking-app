package com.pulsehealth.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.pulsehealth.app.data.AuthState
import com.pulsehealth.app.data.AuthUser
import com.pulsehealth.app.data.NoOpAuthRepository
import com.pulsehealth.app.data.local.HydrationDao
import com.pulsehealth.app.data.local.HydrationEntryEntity
import com.pulsehealth.app.data.sync.SupabaseSyncRepository
import com.pulsehealth.app.data.sync.SyncResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SupabaseSyncRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context

    private class FakeHydrationDao : HydrationDao {
        val entries = mutableListOf<HydrationEntryEntity>()
        var shouldThrowOnQuery = false

        override suspend fun upsert(entry: HydrationEntryEntity) {
            entries.removeAll { it.id == entry.id }
            entries.add(entry)
        }

        override suspend fun upsertAll(entries: List<HydrationEntryEntity>) {
            entries.forEach { upsert(it) }
        }

        override suspend fun getEntriesBySyncState(syncState: String): List<HydrationEntryEntity> {
            if (shouldThrowOnQuery) throw IllegalStateException("Database query failed")
            return entries.filter { it.syncState == syncState }
        }

        override suspend fun updateSyncState(id: String, syncState: String): Int {
            val index = entries.indexOfFirst { it.id == id }
            return if (index != -1) {
                entries[index] = entries[index].copy(syncState = syncState)
                1
            } else 0
        }

        override suspend fun delete(entry: HydrationEntryEntity) {
            entries.removeAll { it.id == entry.id }
        }

        override suspend fun deleteById(id: String): Int {
            val removed = entries.removeAll { it.id == id }
            return if (removed) 1 else 0
        }

        override suspend fun updateAmount(id: String, amountMilliliters: Int): Int {
            val index = entries.indexOfFirst { it.id == id }
            return if (index != -1) {
                entries[index] = entries[index].copy(amountMilliliters = amountMilliliters)
                1
            } else 0
        }

        override fun observeTotalMilliliters(startInclusive: Long, endExclusive: Long): Flow<Long> = flowOf(0L)
        override fun observeEntries(startInclusive: Long, endExclusive: Long): Flow<List<HydrationEntryEntity>> =
            flowOf(entries)
        override fun observeRecentEntries(limit: Int, offset: Int): Flow<List<HydrationEntryEntity>> = flowOf(entries)
        override suspend fun getEntryById(id: String): HydrationEntryEntity? = entries.find { it.id == id }
        override suspend fun getAllEntries(): List<HydrationEntryEntity> = entries.toList()
        override suspend fun deleteAll(): Int {
            val count = entries.size
            entries.clear()
            return count
        }
        override suspend fun deleteOlderThan(cutoffEpochMillis: Long): Int = 0
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun syncSkipsWhenUnauthenticated() = runTest(testDispatcher) {
        val authRepo = NoOpAuthRepository(AuthState.Unauthenticated)
        val dao = FakeHydrationDao()
        val syncRepo = SupabaseSyncRepository(
            context = context,
            hydrationDao = dao,
            authRepository = authRepo,
            supabaseClientProvider = { null },
            ioDispatcher = testDispatcher
        )

        val result = syncRepo.sync()
        assertEquals(SyncResult.SkippedUnauthenticated, result)
        assertNull(syncRepo.lastSyncedEpochMillis.value)
    }

    @Test
    fun syncSkipsWhenUserIsAnonymous() = runTest(testDispatcher) {
        val authRepo = NoOpAuthRepository(AuthState.Guest)
        val dao = FakeHydrationDao()
        val syncRepo = SupabaseSyncRepository(
            context = context,
            hydrationDao = dao,
            authRepository = authRepo,
            supabaseClientProvider = { null },
            ioDispatcher = testDispatcher
        )

        val result = syncRepo.sync()
        assertEquals(SyncResult.SkippedUnauthenticated, result)
    }

    @Test
    fun syncUploadsPendingEntriesAndMarksThemSynced() = runTest(testDispatcher) {
        val user = AuthUser("user-1", "user@test.com", "Test User", isAnonymous = false)
        val authRepo = NoOpAuthRepository(AuthState.Authenticated(user))
        val dao = FakeHydrationDao().apply {
            entries.add(
                HydrationEntryEntity(
                    id = "entry-1",
                    amountMilliliters = 250,
                    recordedAtEpochMillis = 1000L,
                    source = "manual",
                    syncState = "pending"
                )
            )
            entries.add(
                HydrationEntryEntity(
                    id = "entry-2",
                    amountMilliliters = 500,
                    recordedAtEpochMillis = 2000L,
                    source = "manual",
                    syncState = "pending"
                )
            )
        }

        val syncRepo = SupabaseSyncRepository(
            context = context,
            hydrationDao = dao,
            authRepository = authRepo,
            supabaseClientProvider = { null },
            ioDispatcher = testDispatcher
        )

        val result = syncRepo.sync()
        assertTrue(result is SyncResult.Success)
        val success = result as SyncResult.Success
        assertEquals(2, success.uploadedCount)

        val updatedEntries = dao.getEntriesBySyncState("synced")
        assertEquals(2, updatedEntries.size)
        assertNotNull(syncRepo.lastSyncedEpochMillis.value)
        assertNull(syncRepo.lastSyncError.value)
    }

    @Test
    fun syncCatchesAndExposesErrorWhenFailureOccurs() = runTest(testDispatcher) {
        val user = AuthUser("user-1", "user@test.com", "Test User", isAnonymous = false)
        val authRepo = NoOpAuthRepository(AuthState.Authenticated(user))
        val dao = FakeHydrationDao().apply {
            shouldThrowOnQuery = true
        }

        val syncRepo = SupabaseSyncRepository(
            context = context,
            hydrationDao = dao,
            authRepository = authRepo,
            supabaseClientProvider = { null },
            ioDispatcher = testDispatcher
        )

        val result = syncRepo.sync()
        assertTrue(result is SyncResult.Error)
        val error = result as SyncResult.Error
        assertEquals("Database query failed", error.message)
        assertEquals("Database query failed", syncRepo.lastSyncError.value)
    }

    @Test
    fun scheduleAndTriggerDoNotThrow() {
        val authRepo = NoOpAuthRepository(AuthState.Unauthenticated)
        val dao = FakeHydrationDao()
        val syncRepo = SupabaseSyncRepository(
            context = context,
            hydrationDao = dao,
            authRepository = authRepo,
            supabaseClientProvider = { null },
            ioDispatcher = testDispatcher
        )

        // Verifying that calls complete gracefully
        syncRepo.schedulePeriodicSync()
        syncRepo.triggerImmediateSync()
    }
}
