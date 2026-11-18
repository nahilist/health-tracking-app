package com.pulsehealth.app.ui.medication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.medication.DoseStatus
import com.pulsehealth.app.data.medication.MedicationAdherenceSummary
import com.pulsehealth.app.data.medication.ScheduledDoseItem
import java.util.Locale

@Composable
fun MedicationScheduleCard(
    schedule: List<ScheduledDoseItem>,
    adherence: MedicationAdherenceSummary,
    modifier: Modifier = Modifier,
    onTakeDose: (medicationId: String, scheduledTimeEpochMillis: Long) -> Unit,
    onSkipDose: (medicationId: String, scheduledTimeEpochMillis: Long) -> Unit,
    onUndoDose: (medicationId: String, scheduledTimeEpochMillis: Long) -> Unit,
    onAddMedicationClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("medication_schedule_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0D5C58).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = Color(0xFF0D5C58),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Daily Medications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val streakText = if (adherence.currentStreakDays > 0) {
                            " · ${adherence.currentStreakDays}d streak 🔥"
                        } else ""
                        Text(
                            text = "${adherence.adherencePercentage}% adherence$streakText",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF0D5C58),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = onAddMedicationClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .testTag("add_medication_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add medication or supplement",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (schedule.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "No medications scheduled for today",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Track your daily vitamins, supplements, and prescriptions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onAddMedicationClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0D5C58)
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add Medication",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // List of scheduled doses
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    schedule.forEach { dose ->
                        ScheduledDoseRow(
                            dose = dose,
                            onTake = { onTakeDose(dose.medication.id, dose.scheduledTimeEpochMillis) },
                            onSkip = { onSkipDose(dose.medication.id, dose.scheduledTimeEpochMillis) },
                            onUndo = { onUndoDose(dose.medication.id, dose.scheduledTimeEpochMillis) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduledDoseRow(
    dose: ScheduledDoseItem,
    onTake: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val med = dose.medication
    val medColor = try {
        Color(android.graphics.Color.parseColor(med.colorHex))
    } catch (_: Exception) {
        Color(0xFF0D5C58)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dose_item_${med.id}"),
        shape = RoundedCornerShape(14.dp),
        color = when (dose.status) {
            DoseStatus.TAKEN -> Color(0xFFE6F4EA).copy(alpha = 0.7f)
            DoseStatus.SKIPPED -> Color(0xFFFCE8E6).copy(alpha = 0.7f)
            DoseStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(
            1.dp,
            when (dose.status) {
                DoseStatus.TAKEN -> Color(0xFF34A853).copy(alpha = 0.4f)
                DoseStatus.SKIPPED -> Color(0xFFEA4335).copy(alpha = 0.3f)
                DoseStatus.PENDING -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Color pill + Name + Dosage + Instructions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(medColor)
                )

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = med.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${dose.timeLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    val dosageText = if (med.dosage % 1.0 == 0.0) {
                        "${med.dosage.toInt()} ${med.unit}"
                    } else {
                        "${med.dosage} ${med.unit}"
                    }

                    val instructionsSuffix = if (!med.instructions.isNullOrBlank()) {
                        " · ${med.instructions}"
                    } else ""

                    Text(
                        text = "$dosageText$instructionsSuffix",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: Status button / badge
            when (dose.status) {
                DoseStatus.PENDING -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("skip_dose_${med.id}"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Skip",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onTake,
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("take_dose_${med.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0D5C58)
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Take",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                DoseStatus.TAKEN -> {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF34A853).copy(alpha = 0.15f))
                            .clickable(onClick = onUndo)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("taken_badge_${med.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Taken. Tap to undo",
                            tint = Color(0xFF137333),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Taken",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF137333)
                        )
                    }
                }
                DoseStatus.SKIPPED -> {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEA4335).copy(alpha = 0.15f))
                            .clickable(onClick = onUndo)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("skipped_badge_${med.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Skipped. Tap to undo",
                            tint = Color(0xFFC5221F),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Skipped",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC5221F)
                        )
                    }
                }
            }
        }
    }
}
