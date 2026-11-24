package com.pulsehealth.app.ui.sharing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.ShareDuration
import com.pulsehealth.app.data.ShareRelationship
import com.pulsehealth.app.data.TrustedContact
import com.pulsehealth.app.ui.theme.AthleticOrange
import com.pulsehealth.app.ui.theme.AthleticOrangeLight
import com.pulsehealth.app.ui.theme.DeepCharcoal
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.SleepPurple
import com.pulsehealth.app.ui.theme.SoftPillBackground
import com.pulsehealth.app.ui.theme.StandCyan
import com.pulsehealth.app.ui.theme.WarmOrange
import com.pulsehealth.app.ui.theme.WaterBlue

data class SharingMetricItem(
    val key: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val color: Color,
    val icon: ImageVector
)

val DEFAULT_SHARING_METRICS: List<SharingMetricItem> = listOf(
    SharingMetricItem(
        key = "Steps",
        title = "Steps",
        subtitle = "Daily step count and walking progress",
        category = "Activity & Movement",
        color = WarmOrange,
        icon = Icons.AutoMirrored.Filled.DirectionsRun
    ),
    SharingMetricItem(
        key = "Active calories",
        title = "Active Calories",
        subtitle = "Energy burned moving and during workouts",
        category = "Activity & Movement",
        color = PulsePink,
        icon = Icons.Default.Whatshot
    ),
    SharingMetricItem(
        key = "Exercise",
        title = "Exercise Minutes",
        subtitle = "Moderate-to-vigorous physical activity",
        category = "Activity & Movement",
        color = ExerciseGreen,
        icon = Icons.Default.FitnessCenter
    ),
    SharingMetricItem(
        key = "Stand",
        title = "Stand Hours",
        subtitle = "Hourly standing and active movement",
        category = "Activity & Movement",
        color = StandCyan,
        icon = Icons.Default.AccessibilityNew
    ),
    SharingMetricItem(
        key = "Activity",
        title = "Activity Overview",
        subtitle = "Move, Exercise and Stand ring progress",
        category = "Activity & Movement",
        color = AthleticOrange,
        icon = Icons.AutoMirrored.Filled.DirectionsRun
    ),
    SharingMetricItem(
        key = "Heart rate",
        title = "Heart Rate",
        subtitle = "Resting, active and pulse cardiovascular rates",
        category = "Vitals & Heart",
        color = PulsePink,
        icon = Icons.Default.MonitorHeart
    ),
    SharingMetricItem(
        key = "Sleep",
        title = "Sleep & Rest",
        subtitle = "Duration, sleep stages and sleep consistency",
        category = "Sleep & Recovery",
        color = SleepPurple,
        icon = Icons.Default.NightsStay
    ),
    SharingMetricItem(
        key = "Water",
        title = "Hydration Intake",
        subtitle = "Daily water and fluid tracking records",
        category = "Hydration & Nutrition",
        color = WaterBlue,
        icon = Icons.Default.LocalDrink
    )
)

