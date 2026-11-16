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
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
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
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.WarmOrange

/**
 * Distinct workflow modes for the unified executive authentication experience.
 */
enum class AuthScreenMode {
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
            AuthScreenMode.SIGN_IN -> onBack()
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
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        focusedBorderColor = PulsePink,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        focusedLabelColor = PulsePink,
        unfocusedLabelColor = Muted,
        focusedLeadingIconColor = PulsePink,
        unfocusedLeadingIconColor = Muted
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        PulsePink.copy(alpha = 0.10f),
                        WarmOrange.copy(alpha = 0.06f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        when (mode) {
                            AuthScreenMode.SIGN_IN -> onBack()
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
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            // Brand Header with Health Curve & Heartbeat
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = PulsePink.copy(alpha = 0.12f),
                    border = BorderStroke(1.5.dp, PulsePink.copy(alpha = 0.3f)),
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = PulsePink,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pulse",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Health",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PulsePink
                    )
                }

                Text(
                    text = "Personal & Private Wellness Vault",
                    fontSize = 12.sp,
                    color = Muted,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }

            // Bottom White Card Container with Rounded Top Corners
            Surface(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
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
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
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
                            shape = RoundedCornerShape(14.dp),
                            color = ExerciseGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, ExerciseGreen.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ExerciseGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = successMessage ?: "",
                                    color = MaterialTheme.colorScheme.onSurface,
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
// Screen 1: Sign in Your Account
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
    onNavigateToSignUp: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign in Your Account",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Enter Your Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Enter Your Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSignInSubmit() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        // Remember Me & Forgot Password Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onToggleRememberMe(!rememberMe) }
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (rememberMe) PulsePink else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (rememberMe) PulsePink else Muted),
                    modifier = Modifier.size(18.dp)
                ) {
                    if (rememberMe) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Remember me",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onForgotPasswordClick) {
                Text(
                    text = "Forgot Password?",
                    fontSize = 12.sp,
                    color = WarmOrange,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Sign In Action Button
        Button(
            onClick = onSignInSubmit,
            enabled = !isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PulsePink),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Sign in to account"
                }
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Signing In...", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Text("  Or sign in with  ", color = Muted, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }

        Spacer(Modifier.height(16.dp))

        // Alternative Local-Only Guest Button
        OutlinedButton(
            onClick = onGuestClick,
            enabled = !isLoading,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = ExerciseGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Continue as Guest (Local-Only)", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }

        Spacer(Modifier.height(24.dp))

        // Footer Switch to Sign Up
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an account? ", color = Muted, fontSize = 13.sp)
            Text(
                text = "Sign Up",
                color = PulsePink,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.clickable { onNavigateToSignUp() }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Screen 2: Create An Account
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
    onNavigateToSignIn: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create An Account",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Enter Your Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password (min 6 characters)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onToggleConfirmPasswordVisibility) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(14.dp),
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
                .clickable { onToggleAgreeToTerms(!agreeToTerms) }
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (agreeToTerms) PulsePink else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (agreeToTerms) PulsePink else Muted),
                modifier = Modifier.size(18.dp)
            ) {
                if (agreeToTerms) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "I agree to the Health Privacy Policy & Terms",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onSignUpSubmit,
            enabled = !isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PulsePink),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Create new account"
                }
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Creating Account...", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Text("Sign Up", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Text("  Or sign up with  ", color = Muted, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onGuestClick,
            enabled = !isLoading,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = ExerciseGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Continue as Guest (Local-Only)", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }

        Spacer(Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ", color = Muted, fontSize = 13.sp)
            Text(
                text = "Sign In",
                color = PulsePink,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.clickable { onNavigateToSignIn() }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Screen 3: Forgot Password
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
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Please enter your email address and we'll send a 6-digit verification code to recover your account.",
            fontSize = 13.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Enter Your Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSendOtpSubmit() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onSendOtpSubmit,
            enabled = !isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PulsePink),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Send verification code"
                }
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Sending Code...", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Text("Send Code", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Remember your password? ", color = Muted, fontSize = 13.sp)
            Text(
                text = "Sign In",
                color = PulsePink,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.clickable { onBackToSignIn() }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Screen 4: Verification Code (OTP)
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
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "We sent a 6-digit code to $email. Please check your inbox and enter your code below.",
            fontSize = 13.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(28.dp))

        // 6-digit OTP Box Inputs
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
                        .height(56.dp)
                        .focusRequester(focusRequesters[i])
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.key == Key.Backspace && otpDigits[i].isEmpty() && i > 0) {
                                focusRequesters[i - 1].requestFocus()
                                true
                            } else false
                        },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if (i == 5) ImeAction.Done else ImeAction.Next),
                    keyboardActions = KeyboardActions(onDone = { onVerifySubmit() }),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PulsePink,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onVerifySubmit,
            enabled = !isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PulsePink),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Verify authentication code"
                }
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Verifying...", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Text("Verify", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Didn't receive the code? ", color = Muted, fontSize = 13.sp)
            Text(
                text = "Resend",
                color = WarmOrange,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.clickable { onResendCode() }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Screen 5: Reset Password
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
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Create a secure new password with at least 6 characters.",
            fontSize = 13.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            label = { Text("Enter New Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirm New Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onToggleConfirmPasswordVisibility) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onResetSubmit() }),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onResetSubmit,
            enabled = !isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PulsePink),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Save new password"
                }
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Updating Password...", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Text("Reset Password", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
