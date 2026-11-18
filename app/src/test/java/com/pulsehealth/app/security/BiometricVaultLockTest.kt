package com.pulsehealth.app.security

import com.pulsehealth.app.data.LockTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BiometricVaultLockTest {

    @Test
    fun lockTimeout_fromSeconds_returnsExpectedTimeout() {
        assertEquals(LockTimeout.IMMEDIATELY, LockTimeout.fromSeconds(0L))
        assertEquals(LockTimeout.THIRTY_SECONDS, LockTimeout.fromSeconds(30L))
        assertEquals(LockTimeout.ONE_MINUTE, LockTimeout.fromSeconds(60L))
        assertEquals(LockTimeout.FIVE_MINUTES, LockTimeout.fromSeconds(300L))
        assertEquals(LockTimeout.FIFTEEN_MINUTES, LockTimeout.fromSeconds(900L))
        // Default fallback for unknown seconds
        assertEquals(LockTimeout.IMMEDIATELY, LockTimeout.fromSeconds(12345L))
    }

    @Test
    fun biometricStatus_usabilityAndDescriptions() {
        assertTrue(BiometricStatus.Available.isUsable)
        assertEquals("Biometrics & Device Lock Ready", BiometricStatus.Available.description)

        assertFalse(BiometricStatus.NoHardware.isUsable)
        assertEquals("No Biometric Hardware Detected", BiometricStatus.NoHardware.description)

        assertFalse(BiometricStatus.HardwareUnavailable.isUsable)
        assertEquals("Biometric Sensor Temporarily Unavailable", BiometricStatus.HardwareUnavailable.description)

        // NoneEnrolled allows fallback to Device PIN/Pattern/Password, so isUsable is true
        assertTrue(BiometricStatus.NoneEnrolled.isUsable)
        assertEquals("No Biometrics Enrolled (Device PIN Active)", BiometricStatus.NoneEnrolled.description)

        assertFalse(BiometricStatus.Unsupported.isUsable)
        assertEquals("Biometric Security Not Supported", BiometricStatus.Unsupported.description)
    }

    @Test
    fun lockTimeout_labelsAreAccurate() {
        assertEquals("Immediately", LockTimeout.IMMEDIATELY.label)
        assertEquals("30s", LockTimeout.THIRTY_SECONDS.label)
        assertEquals("1m", LockTimeout.ONE_MINUTE.label)
        assertEquals("5m", LockTimeout.FIVE_MINUTES.label)
        assertEquals("15m", LockTimeout.FIFTEEN_MINUTES.label)
    }
}
