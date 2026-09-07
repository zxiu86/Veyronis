package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.WorldRule
import com.example.domain.WarningSeverity
import com.example.ui.VeyronisViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldRulesScreen(
    viewModel: VeyronisViewModel,
    modifier: Modifier = Modifier
) {
    val allWorldRules by viewModel.allWorldRules.collectAsStateWithLifecycle()
    val warnings by viewModel.temporalWarnings.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: World Rules, 1: Continuity Engine
    var showEditRuleDialog by remember { mutableStateOf(false) }
    var ruleToEdit by remember { mutableStateOf<WorldRule?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VeyronisBackground,
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        ruleToEdit = WorldRule(name = "")
                        showEditRuleDialog = true
                    },
                    containerColor = VeyronisPrimary,
                    contentColor = VeyronisTextPrimary,
                    modifier = Modifier.testTag("add_world_rule_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Rule")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Universe Laws & Consistency",
                style = MaterialTheme.typography.headlineSmall,
                color = VeyronisTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Define fictional physics and detect timeline contradictions automatically",
                style = MaterialTheme.typography.bodySmall,
                color = VeyronisTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = VeyronisPanel,
                contentColor = VeyronisPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("World Rules (${allWorldRules.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Continuity Engine")
                            if (warnings.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = VeyronisWarning,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = warnings.size.toString(),
                                        color = Color.Black,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // World Rules Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allWorldRules) { rule ->
                        WorldRuleCard(
                            rule = rule,
                            onEdit = {
                                ruleToEdit = rule
                                showEditRuleDialog = true
                            },
                            onDelete = { viewModel.deleteWorldRule(rule) }
                        )
                    }
                }
            } else {
                // Consistency Warnings Tab
                if (warnings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = VeyronisSuccess,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Zero Temporal Paradoxes Found",
                                style = MaterialTheme.typography.titleMedium,
                                color = VeyronisTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Every event sequence, character appearance, and timeline dilation obeys universal causality laws.",
                                style = MaterialTheme.typography.bodySmall,
                                color = VeyronisTextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(warnings) { warning ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("warning_card_${warning.id}")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = when (warning.severity) {
                                                WarningSeverity.CRITICAL -> VeyronisWarning
                                                WarningSeverity.WARNING -> VeyronisTertiary
                                                WarningSeverity.ADVISORY -> VeyronisSecondary
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = warning.severity.name,
                                                color = Color.Black,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        if (warning.cosmicTimestamp != null) {
                                            Text(
                                                text = "Cosmic: ${warning.cosmicTimestamp}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = VeyronisTextMuted
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = warning.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = VeyronisTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = warning.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VeyronisTextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = VeyronisSurfaceHighlight)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = VeyronisSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Fix: ${warning.recommendation}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = VeyronisSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add / Edit World Rule
    if (showEditRuleDialog && ruleToEdit != null) {
        val editing = ruleToEdit!!
        var name by remember { mutableStateOf(editing.name) }
        var category by remember { mutableStateOf(editing.category) }
        var description by remember { mutableStateOf(editing.description) }
        var localTimeStr by remember { mutableStateOf(editing.localTimeEquivalentSeconds.toString()) }
        var externalTimeStr by remember { mutableStateOf(editing.externalTimeEquivalentYears.toString()) }
        var efficiencyStr by remember { mutableStateOf(editing.efficiencyPercent.toString()) }
        var energyType by remember { mutableStateOf(editing.energyType) }
        var enablesTimeTravel by remember { mutableStateOf(editing.enablesTimeTravel) }

        AlertDialog(
            onDismissRequest = { showEditRuleDialog = false },
            containerColor = VeyronisPanel,
            title = { Text(if (editing.id == 0L) "Define World Rule" else "Edit World Rule", color = VeyronisTextPrimary) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Rule Name (e.g. Temporal Distortion) *") },
                        modifier = Modifier.fillMaxWidth().testTag("world_rule_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (Physics, Temporal, Magic, Law)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Scientific / Lore Description") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = localTimeStr,
                            onValueChange = { localTimeStr = it },
                            label = { Text("Local Seconds") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                        )
                        OutlinedTextField(
                            value = externalTimeStr,
                            onValueChange = { externalTimeStr = it },
                            label = { Text("External Years") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = efficiencyStr,
                            onValueChange = { efficiencyStr = it },
                            label = { Text("Efficiency %") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                        )
                        OutlinedTextField(
                            value = energyType,
                            onValueChange = { energyType = it },
                            label = { Text("Energy Medium") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = enablesTimeTravel,
                            onCheckedChange = { enablesTimeTravel = it },
                            colors = CheckboxDefaults.colors(checkedColor = VeyronisSecondary)
                        )
                        Text("Enables Closed-Timelike Loop / Travel", color = VeyronisTextPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val updated = editing.copy(
                                name = name,
                                category = category.ifBlank { "Physics" },
                                description = description,
                                localTimeEquivalentSeconds = localTimeStr.toDoubleOrNull() ?: 5.0,
                                externalTimeEquivalentYears = externalTimeStr.toDoubleOrNull() ?: 13.0,
                                efficiencyPercent = efficiencyStr.toDoubleOrNull() ?: 100.0,
                                energyType = energyType.ifBlank { "Cosmic Energy" },
                                enablesTimeTravel = enablesTimeTravel
                            )
                            viewModel.saveWorldRule(updated)
                            showEditRuleDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
                    modifier = Modifier.testTag("save_world_rule_button")
                ) {
                    Text("Save Rule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditRuleDialog = false }) {
                    Text("Cancel", color = VeyronisTextSecondary)
                }
            }
        )
    }
}

@Composable
fun WorldRuleCard(
    rule: WorldRule,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("world_rule_card_${rule.name.lowercase().replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = VeyronisPrimaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = rule.category.uppercase(),
                        color = VeyronisPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = VeyronisTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = VeyronisWarning, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = rule.name, style = MaterialTheme.typography.titleMedium, color = VeyronisTextPrimary, fontWeight = FontWeight.Bold)

            if (rule.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = rule.description, style = MaterialTheme.typography.bodySmall, color = VeyronisTextSecondary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = VeyronisSurfaceHighlight)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dilation: ${rule.localTimeEquivalentSeconds}s local = ${rule.externalTimeEquivalentYears} yrs external",
                    style = MaterialTheme.typography.labelSmall,
                    color = VeyronisTertiary
                )
                Text(
                    text = "Efficiency: ${rule.efficiencyPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = VeyronisSecondary
                )
            }
        }
    }
}
