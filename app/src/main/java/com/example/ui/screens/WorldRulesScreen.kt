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
import com.example.ui.AppLanguage
import com.example.ui.Strings
import com.example.ui.VeyronisViewModel
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.components.LuxuryGradientButton
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldRulesScreen(
    viewModel: VeyronisViewModel,
    modifier: Modifier = Modifier
) {
    val allWorldRules by viewModel.allWorldRules.collectAsStateWithLifecycle()
    val warnings by viewModel.temporalWarnings.collectAsStateWithLifecycle()
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: World Rules, 1: Continuity Engine
    var showEditRuleDialog by remember { mutableStateOf(false) }
    var ruleToEdit by remember { mutableStateOf<WorldRule?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        ruleToEdit = WorldRule(name = "")
                        showEditRuleDialog = true
                    },
                    containerColor = LuxuryElectricCyan,
                    contentColor = LuxuryVoidBackground,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_world_rule_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = Strings.get("worldrules_add", language))
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
                text = Strings.get("worldrules_title", language),
                style = MaterialTheme.typography.headlineSmall,
                color = LuxuryTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (language == AppLanguage.ARABIC) "تحديد قوانين فيزياء العالم وكشف تناقضات الخطوط الزمنية تلقائياً" else "Define fictional physics and detect timeline contradictions automatically",
                style = MaterialTheme.typography.bodySmall,
                color = LuxuryTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = LuxurySurface,
                contentColor = LuxuryElectricCyan,
                divider = { HorizontalDivider(color = LuxurySurfaceHighlight) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "${Strings.get("worldrules_tab_rules", language)} (${allWorldRules.size})",
                            color = if (selectedTab == 0) LuxuryElectricCyan else LuxuryTextSecondary,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = Strings.get("worldrules_tab_continuity", language),
                                color = if (selectedTab == 1) LuxuryElectricCyan else LuxuryTextSecondary,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                            if (warnings.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = LuxuryWarning,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "${warnings.size}",
                                        color = LuxuryVoidBackground,
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
                // Tab 0: World Rules List
                if (allWorldRules.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = Strings.get("no_worldrules_prompt", language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LuxuryTextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allWorldRules) { rule ->
                            LuxuryWorldRuleCard(
                                rule = rule,
                                language = language,
                                onEdit = {
                                    ruleToEdit = rule
                                    showEditRuleDialog = true
                                },
                                onDelete = { viewModel.deleteWorldRule(rule) }
                            )
                        }
                    }
                }
            } else {
                // Tab 1: Temporal Continuity Engine Status
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        LuxuryGlassCard(
                            glowColor = if (warnings.isNotEmpty()) LuxuryWarning else LuxurySuccess,
                            modifier = Modifier.fillMaxWidth().testTag("temporal_engine_card")
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = (if (warnings.isNotEmpty()) LuxuryWarning else LuxurySuccess).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (warnings.isNotEmpty()) Icons.Default.Warning else Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = if (warnings.isNotEmpty()) LuxuryWarning else LuxurySuccess,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = if (warnings.isNotEmpty())
                                                (if (language == AppLanguage.ARABIC) "تم اكتشاف تناقضات زمنية" else "Temporal Paradoxes Detected")
                                            else
                                                (if (language == AppLanguage.ARABIC) "جميع الخطوط الزمنية متسقة بالكامل" else "Timeline Fully Consistent"),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (warnings.isNotEmpty()) LuxuryWarning else LuxurySuccess,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${warnings.size} ${Strings.get("warnings_count", language)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = LuxuryTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(warnings) { warning ->
                        LuxuryGlassCard(
                            glowColor = if (warning.severity == WarningSeverity.CRITICAL) LuxuryWarning else LuxuryAuroraViolet,
                            modifier = Modifier.fillMaxWidth().testTag("warning_item_${warning.severity.name.lowercase()}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (warning.severity == WarningSeverity.CRITICAL) LuxuryWarning.copy(alpha = 0.2f) else LuxuryAuroraViolet.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = warning.severity.name,
                                            color = if (warning.severity == WarningSeverity.CRITICAL) LuxuryWarning else LuxuryAuroraViolet,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = warning.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LuxuryTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = warning.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LuxuryTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Edit / Create World Rule
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
            containerColor = LuxurySurface,
            title = {
                Text(
                    text = if (editing.id == 0L) Strings.get("worldrules_add", language) else (if (language == AppLanguage.ARABIC) "تعديل قانون" else "Edit Rule"),
                    color = LuxuryTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
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
                        label = { Text(Strings.get("worldrules_name", language) + " *") },
                        modifier = Modifier.fillMaxWidth().testTag("world_rule_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryElectricCyan,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        )
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(Strings.get("worldrules_category", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryElectricCyan,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        )
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(if (language == AppLanguage.ARABIC) "الوصف العلمي والقصصي" else "Scientific / Lore Description") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryElectricCyan,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        )
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = localTimeStr,
                            onValueChange = { localTimeStr = it },
                            label = { Text(if (language == AppLanguage.ARABIC) "الثواني المحلية" else "Local Seconds") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryElectricCyan,
                                focusedTextColor = LuxuryTextPrimary,
                                unfocusedTextColor = LuxuryTextPrimary,
                                unfocusedContainerColor = LuxurySurfaceElevated,
                                focusedContainerColor = LuxurySurfaceElevated
                            )
                        )
                        OutlinedTextField(
                            value = externalTimeStr,
                            onValueChange = { externalTimeStr = it },
                            label = { Text(if (language == AppLanguage.ARABIC) "السنوات الخارجية" else "External Years") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryElectricCyan,
                                focusedTextColor = LuxuryTextPrimary,
                                unfocusedTextColor = LuxuryTextPrimary,
                                unfocusedContainerColor = LuxurySurfaceElevated,
                                focusedContainerColor = LuxurySurfaceElevated
                            )
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = efficiencyStr,
                            onValueChange = { efficiencyStr = it },
                            label = { Text(if (language == AppLanguage.ARABIC) "الكفاءة %" else "Efficiency %") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryElectricCyan,
                                focusedTextColor = LuxuryTextPrimary,
                                unfocusedTextColor = LuxuryTextPrimary,
                                unfocusedContainerColor = LuxurySurfaceElevated,
                                focusedContainerColor = LuxurySurfaceElevated
                            )
                        )
                        OutlinedTextField(
                            value = energyType,
                            onValueChange = { energyType = it },
                            label = { Text(if (language == AppLanguage.ARABIC) "نوع الطاقة" else "Energy Medium") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryElectricCyan,
                                focusedTextColor = LuxuryTextPrimary,
                                unfocusedTextColor = LuxuryTextPrimary,
                                unfocusedContainerColor = LuxurySurfaceElevated,
                                focusedContainerColor = LuxurySurfaceElevated
                            )
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = enablesTimeTravel,
                            onCheckedChange = { enablesTimeTravel = it },
                            colors = CheckboxDefaults.colors(checkedColor = LuxuryElectricCyan)
                        )
                        Text(
                            text = if (language == AppLanguage.ARABIC) "يتيح حلقة / سفر عبر الزمن مغلق" else "Enables Closed-Timelike Loop / Travel",
                            color = LuxuryTextPrimary,
                            style = MaterialTheme.typography.bodySmall
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
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryElectricCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("save_world_rule_button")
                ) {
                    Text(Strings.get("save", language), color = LuxuryVoidBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditRuleDialog = false }) {
                    Text(Strings.get("cancel", language), color = LuxuryTextSecondary)
                }
            }
        )
    }
}

@Composable
fun LuxuryWorldRuleCard(
    rule: WorldRule,
    language: AppLanguage,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    LuxuryGlassCard(
        glowColor = LuxuryElectricCyan,
        modifier = Modifier.fillMaxWidth().testTag("world_rule_card_${rule.name.lowercase().replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = LuxuryElectricCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryElectricCyan.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = rule.category.uppercase(),
                        color = LuxuryElectricCyan,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = Strings.get("edit", language), tint = LuxuryTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = Strings.get("delete", language), tint = LuxuryWarning, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = rule.name, style = MaterialTheme.typography.titleMedium, color = LuxuryTextPrimary, fontWeight = FontWeight.Bold)

            if (rule.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = rule.description, style = MaterialTheme.typography.bodySmall, color = LuxuryTextSecondary)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LuxurySurfaceHighlight)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${if (language == AppLanguage.ARABIC) "التمدد" else "Dilation"}: ${rule.localTimeEquivalentSeconds}s = ${rule.externalTimeEquivalentYears} ${if (language == AppLanguage.ARABIC) "سنة" else "yrs"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LuxuryAuroraViolet,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${if (language == AppLanguage.ARABIC) "الكفاءة" else "Efficiency"}: ${rule.efficiencyPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = LuxuryCyberIndigo,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
