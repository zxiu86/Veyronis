package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Timeline
import com.example.ui.AppSection
import com.example.ui.VeyronisViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: VeyronisViewModel,
    modifier: Modifier = Modifier
) {
    val allTimelines by viewModel.allTimelines.collectAsStateWithLifecycle()
    val allEvents by viewModel.allStoryEvents.collectAsStateWithLifecycle()
    val allWorldRules by viewModel.allWorldRules.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var timelineToEdit by remember { mutableStateOf<Timeline?>(null) }

    // Dilation Calculator State
    var calcInputTime by remember { mutableStateOf("5.0") }
    var calcSelectedTimelineId by remember { mutableStateOf<Long?>(allTimelines.firstOrNull()?.id) }
    var calcDirectionToCosmic by remember { mutableStateOf(true) } // Local -> Cosmic or Cosmic -> Local

    val activeTimeline = allTimelines.find { it.id == calcSelectedTimelineId } ?: allTimelines.firstOrNull()
    val activeRule = allWorldRules.find { it.name.contains("Temporal Distortion", ignoreCase = true) }

    val calculatedOutput = remember(calcInputTime, activeTimeline, activeRule, calcDirectionToCosmic) {
        val input = calcInputTime.toDoubleOrNull() ?: 0.0
        if (activeRule != null && activeRule.localTimeEquivalentSeconds > 0) {
            val ratio = (activeRule.externalTimeEquivalentYears * 365.25 * 86400.0) / activeRule.localTimeEquivalentSeconds
            if (calcDirectionToCosmic) {
                // Input is seconds -> Output is Cosmic Years
                val cosmicYears = (input * ratio) / (365.25 * 86400.0)
                "%.3f Cosmic Years".format(cosmicYears)
            } else {
                // Input is Cosmic Years -> Output is Local Seconds
                val localSeconds = (input * (365.25 * 86400.0)) / ratio
                "%.3f Local Seconds".format(localSeconds)
            }
        } else {
            val multiplier = activeTimeline?.dilationMultiplier ?: 1.0
            if (calcDirectionToCosmic) {
                "%.2f Cosmic Time".format(input / multiplier)
            } else {
                "%.2f Local Time".format(input * multiplier)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VeyronisBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    timelineToEdit = Timeline(name = "")
                    showAddDialog = true
                },
                containerColor = VeyronisSecondary,
                contentColor = VeyronisBackground,
                modifier = Modifier.testTag("add_timeline_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Timeline")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Multi-Timeline Architecture",
                            style = MaterialTheme.typography.headlineSmall,
                            color = VeyronisTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage concurrent, dilated & branched causal streams",
                            style = MaterialTheme.typography.bodySmall,
                            color = VeyronisTextSecondary
                        )
                    }
                }
            }

            // Interactive Dilation Calculator Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("temporal_dilation_calculator_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = VeyronisTertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TEMPORAL DILATION CALCULATOR",
                                style = MaterialTheme.typography.labelMedium,
                                color = VeyronisTertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (activeRule != null)
                                "Governed by active rule: '${activeRule.name}' (${activeRule.localTimeEquivalentSeconds}s local = ${activeRule.externalTimeEquivalentYears} yrs external)"
                            else
                                "Governed by standard dilation multiplier.",
                            style = MaterialTheme.typography.bodySmall,
                            color = VeyronisTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = calcInputTime,
                                onValueChange = { calcInputTime = it },
                                label = { Text(if (calcDirectionToCosmic) "Local Seconds" else "Cosmic Years") },
                                modifier = Modifier.weight(1f).testTag("calc_input_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VeyronisSecondary,
                                    focusedTextColor = VeyronisTextPrimary,
                                    unfocusedTextColor = VeyronisTextPrimary
                                )
                            )

                            IconButton(
                                onClick = { calcDirectionToCosmic = !calcDirectionToCosmic },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "Swap conversion direction", tint = VeyronisPrimary)
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = VeyronisPanelVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (calcDirectionToCosmic) "External Cosmic Time:" else "Internal Local Time:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VeyronisTextMuted
                                    )
                                    Text(
                                        text = calculatedOutput,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = VeyronisTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.testTag("calculated_output_text")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Timelines List
            item {
                Text(
                    text = "Active Timelines (${allTimelines.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = VeyronisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            items(allTimelines) { timeline ->
                val eventsInTimeline = allEvents.filter { it.timelineId == timeline.id }
                TimelineCard(
                    timeline = timeline,
                    eventCount = eventsInTimeline.size,
                    onEdit = {
                        timelineToEdit = timeline
                        showAddDialog = true
                    },
                    onDelete = { viewModel.deleteTimeline(timeline) }
                )
            }

            // Direct button to World Rules
            item {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppSection.WORLD_RULES) },
                    modifier = Modifier.fillMaxWidth().testTag("go_to_world_rules_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VeyronisPrimary)
                ) {
                    Icon(Icons.Default.Rule, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configure World Rules & Fictional Physics")
                }
            }
        }
    }

    // Add / Edit Timeline Dialog
    if (showAddDialog && timelineToEdit != null) {
        val editing = timelineToEdit!!
        var name by remember { mutableStateOf(editing.name) }
        var description by remember { mutableStateOf(editing.description) }
        var isCosmicPrime by remember { mutableStateOf(editing.isCosmicPrime) }
        var offsetStr by remember { mutableStateOf(editing.cosmicTimeOffset.toString()) }
        var dilationStr by remember { mutableStateOf(editing.dilationMultiplier.toString()) }
        var divergenceStr by remember { mutableStateOf(editing.divergencePointCosmic?.toString() ?: "") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = VeyronisPanel,
            title = { Text(if (editing.id == 0L) "Create Timeline" else "Edit Timeline", color = VeyronisTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Timeline Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("timeline_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisSecondary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisSecondary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isCosmicPrime,
                            onCheckedChange = { isCosmicPrime = it },
                            colors = CheckboxDefaults.colors(checkedColor = VeyronisSecondary)
                        )
                        Text("Is Cosmic Prime Coordinate Baseline", color = VeyronisTextPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedTextField(
                        value = offsetStr,
                        onValueChange = { offsetStr = it },
                        label = { Text("Cosmic Time Offset") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisSecondary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = dilationStr,
                        onValueChange = { dilationStr = it },
                        label = { Text("Dilation Multiplier (1.0 = normal)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisSecondary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    if (!isCosmicPrime) {
                        OutlinedTextField(
                            value = divergenceStr,
                            onValueChange = { divergenceStr = it },
                            label = { Text("Branch Divergence Point (Cosmic Year)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisSecondary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val updated = editing.copy(
                                name = name,
                                description = description,
                                isCosmicPrime = isCosmicPrime,
                                cosmicTimeOffset = offsetStr.toDoubleOrNull() ?: 0.0,
                                dilationMultiplier = dilationStr.toDoubleOrNull() ?: 1.0,
                                divergencePointCosmic = divergenceStr.toDoubleOrNull()
                            )
                            viewModel.saveTimeline(updated)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VeyronisSecondary),
                    modifier = Modifier.testTag("save_timeline_button")
                ) {
                    Text("Save", color = VeyronisBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = VeyronisTextSecondary)
                }
            }
        )
    }
}

@Composable
fun TimelineCard(
    timeline: Timeline,
    eventCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(timeline.colorHex))
    } catch (e: Exception) {
        VeyronisSecondary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_card_${timeline.name.lowercase().replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color, shape = RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timeline.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = VeyronisTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = if (timeline.isCosmicPrime) VeyronisPrimaryContainer else VeyronisPanelVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (timeline.isCosmicPrime) "COSMIC PRIME" else "BRANCHED",
                        color = if (timeline.isCosmicPrime) VeyronisPrimary else VeyronisTextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (timeline.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = timeline.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = VeyronisTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = VeyronisSurfaceHighlight)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dilation: ${timeline.dilationMultiplier}x • Offset: ${timeline.cosmicTimeOffset} • $eventCount events",
                    style = MaterialTheme.typography.labelSmall,
                    color = VeyronisTertiary
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = VeyronisTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    if (!timeline.isCosmicPrime) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = VeyronisWarning, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
