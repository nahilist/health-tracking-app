package com.pulsehealth.app.ui.sharing

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardUiState
import com.pulsehealth.app.data.ExportedHealthPackage
import com.pulsehealth.app.data.MetricRegistry
import com.pulsehealth.app.data.ShareDuration
import com.pulsehealth.app.data.ShareRelationship
import com.pulsehealth.app.data.ShareStatus
import com.pulsehealth.app.data.TimeRange
import com.pulsehealth.app.data.TrustedContact
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.ExerciseGreen
import com.pulsehealth.app.ui.theme.Muted
import com.pulsehealth.app.ui.theme.PulsePink
import com.pulsehealth.app.ui.theme.WarmOrange
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SharingScreen(
    state: DashboardUiState,
    padding: PaddingValues,
    onAddContact: (String, ShareRelationship, String, Set<String>, ShareDuration) -> Unit,
    onUpdateMetrics: (String, Set<String>) -> Unit,
    onRevokeContact: (String) -> Unit,
    onDeleteContact: (String) -> Unit,
    onExportPackage: suspend (String?, Set<String>, TimeRange) -> ExportedHealthPackage,
    onFormatShareText: (ExportedHealthPackage) -> String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<TrustedContact?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var previewPackage by remember { mutableStateOf<ExportedHealthPackage?>(null) }
    var previewShareText by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = padding.calculateTopPadding() + 18.dp,
            end = 20.dp,
            bottom = padding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header with integrated privacy reassurance
        item {
            Column {
                Text("Sharing", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = ExerciseGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "End-to-end private. Only people you invite can see your records.",
                        color = Muted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PulsePink)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add Contact", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Export Report", fontSize = 13.sp)
                }
            }
        }

        // Trusted Contacts Section Header
        item {
            Text(
                text = "TRUSTED RECIPIENTS (${state.trustedContacts.size})",
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        // Trusted Contacts List
        if (state.trustedContacts.isEmpty()) {
            item {
                HealthCard(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Muted.copy(alpha = 0.12f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Muted, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("No active shares", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "You are not currently sharing your health data with anyone. Tap 'Add Contact' to invite a healthcare provider, caregiver, partner, or coach.",
                            color = Muted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        } else {
            items(state.trustedContacts, key = { it.id }) { contact ->
                ContactCard(
                    contact = contact,
                    onEditMetrics = { editingContact = contact },
                    onRevoke = { onRevokeContact(contact.id) },
                    onDelete = { onDeleteContact(contact.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, rel, contactId, metrics, duration ->
                onAddContact(name, rel, contactId, metrics, duration)
                showAddDialog = false
            }
        )
    }

    editingContact?.let { contact ->
        EditMetricsDialog(
            contact = contact,
            onDismiss = { editingContact = null },
            onSave = { updatedMetrics ->
                onUpdateMetrics(contact.id, updatedMetrics)
                editingContact = null
            }
        )
    }

    if (showExportDialog) {
        ConfigureExportDialog(
            state = state,
            onDismiss = { showExportDialog = false },
            onGenerate = { recipientName, selectedMetrics, range ->
                coroutineScope.launch {
                    val pkg = onExportPackage(recipientName, selectedMetrics, range)
                    val text = onFormatShareText(pkg)
                    previewPackage = pkg
                    previewShareText = text
                    showExportDialog = false
                }
            }
        )
    }

    previewPackage?.let { pkg ->
        previewShareText?.let { text ->
            PreviewShareDialog(
                pkg = pkg,
                shareText = text,
                onDismiss = {
                    previewPackage = null
                    previewShareText = null
                },
                onShare = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, text)
                        putExtra(Intent.EXTRA_TITLE, "Pulse Health Summary")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Wellness Report")
                    context.startActivity(shareIntent)
                    previewPackage = null
                    previewShareText = null
                }
            )
        }
    }
}

@Composable
fun ContactCard(
    contact: TrustedContact,
    onEditMetrics: () -> Unit,
    onRevoke: () -> Unit,
    onDelete: () -> Unit
) {
    val locale = LocalLocale.current.platformLocale
    HealthCard(
        Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Contact ${contact.name}, ${contact.relationship.label}, Status ${contact.status.name}"
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (contact.status) {
                            ShareStatus.ACTIVE -> if (contact.isExpired) WarmOrange.copy(alpha = 0.15f) else ExerciseGreen.copy(alpha = 0.15f)
                            ShareStatus.REVOKED -> MaterialTheme.colorScheme.errorContainer
                            else -> Muted.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = if (contact.isExpired) "EXPIRED" else contact.status.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (contact.status) {
                                ShareStatus.ACTIVE -> if (contact.isExpired) WarmOrange else ExerciseGreen
                                ShareStatus.REVOKED -> MaterialTheme.colorScheme.error
                                else -> Muted
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${contact.relationship.label}${if (contact.contactIdentifier.isNotBlank()) " · ${contact.contactIdentifier}" else ""}",
                    color = Muted,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete contact", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(10.dp))

        Text("Shared metrics:", fontSize = 11.sp, color = Muted, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (contact.sharedMetrics.isEmpty()) {
                Text("No metrics shared", fontSize = 12.sp, color = Muted)
            } else {
                contact.sharedMetrics.forEach { metricKey ->
                    val info = MetricRegistry.getById(metricKey)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = info.color.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = info.title,
                            color = info.color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val durationText = when {
                contact.expiresAtEpochMillis != null -> {
                    val expDate = Instant.ofEpochMilli(contact.expiresAtEpochMillis)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("MMM d", locale))
                    "Access until $expDate"
                }
                else -> "Until revoked"
            }
            Text(durationText, fontSize = 11.sp, color = Muted)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEditMetrics) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp)
                }

                if (contact.isEffectivelyActive) {
                    TextButton(onClick = onRevoke) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Revoke", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, ShareRelationship, String, Set<String>, ShareDuration) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf(ShareRelationship.DOCTOR) }
    var contactId by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf(ShareDuration.ONE_MONTH) }
    var selectedMetrics by remember {
        mutableStateOf(setOf("Steps", "Heart rate", "Sleep", "Water"))
    }
    var expandedDropdown by remember { mutableStateOf(false) }

    val allMetricKeys = listOf("Activity", "Steps", "Active calories", "Exercise", "Stand", "Heart rate", "Sleep", "Water")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Trusted Recipient") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Recipient Name *") },
                    placeholder = { Text("e.g. Dr. Sarah Smith") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = relationship.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Relationship") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        ShareRelationship.entries.forEach { rel ->
                            DropdownMenuItem(
                                text = { Text(rel.label) },
                                onClick = {
                                    relationship = rel
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = contactId,
                    onValueChange = { contactId = it },
                    label = { Text("Email or Contact Info") },
                    placeholder = { Text("e.g. clinic@health.org") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Authorized Metrics:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                allMetricKeys.chunked(2).forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowKeys.forEach { key ->
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedMetrics = if (key in selectedMetrics) {
                                            selectedMetrics - key
                                        } else {
                                            selectedMetrics + key
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = key in selectedMetrics,
                                    onCheckedChange = { isChecked ->
                                        selectedMetrics = if (isChecked) selectedMetrics + key else selectedMetrics - key
                                    }
                                )
                                Text(key, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Text("Sharing Duration:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                ShareDuration.entries.forEach { dur ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { duration = dur },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = duration == dur,
                            onClick = { duration = dur }
                        )
                        Text(dur.label, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedMetrics.isNotEmpty()) {
                        onConfirm(name, relationship, contactId, selectedMetrics, duration)
                    }
                },
                enabled = name.isNotBlank() && selectedMetrics.isNotEmpty()
            ) {
                Text("Authorize & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditMetricsDialog(
    contact: TrustedContact,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit
) {
    var selectedMetrics by remember { mutableStateOf(contact.sharedMetrics) }
    val allMetricKeys = listOf("Activity", "Steps", "Active calories", "Exercise", "Stand", "Heart rate", "Sleep", "Water")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Shared Metrics") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Select which health metrics ${contact.name} is authorized to access:", fontSize = 12.sp, color = Muted)
                Spacer(Modifier.height(4.dp))
                allMetricKeys.forEach { key ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMetrics = if (key in selectedMetrics) selectedMetrics - key else selectedMetrics + key
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = key in selectedMetrics,
                            onCheckedChange = { isChecked ->
                                selectedMetrics = if (isChecked) selectedMetrics + key else selectedMetrics - key
                            }
                        )
                        Text(key, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedMetrics) },
                enabled = selectedMetrics.isNotEmpty()
            ) {
                Text("Save Permissions")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ConfigureExportDialog(
    state: DashboardUiState,
    onDismiss: () -> Unit,
    onGenerate: (String?, Set<String>, TimeRange) -> Unit
) {
    var recipientName by remember { mutableStateOf("") }
    var selectedRange by remember { mutableStateOf(state.selectedRange) }
    var selectedMetrics by remember {
        mutableStateOf(setOf("Steps", "Heart rate", "Sleep", "Water", "Activity"))
    }
    val allMetricKeys = listOf("Activity", "Steps", "Active calories", "Exercise", "Stand", "Heart rate", "Sleep", "Water")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Wellness Report") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    label = { Text("Recipient (Optional)") },
                    placeholder = { Text("e.g. Dr. Smith / Personal Archive") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Time Period:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TimeRange.entries.forEach { range ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedRange = range },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedRange == range) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = range.label,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedRange == range) MaterialTheme.colorScheme.onPrimary else Muted
                            )
                        }
                    }
                }

                Text("Include Metrics:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                allMetricKeys.forEach { key ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMetrics = if (key in selectedMetrics) selectedMetrics - key else selectedMetrics + key
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = key in selectedMetrics,
                            onCheckedChange = { isChecked ->
                                selectedMetrics = if (isChecked) selectedMetrics + key else selectedMetrics - key
                            }
                        )
                        Text(key, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGenerate(recipientName, selectedMetrics, selectedRange) },
                enabled = selectedMetrics.isNotEmpty()
            ) {
                Text("Preview Report")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PreviewShareDialog(
    pkg: ExportedHealthPackage,
    shareText: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Preview") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = shareText,
                        modifier = Modifier.padding(12.dp),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share via Apps")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
