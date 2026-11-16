package com.pulsehealth.app

import com.pulsehealth.app.util.NoOpNetworkMonitor
import com.pulsehealth.app.util.TestNetworkMonitor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@kotlinx.coroutines.ExperimentalCoroutinesApi
class NetworkMonitorTest {

    @Test
    fun noOpNetworkMonitor_emitsConfiguredDefault() = runTest {
        val monitorOnline = NoOpNetworkMonitor(defaultOnline = true)
        val monitorOffline = NoOpNetworkMonitor(defaultOnline = false)

        assertTrue(monitorOnline.isOnline.first())
        assertFalse(monitorOffline.isOnline.first())
    }

    @Test
    fun testNetworkMonitor_startsWithInitialStateAndUpdates() = runTest {
        val monitor = TestNetworkMonitor(initialOnline = true)
        assertTrue(monitor.isOnline.first())

        monitor.setOnline(false)
        assertFalse(monitor.isOnline.first())

        monitor.setOnline(true)
        assertTrue(monitor.isOnline.first())
    }

    @Test
    fun testNetworkMonitor_emitsSequentialTransitionsToCollectors() = runTest {
        val monitor = TestNetworkMonitor(initialOnline = false)
        val collected = mutableListOf<Boolean>()

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            monitor.isOnline.collect { collected.add(it) }
        }

        monitor.setOnline(true)
        monitor.setOnline(false)
        monitor.setOnline(true)

        job.cancel()

        assertTrue(collected.contains(false))
        assertTrue(collected.contains(true))
        assertEquals(true, collected.last())
    }
}
