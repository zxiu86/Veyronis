package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.CodexEntry
import com.example.ui.AppLanguage
import com.example.ui.Strings
import com.example.ui.VeyronisViewModel
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.components.LuxuryGradientButton
import com.example.ui.theme.*

val CODEX_CATEGORIES = listOf(
    "All", "Characters", "Locations", "Worlds", "Universes", "Species",
    "Organizations", "Technology", "Powers", "Weapons", "Artifacts",
    "Events", "History", "Physics", "Terminology", "Custom"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodexScreen(
    viewModel: VeyronisViewModel,
    modifier: Modifier = Modifier
) {
    val allCodex by viewModel.allCodexEntries.collectAsStateWithLifecycle()
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedEntry by remember { mutableStateOf<CodexEntry?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<CodexEntry?>(null) }

    val filteredEntries = remember(allCodex, selectedCategory, searchQuery) {
        allCodex.filter { entry ->
            val matchesCategory = (selectedCategory == "All" || entry.category.equals(selectedCategory, ignoreCase = true))
            val matchesSearch = searchQuery.isBlank() ||
                    entry.title.contains(searchQuery, ignoreCase = true) ||
                    entry.summary.contains(searchQuery, ignoreCase = true) ||
                    entry.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    entryToEdit = CodexEntry(
                        title = "",
                        category = if (selectedCategory == "All") "Physics" else selectedCategory
                    )
                    showEditDialog = true
                },
                containerColor = LuxurySubtleGold,
                contentColor = LuxuryVoidBackground,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_codex_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = Strings.get("codex_add", language))
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("codex_title", language),
                    style = MaterialTheme.typography.headlineSmall,
                    color = LuxuryTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = LuxurySurfaceElevated,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${allCodex.size} ${Strings.get("codex_entries_count", language)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LuxuryTextSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(Strings.get("codex_search_hint", language), color = LuxuryTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LuxuryTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxurySubtleGold,
                    focusedTextColor = LuxuryTextPrimary,
                    unfocusedTextColor = LuxuryTextPrimary,
                    unfocusedContainerColor = LuxurySurface,
                    focusedContainerColor = LuxurySurface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("codex_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips Horizontal Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (cat in CODEX_CATEGORIES) {
                    val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                    Surface(
                        color = if (isSelected) LuxurySubtleGold else LuxurySurfaceElevated,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) LuxurySubtleGold else LuxurySurfaceHighlight
                        ),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) LuxuryVoidBackground else LuxuryTextSecondary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEntries) { entry ->
                    LuxuryCodexEntryCard(
                        entry = entry,
                        language = language,
                        onClick = { selectedEntry = entry },
                        onEdit = {
                            entryToEdit = entry
                            showEditDialog = true
                        },
                        onDelete = { viewModel.deleteCodexEntry(entry) }
                    )
                }
            }
        }
    }

    // Detail Modal Dialog
    if (selectedEntry != null) {
        val entry = selectedEntry!!
        AlertDialog(
            onDismissRequest = { selectedEntry = null },
            containerColor = LuxurySurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = LuxurySubtleGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = entry.category.uppercase(),
                            color = LuxurySubtleGold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = entry.title, color = LuxuryTextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (entry.summary.isNotBlank()) {
                        Text(text = entry.summary, style = MaterialTheme.typography.bodyMedium, color = LuxuryTextSecondary, fontWeight = FontWeight.SemiBold)
                        HorizontalDivider(color = LuxurySurfaceHighlight)
                    }
                    if (entry.description.isNotBlank()) {
                        Text(text = entry.description, style = MaterialTheme.typography.bodySmall, color = LuxuryTextPrimary)
                    }
                    if (entry.relatedCharacters.isNotBlank()) {
                        Text(text = "${if (language == AppLanguage.ARABIC) "الشخصيات المرتبطة:" else "Related Characters:"} ${entry.relatedCharacters}", style = MaterialTheme.typography.bodySmall, color = LuxuryAuroraViolet)
                    }
                    if (entry.relatedLocations.isNotBlank()) {
                        Text(text = "${if (language == AppLanguage.ARABIC) "المواقع المرتبطة:" else "Related Locations:"} ${entry.relatedLocations}", style = MaterialTheme.typography.bodySmall, color = LuxuryElectricCyan)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEntry = null }) {
                    Text(Strings.get("close", language), color = LuxurySubtleGold, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Edit/Create Dialog
    if (showEditDialog && entryToEdit != null) {
        val editing = entryToEdit!!
        var title by remember { mutableStateOf(editing.title) }
        var category by remember { mutableStateOf(editing.category) }
        var summary by remember { mutableStateOf(editing.summary) }
        var description by remember { mutableStateOf(editing.description) }
        var relatedCharacters by remember { mutableStateOf(editing.relatedCharacters) }
        var relatedLocations by remember { mutableStateOf(editing.relatedLocations) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = LuxurySurface,
            title = { Text(if (editing.id == 0L) Strings.get("codex_add", language) else Strings.get("codex_edit", language), color = LuxuryTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(Strings.get("codex_name", language) + " *") },
                        modifier = Modifier.fillMaxWidth().testTag("codex_dialog_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxurySubtleGold,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        )
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text(Strings.get("codex_category", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxurySubtleGold,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        )
                    )
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text(Strings.get("codex_summary", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxurySubtleGold,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        )
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(Strings.get("codex_desc", language)) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxurySubtleGold,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        )
                    )
                    OutlinedTextField(
                        value = relatedCharacters,
                        onValueChange = { relatedCharacters = it },
                        label = { Text(if (language == AppLanguage.ARABIC) "الشخصيات المرتبطة (مفصولة بفواصل)" else "Related Characters (comma-separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxurySubtleGold,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        )
                    )
                    OutlinedTextField(
                        value = relatedLocations,
                        onValueChange = { relatedLocations = it },
                        label = { Text(if (language == AppLanguage.ARABIC) "المواقع المرتبطة" else "Related Locations") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxurySubtleGold,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val updated = editing.copy(
                                title = title,
                                category = category.ifBlank { "Custom" },
                                summary = summary,
                                description = description,
                                relatedCharacters = relatedCharacters,
                                relatedLocations = relatedLocations,
                                updatedAt = System.currentTimeMillis()
                            )
                            viewModel.saveCodexEntry(updated)
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxurySubtleGold),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("save_codex_button")
                ) {
                    Text(Strings.get("save", language), color = LuxuryVoidBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(Strings.get("cancel", language), color = LuxuryTextSecondary)
                }
            }
        )
    }
}

@Composable
fun LuxuryCodexEntryCard(
    entry: CodexEntry,
    language: AppLanguage,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    LuxuryGlassCard(
        glowColor = LuxurySubtleGold,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("codex_card_${entry.title.lowercase().replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = LuxurySubtleGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuxurySubtleGold.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = entry.category.uppercase(),
                        color = LuxurySubtleGold,
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

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                color = LuxuryTextPrimary,
                fontWeight = FontWeight.Bold
            )

            if (entry.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxuryTextSecondary,
                    maxLines = 2
                )
            }
        }
    }
}
