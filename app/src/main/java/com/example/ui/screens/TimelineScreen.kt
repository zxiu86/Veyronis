package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.AppLanguage
import com.example.ui.AppSection
import com.example.ui.Strings
import com.example.ui.VeyronisViewModel
import com.example.ui.components.LuxuryDialog
import com.example.ui.components.LuxuryGradientButton
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
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var timelineToEdit by remember { mutableStateOf<Timeline?>(null) }

    // Dilation Calculator State
    var calcInputTime by remember { mutableStateOf("5.0") }
    var calcSelectedTimelineId by remember { mutableStateOf<Long?>(allTimelines.firstOrNull()?.id) }
    var calcDirectionToCosmic by remember { mutableStateOf(true) } // Local -> Cosmic or Cosmic -> Local

    val activeTimeline = allTimelines.find { it.id == calcSelectedTimelineId } ?: allTimelines.firstOrNull()
    val activeRule = allWorldRules.find { it.name.contains("Temporal Distortion", ignoreCase = true) }

    val calculatedOutput = remember(calcInputTime, activeTimeline, activeRule, calcDirectionToCosmic, language) {
        val input = calcInputTime.toDoubleOrNull() ?: 0.0
        if (activeRule != null && activeRule.localTimeEquivalentSeconds > 0) {
            val ratio = (activeRule.externalTimeEquivalentYears * 365.25 * 86400.0) / activeRule.localTimeEquivalentSeconds
            if (calcDirectionToCosmic) {
                // Input is seconds -> Output is Cosmic Years
                val cosmicYears = (input * ratio) / (365.25 * 86400.0)
                "%.3f %s".format(cosmicYears, if (language == AppLanguage.ARABIC) "سنة كونية" else "Cosmic Years")
            } else {
                // Input is Cosmic Years -> Output is Local Seconds
                val localSeconds = (input * (365.25 * 86400.0)) / ratio
                "%.3f %s".format(localSeconds, if (language == AppLanguage.ARABIC) "ثانية محلية" else "Local Seconds")
            }
        } else {
            val multiplier = activeTimeline?.dilationMultiplier ?: 1.0
            if (calcDirectionToCosmic) {
                "%.2f %s".format(input / multiplier, if (language == AppLanguage.ARABIC) "وقت كوني" else "Cosmic Time")
            } else {
                "%.2f %s".format(input * multiplier, if (language == AppLanguage.ARABIC) "وقت محلي" else "Local Time")
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
                Icon(Icons.Default.Add, contentDescription = Strings.get("timeline_add", language))
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
                            text = Strings.get("timeline_title", language),
                            style = MaterialTheme.typography.headlineSmall,
                            color = VeyronisTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (language == AppLanguage.ARABIC) "إدارة المسارات السببية المتزامنة والمتمددة والمتفرعة" else "Manage concurrent, dilated & branched causal streams",
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
                                text = Strings.get("timeline_calculator", language),
                                style = MaterialTheme.typography.labelMedium,
                                color = VeyronisTertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (activeRule != null)
                                "${if (language == AppLanguage.ARABIC) "محكوم بالقاعدة النشطة" else "Governed by active rule"}: '${activeRule.name}' (${activeRule.localTimeEquivalentSeconds}s local = ${activeRule.externalTimeEquivalentYears} yrs external)"
                            else
                                if (language == AppLanguage.ARABIC) "محكوم بمعامل التمدد القياسي." else "Governed by standard dilation multiplier.",
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
                                label = { Text(if (calcDirectionToCosmic) (if (language == AppLanguage.ARABIC) "ثواني محلية" else "Local Seconds") else (if (language == AppLanguage.ARABIC) "سنوات كونية" else "Cosmic Years")) },
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
                                        text = if (calcDirectionToCosmic) (if (language == AppLanguage.ARABIC) "الوقت الكوني الخارجي:" else "External Cosmic Time:") else (if (language == AppLanguage.ARABIC) "الوقت المحلي الداخلي:" else "Internal Local Time:"),
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
                    text = "${Strings.get("timeline_count", language)} (${allTimelines.size})",
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
                    language = language,
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
                    Text(Strings.get("worldrules_title", language))
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

        LuxuryDialog(
            onDismissRequest = { showAddDialog = false },
            title = if (editing.id == 0L) Strings.get("timeline_add", language) else Strings.get("timeline_edit", language),
            subtitle = if (editing.id == 0L) (if (language == AppLanguage.ARABIC) "تكوين خط زمني جديد للملحمة" else "Configure new timeline dimension") else editing.name,
            icon = Icons.Default.HourglassBottom,
            iconColor = LuxuryAuroraViolet,
            actionButtons = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(Strings.get("cancel", language), color = LuxuryTextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                LuxuryGradientButton(
                    text = Strings.get("save", language),
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
                    brush = LuxuryCosmicGradient,
                    textColor = Color.White,
                    modifier = Modifier.testTag("save_timeline_button")
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Strings.get("timeline_name", language)) },
                    modifier = Modifier.fillMaxWidth().testTag("timeline_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryAuroraViolet,
                        focusedTextColor = LuxuryTextPrimary,
                        unfocusedTextColor = LuxuryTextPrimary,
                        unfocusedContainerColor = LuxurySurfaceElevated,
                        focusedContainerColor = LuxurySurfaceElevated
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (language == AppLanguage.ARABIC) "الوصف والملاحظات" else "Description & Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryAuroraViolet,
                        focusedTextColor = LuxuryTextPrimary,
                        unfocusedTextColor = LuxuryTextPrimary,
                        unfocusedContainerColor = LuxurySurfaceElevated,
                        focusedContainerColor = LuxurySurfaceElevated
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LuxurySurfaceElevated, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isCosmicPrime,
                        onCheckedChange = { isCosmicPrime = it },
                        colors = CheckboxDefaults.colors(checkedColor = LuxuryAuroraViolet)
                    )
                    Text(
                        text = if (language == AppLanguage.ARABIC) "الخط الزمني المرجعي الكوني الأساسي" else "Is Cosmic Prime Coordinate Baseline",
                        color = LuxuryTextPrimary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OutlinedTextField(
                    value = offsetStr,
                    onValueChange = { offsetStr = it },
                    label = { Text(Strings.get("timeline_offset", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryAuroraViolet,
                        focusedTextColor = LuxuryTextPrimary,
                        unfocusedTextColor = LuxuryTextPrimary,
                        unfocusedContainerColor = LuxurySurfaceElevated,
                        focusedContainerColor = LuxurySurfaceElevated
                    )
                )
                OutlinedTextField(
                    value = dilationStr,
                    onValueChange = { dilationStr = it },
                    label = { Text(Strings.get("timeline_dilation", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryAuroraViolet,
                        focusedTextColor = LuxuryTextPrimary,
                        unfocusedTextColor = LuxuryTextPrimary,
                        unfocusedContainerColor = LuxurySurfaceElevated,
                        focusedContainerColor = LuxurySurfaceElevated
                    )
                )
                if (!isCosmicPrime) {
                    OutlinedTextField(
                        value = divergenceStr,
                        onValueChange = { divergenceStr = it },
                        label = { Text(if (language == AppLanguage.ARABIC) "نقطة انشعاب الخط (السنة الكونية)" else "Branch Divergence Point (Cosmic Year)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryAuroraViolet,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineCard(
    timeline: Timeline,
    eventCount: Int,
    language: AppLanguage,
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
                        text = if (timeline.isCosmicPrime) (if (language == AppLanguage.ARABIC) "الرئيسي الكوني" else "COSMIC PRIME") else (if (language == AppLanguage.ARABIC) "متفرع" else "BRANCHED"),
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
                    text = "${Strings.get("timeline_dilation", language)}: ${timeline.dilationMultiplier}x • ${Strings.get("timeline_offset", language)}: ${timeline.cosmicTimeOffset} • $eventCount ${Strings.get("events_count", language)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = VeyronisTertiary
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = Strings.get("edit", language), tint = VeyronisTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    if (!timeline.isCosmicPrime) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = Strings.get("delete", language), tint = VeyronisWarning, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
