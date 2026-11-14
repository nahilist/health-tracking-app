package com.pulsehealth.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.WarmOrange

@Composable
fun AuthDialog(
    onDismiss: () -> Unit,
    onSignIn: (email: String, password: String, onError: (String) -> Unit) -> Unit,
    onSignUp: (email: String, password: String, onError: (String) -> Unit) -> Unit,
    onGuest: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Sign Up
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rawErrorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Double-layer sanitizer: guarantee no technical headers, URLs, or API keys are ever rendered
    val displayError = remember(rawErrorMessage) {
        val raw = rawErrorMessage?.trim() ?: return@remember null
        val lower = raw.lowercase(java.util.Locale.ROOT)
        when {
            lower.contains("apikey") || lower.contains("bearer") || lower.contains("header") || lower.contains("url:") || lower.contains("http") ->
                "Authentication failed. Please verify your email and password."
            raw.length > 90 ->
                raw.substringBefore("\n").substringBefore("URL:").trim().take(90)
            else ->
                raw
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

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(PulsePink.copy(alpha = 0.15f), WarmOrange.copy(alpha = 0.2f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = PulsePink,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = if (selectedTabIndex == 0) "Sign In to Pulse Health" else "Create Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Sync and secure your health data across devices with hardware encryption.",
                    color = Muted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, start = 8.dp, end = 8.dp)
                )
            }
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                // Segmented Pill Control
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Sign In Pill
                        val isSignIn = selectedTabIndex == 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSignIn) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    selectedTabIndex = 0
                                    rawErrorMessage = null
                                }
                                .padding(vertical = 10.dp)
                                .semantics { role = Role.Button },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sign In",
                                fontWeight = if (isSignIn) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSignIn) MaterialTheme.colorScheme.onSurface else Muted,
                                fontSize = 13.sp
                            )
                        }

                        // Sign Up Pill
                        val isSignUp = selectedTabIndex == 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSignUp) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    selectedTabIndex = 1
                                    rawErrorMessage = null
                                }
                                .padding(vertical = 10.dp)
                                .semantics { role = Role.Button },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sign Up",
                                fontWeight = if (isSignUp) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSignUp) MaterialTheme.colorScheme.onSurface else Muted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        rawErrorMessage = null
                    },
                    label = { Text("Email address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        rawErrorMessage = null
                    },
                    label = { Text("Password (min 6 characters)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Muted
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                if (displayError != null) {
                    Spacer(Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
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
                                text = displayError,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        rawErrorMessage = "Please enter both email and password."
                        return@Button
                    }
                    isLoading = true
                    rawErrorMessage = null
                    if (selectedTabIndex == 0) {
                        onSignIn(email, password) { err ->
                            isLoading = false
                            rawErrorMessage = err
                        }
                    } else {
                        onSignUp(email, password) { err ->
                            isLoading = false
                            rawErrorMessage = err
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PulsePink),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Connecting...", fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    Text(
                        if (selectedTabIndex == 0) "Sign In" else "Create Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(
                    onClick = onGuest,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Text("Continue as Guest (Local-Only)", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(6.dp))

                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text("Cancel", color = Muted, fontSize = 13.sp)
                }
            }
        }
    )
}
