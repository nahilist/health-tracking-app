package com.pulsehealth.app.ui.vitals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.BloodPressureClassification
import com.pulsehealth.app.data.HeartRateClassification
import com.pulsehealth.app.data.MeasurementContext
import com.pulsehealth.app.data.VitalType
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.WarmOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogVitalBottomSheet(
    initialType: VitalType = VitalType.BLOOD_PRESSURE,
    onLogBloodPressure: (systolic: Int, diastolic: Int, bpm: Int?, context: MeasurementContext, notes: String?) -> Unit,
    onLogHeartRate: (bpm: Int, context: MeasurementContext, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedType by remember { mutableStateOf(initialType) }

    // Blood Pressure values
    var systolic by remember { mutableIntStateOf(120) }
    var diastolic by remember { mutableIntStateOf(80) }
    var bpPulseBpm by remember { mutableStateOf<Int?>(72) }

    // Heart Rate value
    var heartRateBpm by remember { mutableIntStateOf(72) }

    // Measurement Context
    var selectedContext by remember { mutableStateOf(MeasurementContext.RESTING) }
    var notesText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val bpClassification = remember(systolic, diastolic) {
        BloodPressureClassification.classify(systolic, diastolic)
    }

    val hrClassification = remember(heartRateBpm) {
        HeartRateClassification.classifyRestingBpm(heartRateBpm)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PulsePink.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selectedType == VitalType.BLOOD_PRESSURE) Icons.Default.MonitorHeart else Icons.Default.Favorite,
                            contentDescription = null,
                            tint = PulsePink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (selectedType == VitalType.BLOOD_PRESSURE) "Log Blood Pressure" else "Log Heart Rate",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.semantics { contentDescription = "Close vital logger" }
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Muted)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Vital Type Selector
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VitalType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        val label = if (type == VitalType.BLOOD_PRESSURE) "Blood Pressure" else "Heart Rate"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    selectedType = type
                                    errorMessage = null
                                }
                                .padding(vertical = 10.dp)
                                .semantics {
                                    role = Role.Tab
                                    contentDescription = "Select $label"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Muted
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (selectedType == VitalType.BLOOD_PRESSURE) {
                // Blood Pressure Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "BLOOD PRESSURE (mmHg)",
                            color = Muted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        // Systolic Stepper
                        VitalStepperRow(
                            label = "Systolic (Top number)",
                            value = systolic,
                            unit = "mmHg",
                            onValueChange = { systolic = it.coerceIn(70, 240) },
                            tint = PulsePink
                        )

                        Spacer(Modifier.height(16.dp))

                        // Diastolic Stepper
                        VitalStepperRow(
                            label = "Diastolic (Bottom number)",
                            value = diastolic,
                            unit = "mmHg",
                            onValueChange = { diastolic = it.coerceIn(40, 150) },
                            tint = WarmOrange
                        )

                        Spacer(Modifier.height(16.dp))

                        // Optional Resting Pulse during BP measurement
                        VitalStepperRow(
                            label = "Resting Pulse (Optional)",
                            value = bpPulseBpm ?: 72,
                            unit = "bpm",
                            onValueChange = { bpPulseBpm = it.coerceIn(40, 200) },
                            tint = PulsePink
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Live Classification Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = bpClassification.color.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, bpClassification.color.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(bpClassification.color)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${bpClassification.label} ($systolic / $diastolic mmHg)",
                                fontWeight = FontWeight.Bold,
                                color = bpClassification.color,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = bpClassification.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                // Heart Rate Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "HEART RATE (BPM)",
                            color = Muted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        VitalStepperRow(
                            label = "Heart Rate",
                            value = heartRateBpm,
                            unit = "bpm",
                            onValueChange = { heartRateBpm = it.coerceIn(30, 220) },
                            tint = PulsePink
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Heart Rate Status Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PulsePink.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, PulsePink.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PulsePink,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "$hrClassification ($heartRateBpm BPM)",
                                fontWeight = FontWeight.Bold,
                                color = PulsePink,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Typical resting heart rates for adults range between 60 and 100 beats per minute.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Measurement Context Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "MEASUREMENT CONTEXT",
                    color = Muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MeasurementContext.entries.forEach { ctx ->
                        val isSelected = selectedContext == ctx
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PulsePink.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) PulsePink.copy(alpha = 0.5f) else Color.Transparent
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedContext = ctx }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Select context ${ctx.label}"
                                }
                        ) {
                            Text(
                                text = ctx.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PulsePink else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Optional Notes
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notes (optional, e.g. after morning coffee)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Measurement notes" },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PulsePink,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                maxLines = 2
            )

            // Error notice
            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (selectedType == VitalType.BLOOD_PRESSURE) {
                        if (systolic <= diastolic) {
                            errorMessage = "Systolic pressure must be greater than diastolic pressure."
                            return@Button
                        }
                        onLogBloodPressure(systolic, diastolic, bpPulseBpm, selectedContext, notesText)
                    } else {
                        onLogHeartRate(heartRateBpm, selectedContext, notesText)
                    }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = if (selectedType == VitalType.BLOOD_PRESSURE) "Save Blood Pressure Record" else "Save Heart Rate Record"
                    },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PulsePink)
            ) {
                Text(
                    text = if (selectedType == VitalType.BLOOD_PRESSURE) "Save Blood Pressure" else "Save Heart Rate",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun VitalStepperRow(
    label: String,
    value: Int,
    unit: String,
    onValueChange: (Int) -> Unit,
    tint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$value",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = tint
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = unit,
                    fontSize = 13.sp,
                    color = Muted,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onValueChange(value - 5) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .semantics { contentDescription = "Decrease $label by 5" }
            ) {
                Text("-5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
            }

            IconButton(
                onClick = { onValueChange(value - 1) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .semantics { contentDescription = "Decrease $label by 1" }
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
            }

            IconButton(
                onClick = { onValueChange(value + 1) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .semantics { contentDescription = "Increase $label by 1" }
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            }

            IconButton(
                onClick = { onValueChange(value + 5) },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .semantics { contentDescription = "Increase $label by 5" }
            ) {
                Text("+5", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
            }
        }
    }
}
