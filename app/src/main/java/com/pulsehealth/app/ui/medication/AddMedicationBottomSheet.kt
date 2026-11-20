package com.pulsehealth.app.ui.medication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.medication.DosageUnit
import com.pulsehealth.app.data.medication.MedicationFrequency

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicationBottomSheet(
    onDismissRequest: () -> Unit,
    onSaveMedication: (
        name: String,
        dosage: Double,
        unit: String,
        frequency: String,
        scheduledTimes: List<String>,
        instructions: String?,
        colorHex: String,
        notes: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var dosageText by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf(DosageUnit.MG) }
    var selectedFrequency by remember { mutableStateOf(MedicationFrequency.DAILY) }
    var instructionsText by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#0D5C58") }
    var notesText by remember { mutableStateOf("") }

    val colorOptions = listOf(
        "#0D5C58", // Deep Teal
        "#F97316", // Warm Orange
        "#2563EB", // Royal Blue
        "#7C3AED", // Violet
        "#DB2777"  // Pink
    )

    val quickInstructions = listOf(
        "Take with food",
        "Before bedtime",
        "On empty stomach",
        "With water"
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier.testTag("add_medication_bottom_sheet"),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 600.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
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
                            .size(36.dp)
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
                            text = "Add Medication",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Prescription or wellness supplement",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.testTag("close_add_medication_sheet")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Medication Name
            Text(
                text = "Medication Name",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("e.g. Vitamin D3, Lisinopril, Omega-3") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("medication_name_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0D5C58),
                    cursorColor = Color(0xFF0D5C58)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dosage and Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(0.45f)) {
                    Text(
                        text = "Dosage",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = dosageText,
                        onValueChange = { dosageText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("medication_dosage_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0D5C58),
                            cursorColor = Color(0xFF0D5C58)
                        )
                    )
                }

                Column(modifier = Modifier.weight(0.55f)) {
                    Text(
                        text = "Unit",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(DosageUnit.MG, DosageUnit.MCG, DosageUnit.ML, DosageUnit.TABLETS, DosageUnit.CAPSULES).forEach { unit ->
                            FilterChip(
                                selected = selectedUnit == unit,
                                onClick = { selectedUnit = unit },
                                label = { Text(unit.displayLabel, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0D5C58).copy(alpha = 0.15f),
                                    selectedLabelColor = Color(0xFF0D5C58)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency
            Text(
                text = "Frequency",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MedicationFrequency.entries.forEach { freq ->
                    FilterChip(
                        selected = selectedFrequency == freq,
                        onClick = { selectedFrequency = freq },
                        label = { Text(freq.displayLabel, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0D5C58).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF0D5C58)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Text(
                text = "Instructions (Optional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                quickInstructions.forEach { inst ->
                    val isSelected = instructionsText == inst
                    FilterChip(
                        selected = isSelected,
                        onClick = { instructionsText = if (isSelected) "" else inst },
                        label = { Text(inst, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0D5C58).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF0D5C58)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Identifier
            Text(
                text = "Tag Color",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                colorOptions.forEach { hex ->
                    val c = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = selectedColorHex == hex
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(c)
                            .clickable { selectedColorHex = hex }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save CTA Button
            val isFormValid = name.isNotBlank() && (dosageText.toDoubleOrNull() ?: 0.0) > 0.0

            Button(
                onClick = {
                    if (isFormValid) {
                        val dosage = dosageText.toDoubleOrNull() ?: 1.0
                        val scheduledTimes = when (selectedFrequency) {
                            MedicationFrequency.DAILY -> listOf("08:00")
                            MedicationFrequency.TWICE_DAILY -> listOf("08:00", "20:00")
                            MedicationFrequency.THREE_TIMES_DAILY -> listOf("08:00", "14:00", "20:00")
                            MedicationFrequency.WEEKLY -> listOf("09:00")
                            MedicationFrequency.AS_NEEDED -> emptyList()
                        }
                        onSaveMedication(
                            name.trim(),
                            dosage,
                            selectedUnit.displayLabel,
                            selectedFrequency.name,
                            scheduledTimes,
                            instructionsText.ifBlank { null },
                            selectedColorHex,
                            notesText.ifBlank { null }
                        )
                        onDismissRequest()
                    }
                },
                enabled = isFormValid,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D5C58)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_medication_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Medication",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
