package com.pulsehealth.app.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.ForestEmerald
import com.pulsehealth.app.ui.theme.ForestEmeraldDark
import com.pulsehealth.app.ui.theme.ForestEmeraldLight
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.SoftCreamBackground
import com.pulsehealth.app.ui.theme.WarmAccent
import com.pulsehealth.app.ui.theme.WarmAccentLight
import com.pulsehealth.app.ui.theme.WarmOrange

/**
 * Distinct workflow modes for the unified executive authentication experience,
 * matching the 6-screen mobile design specification.
 */
enum class AuthScreenMode {
    WELCOME,
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD,
    VERIFY_OTP,
    RESET_PASSWORD
}

@Composable
fun AuthScreen(
    initialMode: AuthScreenMode = AuthScreenMode.SIGN_IN,
    onBack: () -> Unit,
    onSignIn: (email: String, password: String, onError: (String) -> Unit) -> Unit,
    onSignUp: (email: String, password: String, onError: (String) -> Unit) -> Unit,
    onSendResetOtp: (email: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    onVerifyOtp: (email: String, otp: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    onUpdatePassword: (newPassword: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    onSignInAsGuest: () -> Unit,
    onBiometricSignIn: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(initialMode) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var agreeToTerms by remember { mutableStateOf(false) }
    var rawErrorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // OTP state (6 digits)
    val otpDigits = remember { mutableStateListOf("", "", "", "", "", "") }

    // Handle system back navigation
    BackHandler {
        when (mode) {
            AuthScreenMode.WELCOME -> onBack()
            AuthScreenMode.SIGN_IN -> {
                if (initialMode == AuthScreenMode.WELCOME) {
                    mode = AuthScreenMode.WELCOME
                } else {
                    onBack()
                }
            }
            AuthScreenMode.SIGN_UP -> mode = AuthScreenMode.SIGN_IN
            AuthScreenMode.FORGOT_PASSWORD -> mode = AuthScreenMode.SIGN_IN
            AuthScreenMode.VERIFY_OTP -> mode = AuthScreenMode.FORGOT_PASSWORD
            AuthScreenMode.RESET_PASSWORD -> mode = AuthScreenMode.VERIFY_OTP
        }
    }

    // Double-layer sanitizer: Guarantee that NO technical headers, URLs, or API keys ever leak to the screen
    val displayError = remember(rawErrorMessage) {
        val raw = rawErrorMessage?.trim() ?: return@remember null
        val lower = raw.lowercase(java.util.Locale.ROOT)
        when {
            lower.contains("apikey") || lower.contains("bearer") || lower.contains("header") ||
                    lower.contains("url:") || lower.contains("http") || lower.contains("sb_publishable") ->
                "Authentication failed. Please verify your email and credentials."
            raw.length > 90 ->
                raw.substringBefore("\n").substringBefore("URL:").trim().take(90)
            else -> raw
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFF6F8F7),
        unfocusedContainerColor = Color(0xFFF6F8F7),
        disabledContainerColor = Color(0xFFECEFEF),
        focusedBorderColor = ForestEmerald,
        unfocusedBorderColor = Color(0xFFE2E8E6),
        focusedLabelColor = ForestEmerald,
        unfocusedLabelColor = Muted,
        focusedLeadingIconColor = ForestEmerald,
        unfocusedLeadingIconColor = Muted,
        cursorColor = ForestEmerald
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SoftCreamBackground)
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar (with Back button when not on welcome screen)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mode != AuthScreenMode.WELCOME) {
                    IconButton(
                        onClick = {
                            when (mode) {
                                AuthScreenMode.WELCOME -> onBack()
                                AuthScreenMode.SIGN_IN -> {
                                    if (initialMode == AuthScreenMode.WELCOME) {
                                        mode = AuthScreenMode.WELCOME
                                    } else {
                                        onBack()
                                    }
                                }
                                AuthScreenMode.SIGN_UP -> mode = AuthScreenMode.SIGN_IN
                                AuthScreenMode.FORGOT_PASSWORD -> mode = AuthScreenMode.SIGN_IN
                                AuthScreenMode.VERIFY_OTP -> mode = AuthScreenMode.FORGOT_PASSWORD
                                AuthScreenMode.RESET_PASSWORD -> mode = AuthScreenMode.VERIFY_OTP
                            }
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ForestEmeraldDark
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
            }

            // Top Artistic Botanical Health Header
            PulseBrandHeaderArt(
                isCompact = mode != AuthScreenMode.WELCOME,
                showTagline = mode == AuthScreenMode.WELCOME
            )

            // Bottom White Curved Sheet Container
            Surface(
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Inline Error Notice (Zero technical leak guaranteed)
                    AnimatedVisibility(visible = displayError != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = displayError ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Inline Success Notice
                    AnimatedVisibility(visible = successMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ForestEmeraldLight,
                            border = BorderStroke(1.dp, ForestEmerald.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ForestEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = successMessage ?: "",
                                    color = ForestEmeraldDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Animated Switch Between Auth Screen Modes
                    AnimatedContent(
                        targetState = mode,
                        transitionSpec = {
                            if (targetState.ordinal > initialState.ordinal) {
                                slideInHorizontally { width -> width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            } else {
                                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> width } + fadeOut()
                            }
                        },
                        label = "AuthModeTransition"
                    ) { targetMode ->
                        when (targetMode) {
                            AuthScreenMode.WELCOME -> {
                                WelcomeContent(
                                    onGetStarted = {
                                        rawErrorMessage = null
                                        successMessage = null
                                        mode = AuthScreenMode.SIGN_UP
                                    },
                                    onSignIn = {
                                        rawErrorMessage = null
                                        successMessage = null
                                        mode = AuthScreenMode.SIGN_IN
                                    },
                                    onGuestClick = onSignInAsGuest
                                )
                            }
                            AuthScreenMode.SIGN_IN -> {
                                SignInContent(
                                    email = email,
                                    onEmailChange = { email = it; rawErrorMessage = null },
                                    password = password,
                                    onPasswordChange = { password = it; rawErrorMessage = null },
                                    passwordVisible = passwordVisible,
                                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                                    rememberMe = rememberMe,
                                    onToggleRememberMe = { rememberMe = it },
                                    isLoading = isLoading,
                                    fieldColors = fieldColors,
                                    onForgotPasswordClick = {
                                        rawErrorMessage = null
                                        successMessage = null
                                        mode = AuthScreenMode.FORGOT_PASSWORD
                                    },
                                    onSignInSubmit = {
                                        if (email.isBlank() || password.isBlank()) {
                                            rawErrorMessage = "Please enter your email and password."
                                            return@SignInContent
                                        }
                                        isLoading = true
                                        rawErrorMessage = null
                                        onSignIn(email, password) { err ->
                                            isLoading = false
                                            rawErrorMessage = err
                                        }
                                    },
                                    onGuestClick = onSignInAsGuest,
                                    onBiometricClick = onBiometricSignIn,
                                    onNavigateToSignUp = {
                                        rawErrorMessage = null
                                        successMessage = null
                                        mode = AuthScreenMode.SIGN_UP
                                    }
                                )
                            }
                            AuthScreenMode.SIGN_UP -> {
                                SignUpContent(
                                    email = email,
                                    onEmailChange = { email = it; rawErrorMessage = null },
                                    password = password,
                                    onPasswordChange = { password = it; rawErrorMessage = null },
                                    confirmPassword = confirmPassword,
                                    onConfirmPasswordChange = { confirmPassword = it; rawErrorMessage = null },
                                    passwordVisible = passwordVisible,
                                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                                    confirmPasswordVisible = confirmPasswordVisible,
                                    onToggleConfirmPasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                                    agreeToTerms = agreeToTerms,
                                    onToggleAgreeToTerms = { agreeToTerms = it },
                                    isLoading = isLoading,
                                    fieldColors = fieldColors,
                                    onSignUpSubmit = {
                                        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                            rawErrorMessage = "Please fill in all account fields."
                                            return@SignUpContent
                                        }
                                        if (password != confirmPassword) {
                                            rawErrorMessage = "Passwords do not match. Please re-enter."
                                            return@SignUpContent
                                        }
                                        if (!agreeToTerms) {
                                            rawErrorMessage = "Please accept the Privacy Policy to create an account."
                                            return@SignUpContent
                                        }
                                        isLoading = true
                                        rawErrorMessage = null
                                        onSignUp(email, password) { err ->
                                            isLoading = false
                                            rawErrorMessage = err
                                        }
                                    },
                                    onGuestClick = onSignInAsGuest,
                                    onBiometricClick = onBiometricSignIn,
                                    onNavigateToSignIn = {
                                        rawErrorMessage = null
                                        successMessage = null
                                        mode = AuthScreenMode.SIGN_IN
                                    }
                                )
                            }
                            AuthScreenMode.FORGOT_PASSWORD -> {
                                ForgotPasswordContent(
                                    email = email,
                                    onEmailChange = { email = it; rawErrorMessage = null },
                                    isLoading = isLoading,
                                    fieldColors = fieldColors,
                                    onSendOtpSubmit = {
                                        if (email.isBlank()) {
                                            rawErrorMessage = "Please enter your registered email address."
                                            return@ForgotPasswordContent
                                        }
                                        isLoading = true
                                        rawErrorMessage = null
                                        onSendResetOtp(email) { success, err ->
                                            isLoading = false
                                            if (success) {
                                                successMessage = "Verification code sent! Please check your inbox."
                                                mode = AuthScreenMode.VERIFY_OTP
                                            } else {
                                                rawErrorMessage = err ?: "Unable to send verification code. Try again."
                                            }
                                        }
                                    },
                                    onBackToSignIn = {
                                        rawErrorMessage = null
                                        successMessage = null
                                        mode = AuthScreenMode.SIGN_IN
                                    }
                                )
                            }
                            AuthScreenMode.VERIFY_OTP -> {
                                VerifyOtpContent(
                                    email = email,
                                    otpDigits = otpDigits,
                                    isLoading = isLoading,
                                    onVerifySubmit = {
                                        val code = otpDigits.joinToString("")
                                        if (code.length < 6) {
                                            rawErrorMessage = "Please enter the complete 6-digit verification code."
                                            return@VerifyOtpContent
                                        }
                                        isLoading = true
                                        rawErrorMessage = null
                                        onVerifyOtp(email, code) { success, err ->
                                            isLoading = false
                                            if (success) {
                                                successMessage = "Code verified successfully! Choose your new password."
                                                mode = AuthScreenMode.RESET_PASSWORD
                                            } else {
                                                rawErrorMessage = err ?: "Invalid code. Please check and try again."
                                            }
                                        }
                                    },
                                    onResendCode = {
                                        isLoading = true
                                        onSendResetOtp(email) { success, _ ->
                                            isLoading = false
                                            if (success) {
                                                successMessage = "A fresh code has been sent to your email."
                                            }
                                        }
                                    },
                                    onBackToForgot = {
                                        rawErrorMessage = null
                                        mode = AuthScreenMode.FORGOT_PASSWORD
                                    }
                                )
                            }
                            AuthScreenMode.RESET_PASSWORD -> {
                                ResetPasswordContent(
                                    newPassword = password,
                                    onNewPasswordChange = { password = it; rawErrorMessage = null },
                                    confirmPassword = confirmPassword,
                                    onConfirmPasswordChange = { confirmPassword = it; rawErrorMessage = null },
                                    passwordVisible = passwordVisible,
                                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                                    confirmPasswordVisible = confirmPasswordVisible,
                                    onToggleConfirmPasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                                    isLoading = isLoading,
                                    fieldColors = fieldColors,
                                    onResetSubmit = {
                                        if (password.length < 6) {
                                            rawErrorMessage = "Password must be at least 6 characters long."
                                            return@ResetPasswordContent
                                        }
                                        if (password != confirmPassword) {
                                            rawErrorMessage = "Passwords do not match. Please verify."
                                            return@ResetPasswordContent
                                        }
                                        isLoading = true
                                        rawErrorMessage = null
                                        onUpdatePassword(password) { success, err ->
                                            isLoading = false
                                            if (success) {
                                                successMessage = "Password successfully reset! You can now sign in."
                                                password = ""
                                                confirmPassword = ""
                                                mode = AuthScreenMode.SIGN_IN
                                            } else {
                                                rawErrorMessage = err ?: "Failed to reset password."
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Screen 1: Welcome / Splash Screen
// ---------------------------------------------------------------------------
@Composable
private fun WelcomeContent(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    onGuestClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Health, Fully Private",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestEmeraldDark,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Track vitals, medications, hydration & clinical reports with 100% on-device encryption.",
            fontSize = 14.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(28.dp))

        // Feature highlights row
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WelcomeHighlightRow(
                icon = Icons.Default.Shield,
                title = "Local-First Encryption",
                subtitle = "Zero cloud leakage without explicit user consent"
            )
            WelcomeHighlightRow(
                icon = Icons.Default.CheckCircle,
                title = "Comprehensive Vitals & Meds",
                subtitle = "Track blood pressure, daily doses, and sleep staging"
            )
        }

        Spacer(Modifier.height(32.dp))

        // Get Started Primary CTA
        Button(
            onClick = onGetStarted,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Get started with Pulse Health"
                }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        // Sign In Secondary
        OutlinedButton(
            onClick = onSignIn,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.5.dp, ForestEmerald),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("I Already Have an Account", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ForestEmerald)
        }

        Spacer(Modifier.height(20.dp))

        // Guest local mode
        TextButton(onClick = onGuestClick) {
            Text(
                text = "Continue as Guest (Local-Only)",
                color = Muted,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun WelcomeHighlightRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF7FAF9),
        border = BorderStroke(1.dp, Color(0xFFE4EDE9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = ForestEmeraldLight,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = ForestEmerald, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ForestEmeraldDark)
                Text(subtitle, fontSize = 12.sp, color = Muted)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Screen 2: Sign in Your Account
// ---------------------------------------------------------------------------
@Composable
private fun SignInContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    rememberMe: Boolean,
    onToggleRememberMe: (Boolean) -> Unit,
    isLoading: Boolean,
    fieldColors: androidx.compose.material3.TextFieldColors,
    onForgotPasswordClick: () -> Unit,
    onSignInSubmit: () -> Unit,
    onGuestClick: () -> Unit,
    onBiometricClick: () -> Unit = {},
    onNavigateToSignUp: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign in Your Account",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestEmeraldDark,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(24.dp))

        // Email Field (Pill Shape with Soft Container)
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("Enter Your Email") },
            label = { Text("Enter Your Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ForestEmerald) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Password Field (Pill Shape)
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("Enter Your Password") },
            label = { Text("Enter Your Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ForestEmerald) },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Muted
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(24.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSignInSubmit() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Remember Me & Forgot Password Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleRememberMe(!rememberMe) }
                    .padding(vertical = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (rememberMe) ForestEmerald else Color(0xFFF0F4F2),
                    border = BorderStroke(1.dp, if (rememberMe) ForestEmerald else Color(0xFFCCD7D3)),
                    modifier = Modifier.size(20.dp)
                ) {
                    if (rememberMe) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Remember me",
                    fontSize = 13.sp,
                    color = ForestEmeraldDark,
                    fontWeight = FontWeight.Medium
                )
            }

            TextButton(onClick = onForgotPasswordClick) {
                Text(
                    text = "Forgot Password?",
                    fontSize = 13.sp,
                    color = WarmAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Sign In Action Button (Full Width Forest Emerald Pill)
        Button(
            onClick = onSignInSubmit,
            enabled = !isLoading,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Sign in to account"
                }
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                Spacer(Modifier.width(10.dp))
                Text("Signing In...", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Horizontal Divider "Or sign in with"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8E6))
            Text("  Or sign in with  ", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8E6))
        }

        Spacer(Modifier.height(18.dp))

        // Social / Alternative Auth Buttons Row
        SocialAuthButtonsRow(
            onGoogleClick = { /* Handled via Google Auth / IDP */ },
            onBiometricClick = onBiometricClick,
            onGuestClick = onGuestClick
        )

        Spacer(Modifier.height(16.dp))

        // Full Local Guest Option
        OutlinedButton(
            onClick = onGuestClick,
            enabled = !isLoading,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            border = BorderStroke(1.dp, Color(0xFFD4E0DC))
        ) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = ExerciseGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Continue as Guest (Local-Only)",
                color = ForestEmeraldDark,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(26.dp))

        // Footer Switch to Sign Up
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an account? ", color = Muted, fontSize = 13.sp)
            Text(
                text = "Sign Up",
                color = WarmAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onNavigateToSignUp() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Screen 3: Create An Account
// ---------------------------------------------------------------------------
@Composable
private fun SignUpContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    confirmPasswordVisible: Boolean,
    onToggleConfirmPasswordVisibility: () -> Unit,
    agreeToTerms: Boolean,
    onToggleAgreeToTerms: (Boolean) -> Unit,
    isLoading: Boolean,
    fieldColors: androidx.compose.material3.TextFieldColors,
    onSignUpSubmit: () -> Unit,
    onGuestClick: () -> Unit,
    onBiometricClick: () -> Unit = {},
    onNavigateToSignIn: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create An Account",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestEmeraldDark,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("Enter Your Email") },
            label = { Text("Enter Your Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ForestEmerald) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text("Password (min 6 characters)") },
            label = { Text("Password (min 6 characters)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ForestEmerald) },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Muted
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(24.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = { Text("Confirm Password") },
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ForestEmerald) },
            trailingIcon = {
                IconButton(onClick = onToggleConfirmPasswordVisibility) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                        tint = Muted
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(24.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSignUpSubmit() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        // Agree to Terms Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onToggleAgreeToTerms(!agreeToTerms) }
                .padding(vertical = 4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (agreeToTerms) ForestEmerald else Color(0xFFF0F4F2),
                border = BorderStroke(1.dp, if (agreeToTerms) ForestEmerald else Color(0xFFCCD7D3)),
                modifier = Modifier.size(20.dp)
            ) {
                if (agreeToTerms) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "I agree to the Health Privacy Policy & Terms",
                fontSize = 12.sp,
                color = ForestEmeraldDark,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onSignUpSubmit,
            enabled = !isLoading,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Create new account"
                }
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                Spacer(Modifier.width(10.dp))
                Text("Creating Account...", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Text("Sign Up", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8E6))
            Text("  Or sign up with  ", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8E6))
        }

        Spacer(Modifier.height(18.dp))

        SocialAuthButtonsRow(
            onGoogleClick = { /* Google sign up */ },
            onBiometricClick = onBiometricClick,
            onGuestClick = onGuestClick
        )

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onGuestClick,
            enabled = !isLoading,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            border = BorderStroke(1.dp, Color(0xFFD4E0DC))
        ) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = ExerciseGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Continue as Guest (Local-Only)", color = ForestEmeraldDark, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }

        Spacer(Modifier.height(26.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ", color = Muted, fontSize = 13.sp)
            Text(
                text = "Sign In",
                color = WarmAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onNavigateToSignIn() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Screen 4: Forgot Password
// ---------------------------------------------------------------------------
@Composable
private fun ForgotPasswordContent(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    fieldColors: androidx.compose.material3.TextFieldColors,
    onSendOtpSubmit: () -> Unit,
    onBackToSignIn: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Forgot Password",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestEmeraldDark,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Please enter your email address then you get a code in your email",
            fontSize = 13.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text("Enter Your Email") },
            label = { Text("Enter Your Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ForestEmerald) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSendOtpSubmit() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(26.dp))

        Button(
            onClick = onSendOtpSubmit,
            enabled = !isLoading,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Send verification code"
                }
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                Spacer(Modifier.width(10.dp))
                Text("Sending Code...", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Text("Send Code", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(Modifier.height(26.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Remember your password? ", color = Muted, fontSize = 13.sp)
            Text(
                text = "Sign In",
                color = WarmAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onBackToSignIn() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Screen 5: Verification Code (OTP)
// ---------------------------------------------------------------------------
@Composable
private fun VerifyOtpContent(
    email: String,
    otpDigits: androidx.compose.runtime.snapshots.SnapshotStateList<String>,
    isLoading: Boolean,
    onVerifySubmit: () -> Unit,
    onResendCode: () -> Unit,
    onBackToForgot: () -> Unit
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequesters.firstOrNull()?.requestFocus()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Verification Code",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestEmeraldDark,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "We sent a 6-digit code to your email address please check & enter your code",
            fontSize = 13.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(28.dp))

        // 6-digit OTP Box Inputs (Rounded Square Boxes)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 6) {
                OutlinedTextField(
                    value = otpDigits[i],
                    onValueChange = { value ->
                        if (value.length <= 1) {
                            otpDigits[i] = value
                            if (value.isNotEmpty() && i < 5) {
                                focusRequesters[i + 1].requestFocus()
                            } else if (value.isNotEmpty() && i == 5) {
                                focusManager.clearFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .width(48.dp)
                        .height(58.dp)
                        .focusRequester(focusRequesters[i])
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.key == Key.Backspace && otpDigits[i].isEmpty() && i > 0) {
                                focusRequesters[i - 1].requestFocus()
                                true
                            } else false
                        },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        color = ForestEmeraldDark
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (i == 5) ImeAction.Done else ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onDone = { onVerifySubmit() }),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestEmerald,
                        unfocusedBorderColor = Color(0xFFD4E0DC),
                        focusedContainerColor = ForestEmeraldLight,
                        unfocusedContainerColor = Color(0xFFF6F8F7),
                        cursorColor = ForestEmerald
                    )
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onVerifySubmit,
            enabled = !isLoading,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Verify authentication code"
                }
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                Spacer(Modifier.width(10.dp))
                Text("Verifying...", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Text("Verify", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Didn't receive the code? ", color = Muted, fontSize = 13.sp)
            Text(
                text = "Resend",
                color = WarmAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onResendCode() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Screen 6: Reset Password
// ---------------------------------------------------------------------------
@Composable
private fun ResetPasswordContent(
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    confirmPasswordVisible: Boolean,
    onToggleConfirmPasswordVisibility: () -> Unit,
    isLoading: Boolean,
    fieldColors: androidx.compose.material3.TextFieldColors,
    onResetSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Reset Password",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ForestEmeraldDark,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "At least 8 characters with uppercase and lowercase letters",
            fontSize = 13.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            placeholder = { Text("Enter New Password") },
            label = { Text("Enter New Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ForestEmerald) },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Muted
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(24.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = { Text("Confirm New Password") },
            label = { Text("Confirm New Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ForestEmerald) },
            trailingIcon = {
                IconButton(onClick = onToggleConfirmPasswordVisibility) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                        tint = Muted
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(24.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onResetSubmit() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Dynamic Password Strength Meter
        val strength = remember(newPassword) {
            when {
                newPassword.length >= 8 && newPassword.any { it.isUpperCase() } && newPassword.any { it.isDigit() } -> 3
                newPassword.length >= 6 -> 2
                newPassword.isNotEmpty() -> 1
                else -> 0
            }
        }

        if (newPassword.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (s in 1..3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    s <= strength && strength == 3 -> ExerciseGreen
                                    s <= strength && strength == 2 -> WarmOrange
                                    s <= strength -> MaterialTheme.colorScheme.error
                                    else -> Color(0xFFE2E8E6)
                                }
                            )
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = when (strength) {
                    3 -> "Strong password"
                    2 -> "Fair password (add numbers/capitals)"
                    else -> "Weak password"
                },
                fontSize = 11.sp,
                color = when (strength) {
                    3 -> ExerciseGreen
                    2 -> WarmOrange
                    else -> MaterialTheme.colorScheme.error
                },
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onResetSubmit,
            enabled = !isLoading,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Save new password"
                }
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                Spacer(Modifier.width(10.dp))
                Text("Updating Password...", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Text("Reset Password", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Social Auth Buttons Row (Google, Biometric/Passkey, Local-Only)
// ---------------------------------------------------------------------------
@Composable
private fun SocialAuthButtonsRow(
    onGoogleClick: () -> Unit,
    onBiometricClick: () -> Unit,
    onGuestClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Google Button
        Surface(
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8E6)),
            shadowElevation = 1.dp,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onGoogleClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "G",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF4285F4)
                )
            }
        }

        Spacer(Modifier.width(20.dp))

        // Biometric / Passkey Button
        Surface(
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8E6)),
            shadowElevation = 1.dp,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onBiometricClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Sign in with Biometrics or Passkey",
                    tint = ForestEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.width(20.dp))

        // Privacy Guest Shield Button
        Surface(
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8E6)),
            shadowElevation = 1.dp,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onGuestClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Local Guest Session",
                    tint = ExerciseGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
