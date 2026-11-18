package com.pulsehealth.app.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

sealed interface BiometricStatus {
    val description: String
    val isUsable: Boolean

    data object Available : BiometricStatus {
        override val description: String = "Biometrics & Device Lock Ready"
        override val isUsable: Boolean = true
    }
    data object NoHardware : BiometricStatus {
        override val description: String = "No Biometric Hardware Detected"
        override val isUsable: Boolean = false
    }
    data object HardwareUnavailable : BiometricStatus {
        override val description: String = "Biometric Sensor Temporarily Unavailable"
        override val isUsable: Boolean = false
    }
    data object NoneEnrolled : BiometricStatus {
        override val description: String = "No Biometrics Enrolled (Device PIN Active)"
        override val isUsable: Boolean = true
    }
    data object Unsupported : BiometricStatus {
        override val description: String = "Biometric Security Not Supported"
        override val isUsable: Boolean = false
    }
}

sealed interface BiometricAuthResult {
    data object Success : BiometricAuthResult
    data class Error(val errorCode: Int, val errString: CharSequence) : BiometricAuthResult
    data object Failed : BiometricAuthResult
    data object UserCanceled : BiometricAuthResult
}

object BiometricAuthManager {

    fun getBiometricStatus(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NoHardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NoneEnrolled
            else -> BiometricStatus.Unsupported
        }
    }

    fun canAuthenticate(context: Context): Boolean {
        return getBiometricStatus(context) == BiometricStatus.Available
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock Pulse Health",
        subtitle: String = "Verify your identity to access health records",
        onResult: (BiometricAuthResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onResult(BiometricAuthResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onResult(BiometricAuthResult.UserCanceled)
                } else {
                    onResult(BiometricAuthResult.Error(errorCode, errString))
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onResult(BiometricAuthResult.Failed)
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        val prompt = BiometricPrompt(activity, executor, callback)
        prompt.authenticate(promptInfo)
    }
}
