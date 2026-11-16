package com.pulsehealth.app.ui.auth

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pulsehealth.app.ui.theme.PulseTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h900dp")
class AuthScreenComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signInMode_rendersBrandHeadlineInputsAndGuestOption() {
        var guestClicked = false

        composeTestRule.setContent {
            PulseTheme {
                AuthScreen(
                    initialMode = AuthScreenMode.SIGN_IN,
                    onBack = {},
                    onSignIn = { _, _, _ -> },
                    onSignUp = { _, _, _ -> },
                    onSendResetOtp = { _, _ -> },
                    onVerifyOtp = { _, _, _ -> },
                    onUpdatePassword = { _, _ -> },
                    onSignInAsGuest = { guestClicked = true }
                )
            }
        }

        // Brand & title
        composeTestRule.onNodeWithText("Pulse").assertIsDisplayed()
        composeTestRule.onNodeWithText("Health").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign in Your Account").assertIsDisplayed()

        // Input labels
        composeTestRule.onNodeWithText("Enter Your Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter Your Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remember me").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forgot Password?").assertIsDisplayed()

        // Action button & Guest option
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continue as Guest (Local-Only)").assertIsDisplayed()

        // Guest click
        composeTestRule.onNodeWithText("Continue as Guest (Local-Only)").performClick()
        assertTrue(guestClicked)
    }

    @Test
    fun clickingForgotPassword_navigatesToRecoveryMode() {
        composeTestRule.setContent {
            PulseTheme {
                AuthScreen(
                    initialMode = AuthScreenMode.SIGN_IN,
                    onBack = {},
                    onSignIn = { _, _, _ -> },
                    onSignUp = { _, _, _ -> },
                    onSendResetOtp = { _, _ -> },
                    onVerifyOtp = { _, _, _ -> },
                    onUpdatePassword = { _, _ -> },
                    onSignInAsGuest = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Forgot Password?").performClick()

        // Verify recovery screen elements
        composeTestRule.onNodeWithText("Forgot Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Send Code").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remember your password? ").assertIsDisplayed()
    }

    @Test
    fun clickingSignUp_navigatesToRegistrationMode() {
        composeTestRule.setContent {
            PulseTheme {
                AuthScreen(
                    initialMode = AuthScreenMode.SIGN_IN,
                    onBack = {},
                    onSignIn = { _, _, _ -> },
                    onSignUp = { _, _, _ -> },
                    onSendResetOtp = { _, _ -> },
                    onVerifyOtp = { _, _, _ -> },
                    onUpdatePassword = { _, _ -> },
                    onSignInAsGuest = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Sign Up").performClick()

        // Verify Create An Account elements
        composeTestRule.onNodeWithText("Create An Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("I agree to the Health Privacy Policy & Terms").assertIsDisplayed()
        composeTestRule.onNodeWithText("Already have an account? ").assertIsDisplayed()
    }

    @Test
    fun verificationCodeMode_rendersDigitInputsAndResendLink() {
        composeTestRule.setContent {
            PulseTheme {
                AuthScreen(
                    initialMode = AuthScreenMode.VERIFY_OTP,
                    onBack = {},
                    onSignIn = { _, _, _ -> },
                    onSignUp = { _, _, _ -> },
                    onSendResetOtp = { _, _ -> },
                    onVerifyOtp = { _, _, _ -> },
                    onUpdatePassword = { _, _ -> },
                    onSignInAsGuest = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Verification Code").assertIsDisplayed()
        composeTestRule.onNodeWithText("Verify").assertIsDisplayed()
        composeTestRule.onNodeWithText("Resend").assertIsDisplayed()
    }

    @Test
    fun resetPasswordMode_rendersNewPasswordFields() {
        composeTestRule.setContent {
            PulseTheme {
                AuthScreen(
                    initialMode = AuthScreenMode.RESET_PASSWORD,
                    onBack = {},
                    onSignIn = { _, _, _ -> },
                    onSignUp = { _, _, _ -> },
                    onSendResetOtp = { _, _ -> },
                    onVerifyOtp = { _, _, _ -> },
                    onUpdatePassword = { _, _ -> },
                    onSignInAsGuest = {}
                )
            }
        }

        // Header title and button both have "Reset Password"
        composeTestRule.onAllNodesWithText("Reset Password").assertCountEquals(2)
        composeTestRule.onNodeWithText("Enter New Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm New Password").assertIsDisplayed()
    }
}
