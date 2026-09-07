package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CodexEntry
import com.example.ui.VeyronisViewModel
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
        containerColor = VeyronisBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    entryToEdit = CodexEntry(
                        title = "",
                        category = if (selectedCategory == "All") "Physics" else selectedCategory
                    )
                    showEditDialog = true
                },
                containerColor = VeyronisTertiary,
                contentColor = VeyronisBackground,
                modifier = Modifier.testTag("add_codex_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Codex Entry")
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
                    text = "Codex & Universe Lore",
                    style = MaterialTheme.typography.headlineSmall,
                    color = VeyronisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${allCodex.size} Entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = VeyronisTextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search lore, locations, physics, technology...", color = VeyronisTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VeyronisTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VeyronisTertiary,
                    focusedTextColor = VeyronisTextPrimary,
                    unfocusedTextColor = VeyronisTextPrimary,
                    unfocusedContainerColor = VeyronisPanel,
                    focusedContainerColor = VeyronisPanel
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("codex_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (cat in CODEX_CATEGORIES) {
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VeyronisTertiary,
                            selectedLabelColor = VeyronisBackground,
                            containerColor = VeyronisPanel,
                            labelColor = VeyronisTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Codex Entries List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEntries) { entry ->
                    CodexEntryCard(
                        entry = entry,
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
            containerColor = VeyronisPanel,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = VeyronisTertiaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = entry.category.uppercase(),
                            color = VeyronisTertiary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = entry.title, color = VeyronisTextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (entry.summary.isNotBlank()) {
                        Text(
                            text = entry.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VeyronisTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (entry.description.isNotBlank()) {
                        HorizontalDivider(color = VeyronisSurfaceHighlight)
                        Text(
                            text = entry.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = VeyronisTextSecondary
                        )
                    }
                    if (entry.relatedCharacters.isNotBlank()) {
                        DetailSectionItem("Related Characters", entry.relatedCharacters)
                    }
                    if (entry.relatedLocations.isNotBlank()) {
                        DetailSectionItem("Related Locations", entry.relatedLocations)
                    }
                    if (entry.relatedEvents.isNotBlank()) {
                        DetailSectionItem("Related Events", entry.relatedEvents)
                    }
                    if (entry.relatedLore.isNotBlank()) {
                        DetailSectionItem("Related Lore", entry.relatedLore)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEntry = null }) {
                    Text("Close", color = VeyronisPrimary)
                }
            }
        )
    }

    // Create / Edit Codex Entry Dialog
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
            containerColor = VeyronisPanel,
            title = { Text(if (editing.id == 0L) "New Codex Entry" else "Edit Codex Entry", color = VeyronisTextPrimary) },
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
                        label = { Text("Title *") },
                        modifier = Modifier.fillMaxWidth().testTag("codex_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisTertiary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (e.g. Physics, Locations, Species)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisTertiary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Short Summary") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisTertiary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Lore Description") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisTertiary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = relatedCharacters,
                        onValueChange = { relatedCharacters = it },
                        label = { Text("Related Characters (comma-separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisTertiary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = relatedLocations,
                        onValueChange = { relatedLocations = it },
                        label = { Text("Related Locations") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisTertiary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
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
                    colors = ButtonDefaults.buttonColors(containerColor = VeyronisTertiary),
                    modifier = Modifier.testTag("save_codex_button")
                ) {
                    Text("Save", color = VeyronisBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = VeyronisTextSecondary)
                }
            }
        )
    }
}

@Composable
fun CodexEntryCard(
    entry: CodexEntry,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
        shape = RoundedCornerShape(12.dp),
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
                    color = VeyronisTertiaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = entry.category.uppercase(),
                        color = VeyronisTertiary,
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                color = VeyronisTextPrimary,
                fontWeight = FontWeight.Bold
            )

            if (entry.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = VeyronisTextSecondary,
                    maxLines = 2
                )
            }
        }
    }
}
