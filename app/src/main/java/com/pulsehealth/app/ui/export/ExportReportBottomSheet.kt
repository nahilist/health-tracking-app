package com.pulsehealth.app.ui.export

import android.net.Uri
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.export.ReportFormat
import com.pulsehealth.app.data.export.ReportSectionConfig
import com.pulsehealth.app.data.export.ReportTimeRange
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportBottomSheet(
    onDismiss: () -> Unit,
    onGenerateAndShare: suspend (format: ReportFormat, range: ReportTimeRange, config: ReportSectionConfig, doctorNotes: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var selectedFormat by remember { mutableStateOf(ReportFormat.PDF) }
    var selectedRange by remember { mutableStateOf(ReportTimeRange.LAST_30_DAYS) }
    var includeVitals by remember { mutableStateOf(true) }
    var includeWorkouts by remember { mutableStateOf(true) }
    var includeSleep by remember { mutableStateOf(true) }
    var includeHydration by remember { mutableStateOf(true) }
    var includeInsights by remember { mutableStateOf(true) }
    var doctorNotes by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 600.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(PulsePink.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selectedFormat == ReportFormat.PDF) Icons.Default.PictureAsPdf else Icons.Default.TableChart,
                            contentDescription = null,
                            tint = PulsePink,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Export Health Report",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Doctor-ready local clinical summary",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close export sheet", tint = Muted)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Format Selection (PDF vs CSV)
            Text(
                text = "REPORT FORMAT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Muted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReportFormat.entries.forEach { format ->
                    val isSelected = selectedFormat == format
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PulsePink.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (isSelected) PulsePink else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedFormat = format }
                            .semantics {
                                role = Role.RadioButton
                                contentDescription = "${format.displayLabel}, ${if (isSelected) "selected" else "not selected"}"
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (format == ReportFormat.PDF) Icons.Default.PictureAsPdf else Icons.Default.TableChart,
                                contentDescription = null,
                                tint = if (isSelected) PulsePink else Muted,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (format == ReportFormat.PDF) "Clinical PDF" else "CSV Bundle",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PulsePink else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Reporting Period
            Text(
                text = "REPORTING PERIOD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Muted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportTimeRange.entries.forEach { range ->
                    val isSelected = selectedRange == range
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedRange = range },
                        label = {
                            Text(
                                text = when (range) {
                                    ReportTimeRange.LAST_7_DAYS -> "7 Days"
                                    ReportTimeRange.LAST_30_DAYS -> "30 Days"
                                    ReportTimeRange.LAST_90_DAYS -> "90 Days"
                                    ReportTimeRange.ALL_TIME -> "All"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PulsePink.copy(alpha = 0.12f),
                            selectedLabelColor = PulsePink
                        ),
                        modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Sections to Include
            Text(
                text = "SECTIONS TO INCLUDE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Muted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionToggleRow("Vitals & Blood Pressure (AHA Ranges)", includeVitals) { includeVitals = it }
                SectionToggleRow("Physical Activity & Workout Sessions", includeWorkouts) { includeWorkouts = it }
                SectionToggleRow("Sleep Architecture & Restfulness", includeSleep) { includeSleep = it }
                SectionToggleRow("Hydration Volume & Habits", includeHydration) { includeHydration = it }
                SectionToggleRow("Statistical Trends & Anomaly Insights", includeInsights) { includeInsights = it }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Consultation Notes Field
            Text(
                text = "DOCTOR / CONSULTATION NOTES (OPTIONAL)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Muted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = doctorNotes,
                onValueChange = { doctorNotes = it },
                placeholder = { Text("e.g., Routine cardiologist follow-up, reviewing recent blood pressure averages", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PulsePink,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Generate & Share CTA
            Button(
                onClick = {
                    if (!isGenerating) {
                        isGenerating = true
                        coroutineScope.launch {
                            try {
                                val config = ReportSectionConfig(
                                    includeVitals = includeVitals,
                                    includeWorkouts = includeWorkouts,
                                    includeSleep = includeSleep,
                                    includeHydration = includeHydration,
                                    includeInsights = includeInsights
                                )
                                onGenerateAndShare(selectedFormat, selectedRange, config, doctorNotes.ifBlank { null })
                                onDismiss()
                            } finally {
                                isGenerating = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PulsePink),
                enabled = !isGenerating && (includeVitals || includeWorkouts || includeSleep || includeHydration || includeInsights)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Generating Report...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedFormat == ReportFormat.PDF) "Generate & Share PDF Report" else "Generate & Share CSV Bundle",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (checked) FontWeight.Medium else FontWeight.Normal
        )
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(
                    if (checked) PulsePink else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}