/**
 * Apple Health style full-page presentation modal for adding a trusted recipient.
 * Features inset grouped cards, categorized permission toggles, relationship chips,
 * and clear end-to-end encryption privacy disclosure.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTrustedRecipientBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, ShareRelationship, String, Set<String>, ShareDuration) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFFF7F9FB),
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
    ) {
        AddTrustedRecipientContent(
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTrustedRecipientContent(
    onDismiss: () -> Unit,
    onConfirm: (String, ShareRelationship, String, Set<String>, ShareDuration) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf(ShareRelationship.DOCTOR) }
    var contactIdentifier by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf(ShareDuration.ONE_MONTH) }
    var selectedMetrics by remember {
        mutableStateOf(setOf("Steps", "Heart rate", "Sleep", "Water"))
    }

    val canSubmit = name.trim().isNotBlank() && selectedMetrics.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FB))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Apple Navigation Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            color = Muted,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "Share Health Data",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal
                    )

                    TextButton(
                        onClick = {
                            if (canSubmit) {
                                onConfirm(name.trim(), relationship, contactIdentifier.trim(), selectedMetrics, duration)
                            }
                        },
                        enabled = canSubmit,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Share",
                            color = if (canSubmit) AthleticOrange else Muted.copy(alpha = 0.4f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Apple Health Hero
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AthleticOrangeLight,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = AthleticOrange,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Share with Someone",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepCharcoal,
                        letterSpacing = (-0.5).sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Choose a healthcare provider, caregiver, or family member to view your wellness records. You control exactly what is shared.",
                        fontSize = 13.sp,
                        color = Muted,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Section 1: Recipient Information
            item {
                Text(
                    text = "RECIPIENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Muted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE8ECEF)),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Recipient Name Input
                        Column {
                            Text(
                                text = "Recipient Name *",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepCharcoal
                            )
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = { Text("e.g. Dr. Sarah Chen or Mom", color = Muted, fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (name.isNotBlank()) AthleticOrange else Muted,
                                        modifier = Modifier.size(19.dp)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AthleticOrange,
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedContainerColor = Color(0xFFFAFAFA),
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Relationship Pills
                        Column {
                            Text(
                                text = "Relationship",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepCharcoal
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Helps identify their access role in health summaries",
                                fontSize = 11.sp,
                                color = Muted
                            )
                            Spacer(Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ShareRelationship.entries.forEach { rel ->
                                    val isSelected = relationship == rel
                                    val icon = when (rel) {
                                        ShareRelationship.DOCTOR -> "🩺"
                                        ShareRelationship.FAMILY -> "👨‍👩‍👧"
                                        ShareRelationship.PARTNER -> "❤️"
                                        ShareRelationship.COACH -> "🏋️"
                                        ShareRelationship.FRIEND -> "🤝"
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) DeepCharcoal else Color(0xFFF1F3F5),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) DeepCharcoal else Color(0xFFE5E7EB)
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { relationship = rel }
                                            .semantics {
                                                role = Role.RadioButton
                                                contentDescription = "${rel.label} relationship"
                                            }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        ) {
                                            Text(icon, fontSize = 13.sp)
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = rel.label,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else DeepCharcoal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Contact Info Input
                        Column {
                            Text(
                                text = "Email or Contact Handle (Optional)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepCharcoal
                            )
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = contactIdentifier,
                                onValueChange = { contactIdentifier = it },
                                placeholder = { Text("e.g. dr.chen@hospital.org", color = Muted, fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AlternateEmail,
                                        contentDescription = null,
                                        tint = Muted,
                                        modifier = Modifier.size(19.dp)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AthleticOrange,
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedContainerColor = Color(0xFFFAFAFA),
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Section 2: Health Data & Permissions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HEALTH DATA PERMISSIONS (${selectedMetrics.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Muted,
                        letterSpacing = 1.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = { selectedMetrics = DEFAULT_SHARING_METRICS.map { it.key }.toSet() },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("All", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AthleticOrange)
                        }
                        TextButton(
                            onClick = { selectedMetrics = setOf("Steps", "Heart rate", "Sleep", "Water") },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Essential", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AthleticOrange)
                        }
                        TextButton(
                            onClick = { selectedMetrics = emptySet() },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Muted)
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE8ECEF)),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        DEFAULT_SHARING_METRICS.forEachIndexed { index, metric ->
                            val isChecked = metric.key in selectedMetrics

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedMetrics = if (isChecked) {
                                            selectedMetrics - metric.key
                                        } else {
                                            selectedMetrics + metric.key
                                        }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                                    .semantics {
                                        role = Role.Switch
                                        contentDescription = "${metric.title} sharing toggle, ${if (isChecked) "authorized" else "not authorized"}"
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = metric.color.copy(alpha = 0.12f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = metric.icon,
                                            contentDescription = null,
                                            tint = metric.color,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = metric.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = DeepCharcoal
                                    )
                                    Text(
                                        text = metric.subtitle,
                                        fontSize = 12.sp,
                                        color = Muted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                Switch(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedMetrics = if (checked) {
                                            selectedMetrics + metric.key
                                        } else {
                                            selectedMetrics - metric.key
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = AthleticOrange,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFE5E7EB),
                                        uncheckedBorderColor = Color.Transparent
                                    )
                                )
                            }

                            if (index < DEFAULT_SHARING_METRICS.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    color = Color(0xFFF1F3F5),
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Access Duration
            item {
                Text(
                    text = "ACCESS DURATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Muted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE8ECEF)),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        ShareDuration.entries.forEachIndexed { index, dur ->
                            val isSelected = duration == dur
                            val description = when (dur) {
                                ShareDuration.ONE_WEEK -> "Ideal for short-term clinic visits and post-op follow-ups"
                                ShareDuration.ONE_MONTH -> "Recommended for monthly check-ins and active fitness coaching"
                                ShareDuration.INDEFINITE -> "Continuous access until you manually revoke permissions"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { duration = dur }
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                                    .semantics {
                                        role = Role.RadioButton
                                        contentDescription = "${dur.label} duration"
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = dur.label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = DeepCharcoal
                                        )
                                        if (dur == ShareDuration.ONE_MONTH) {
                                            Spacer(Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = AthleticOrangeLight
                                            ) {
                                                Text(
                                                    text = "RECOMMENDED",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = AthleticOrange,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = description,
                                        fontSize = 12.sp,
                                        color = Muted
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) AthleticOrange else Color(0xFFD1D5DB),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            if (index < ShareDuration.entries.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF1F3F5), thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            // Apple Privacy & Security Guarantee Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Private & End-to-End Encrypted",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Data is encrypted in transit and on your device. Only the metrics you explicitly select are shared with this contact. You can modify permissions or stop sharing at any time.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF15803D),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Sticky Bottom Authorize Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            color = Color.White.copy(alpha = 0.96f),
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Button(
                    onClick = {
                        if (canSubmit) {
                            onConfirm(name.trim(), relationship, contactIdentifier.trim(), selectedMetrics, duration)
                        }
                    },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AthleticOrange,
                        disabledContainerColor = Color(0xFFE5E7EB),
                        disabledContentColor = Muted
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .semantics {
                            contentDescription = "Authorize and save trusted recipient"
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Authorize & Save",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!canSubmit) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (name.isBlank()) "Enter a recipient name to continue" else "Select at least one metric to share",
                        fontSize = 11.sp,
                        color = Muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Apple Health style modal sheet for editing authorized metrics of an existing contact.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMetricsBottomSheet(
    contact: TrustedContact,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedMetrics by remember { mutableStateOf(contact.sharedMetrics) }
    val canSave = selectedMetrics.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFFF7F9FB),
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FB))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .widthIn(max = 640.dp),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 12.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Muted, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }

                        Text("Edit Permissions", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DeepCharcoal)

                        TextButton(
                            onClick = { if (canSave) onSave(selectedMetrics) },
                            enabled = canSave
                        ) {
                            Text(
                                "Save",
                                color = if (canSave) AthleticOrange else Muted.copy(alpha = 0.4f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Contact Profile Card
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE8ECEF)),
                        shadowElevation = 0.5.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AthleticOrangeLight,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = contact.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = AthleticOrange
                                    )
                                }
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCharcoal
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${contact.relationship.label}${if (contact.contactIdentifier.isNotBlank()) " · ${contact.contactIdentifier}" else ""}",
                                    fontSize = 13.sp,
                                    color = Muted
                                )
                            }
                        }
                    }
                }

                // Health Metrics Selection
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AUTHORIZED METRICS (${selectedMetrics.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Muted,
                            letterSpacing = 1.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = { selectedMetrics = DEFAULT_SHARING_METRICS.map { it.key }.toSet() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("All", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AthleticOrange)
                            }
                            TextButton(
                                onClick = { selectedMetrics = emptySet() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Muted)
                            }
                        }
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE8ECEF)),
                        shadowElevation = 0.5.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            DEFAULT_SHARING_METRICS.forEachIndexed { index, metric ->
                                val isChecked = metric.key in selectedMetrics

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedMetrics = if (isChecked) selectedMetrics - metric.key else selectedMetrics + metric.key
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp)
                                        .semantics {
                                            role = Role.Switch
                                            contentDescription = "${metric.title} toggle, ${if (isChecked) "authorized" else "disabled"}"
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = metric.color.copy(alpha = 0.12f),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = metric.icon,
                                                contentDescription = null,
                                                tint = metric.color,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = metric.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = DeepCharcoal
                                        )
                                        Text(
                                            text = metric.subtitle,
                                            fontSize = 12.sp,
                                            color = Muted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    Switch(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selectedMetrics = if (checked) selectedMetrics + metric.key else selectedMetrics - metric.key
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = AthleticOrange,
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = Color(0xFFE5E7EB),
                                            uncheckedBorderColor = Color.Transparent
                                        )
                                    )
                                }

                                if (index < DEFAULT_SHARING_METRICS.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 56.dp),
                                        color = Color(0xFFF1F3F5),
                                        thickness = 1.dp
                                    )
                                }
                            }
                        }
                    }
                }

                // Privacy Disclosure Card
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFDCFCE7),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Real-time Access Updates",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Changes take effect immediately. ${contact.name} will only be able to view the metrics checked above.",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF15803D),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Sticky Save Button
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .widthIn(max = 640.dp)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = { if (canSave) onSave(selectedMetrics) },
                        enabled = canSave,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AthleticOrange,
                            disabledContainerColor = Color(0xFFE5E7EB),
                            disabledContentColor = Muted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save Permissions", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
