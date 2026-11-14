package com.pulsehealth.app.ui.browse

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsehealth.app.data.DashboardMetrics
import com.pulsehealth.app.data.HealthCategory
import com.pulsehealth.app.data.MetricRegistry
import com.pulsehealth.app.data.MetricTypeInfo
import com.pulsehealth.app.ui.components.HealthCard
import com.pulsehealth.app.ui.theme.Muted

@Composable
fun BrowseScreen(
    padding: PaddingValues,
    metrics: DashboardMetrics = DashboardMetrics(),
    onOpen: (String) -> Unit
) {
    val locale = LocalLocale.current.platformLocale
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<HealthCategory?>(null) }

    val filteredMetrics = remember(searchQuery, selectedCategory) {
        val base = MetricRegistry.search(searchQuery)
        if (selectedCategory != null) {
            base.filter { it.category == selectedCategory }
        } else {
            base
        }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = padding.calculateTopPadding() + 18.dp,
            end = 20.dp,
            bottom = padding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Browse", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Health categories & measurements", color = Muted, fontSize = 13.sp)
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Search health metrics" },
                placeholder = { Text("Search metrics, vitals, habits...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Clear search", tint = Muted, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isAll = selectedCategory == null
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = if (isAll) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedCategory = null }
                ) {
                    Text(
                        "All",
                        color = if (isAll) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
                HealthCategory.entries.forEach { cat ->
                    val isCatSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCatSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = if (isCatSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCategory = if (isCatSelected) null else cat }
                    ) {
                        Text(
                            cat.label,
                            color = if (isCatSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        if (searchQuery.isNotBlank() || selectedCategory != null) {
            if (filteredMetrics.isEmpty()) {
                item {
                    EmptySearchResults(searchQuery.ifBlank { selectedCategory?.label ?: "" })
                }
            } else {
                item {
                    Text(
                        text = if (searchQuery.isNotBlank()) "SEARCH RESULTS (${filteredMetrics.size})" else "${selectedCategory?.label?.uppercase(locale)} (${filteredMetrics.size})",
                        color = Muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                item {
                    HealthCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        filteredMetrics.forEachIndexed { index, metric ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(start = 68.dp)
                                )
                            }
                            MetricRowItem(
                                metric = metric,
                                valueText = MetricRegistry.formatValue(metric.id, metrics, locale),
                                onClick = { onOpen(metric.id) }
                            )
                        }
                    }
                }
            }
        } else {
            HealthCategory.entries.forEach { category ->
                val categoryMetrics = MetricRegistry.allMetrics.filter { it.category == category }
                if (categoryMetrics.isNotEmpty()) {
                    item {
                        Text(
                            text = category.label.uppercase(locale),
                            color = Muted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                        )
                    }
                    item {
                        HealthCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            categoryMetrics.forEachIndexed { index, metric ->
                                if (index > 0) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(start = 68.dp)
                                    )
                                }
                                MetricRowItem(
                                    metric = metric,
                                    valueText = MetricRegistry.formatValue(metric.id, metrics, locale),
                                    onClick = { onOpen(metric.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricRowItem(
    metric: MetricTypeInfo,
    valueText: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp)
            .semantics { contentDescription = "${metric.title}: $valueText" }
    ) {
        Surface(
            shape = CircleShape,
            color = metric.color.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = metric.icon,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = metric.color
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = metric.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Text(
                text = metric.subtitle,
                color = Muted,
                fontSize = 11.sp
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = valueText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (valueText != "No data" && valueText != "—" && valueText != "No activity") {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    Muted
                }
            )
        }

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Muted.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun MetricListItem(
    metric: MetricTypeInfo,
    valueText: String,
    onClick: () -> Unit
) {
    MetricRowItem(metric = metric, valueText = valueText, onClick = onClick)
}

@Composable
fun EmptySearchResults(query: String) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Muted.copy(alpha = 0.12f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "No health metrics found",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "No results matching \"$query\". Try searching for steps, sleep, vitals, or water.",
                color = Muted,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
