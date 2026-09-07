package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.Character
import com.example.ui.VeyronisViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersScreen(
    viewModel: VeyronisViewModel,
    modifier: Modifier = Modifier
) {
    val allCharacters by viewModel.allCharacters.collectAsStateWithLifecycle()
    val allRelationships by viewModel.allRelationships.collectAsStateWithLifecycle()
    val allScenes by viewModel.allScenes.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCharacter by remember { mutableStateOf<Character?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var characterToEdit by remember { mutableStateOf<Character?>(null) }
    var showAddRelationshipDialog by remember { mutableStateOf(false) }

    val filteredCharacters = remember(allCharacters, searchQuery) {
        if (searchQuery.isBlank()) allCharacters
        else allCharacters.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.aliases.contains(searchQuery, ignoreCase = true) ||
                    it.origin.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VeyronisBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    characterToEdit = Character(name = "")
                    showEditDialog = true
                },
                containerColor = VeyronisPrimary,
                contentColor = VeyronisTextPrimary,
                modifier = Modifier.testTag("add_character_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Character")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header & Search
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Character Database",
                    style = MaterialTheme.typography.headlineSmall,
                    color = VeyronisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${allCharacters.size} Characters Registered",
                    style = MaterialTheme.typography.bodySmall,
                    color = VeyronisTextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, alias, origin...", color = VeyronisTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VeyronisTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VeyronisSecondary,
                    focusedTextColor = VeyronisTextPrimary,
                    unfocusedTextColor = VeyronisTextPrimary,
                    unfocusedContainerColor = VeyronisPanel,
                    focusedContainerColor = VeyronisPanel
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("character_search_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCharacters) { character ->
                    val appearancesCount = remember(character, allScenes) {
                        allScenes.sumOf { scene ->
                            viewModel.lexiconEngine.countCharacterAppearances(scene.content, character)
                        }
                    }

                    val charRelationships = allRelationships.filter {
                        it.sourceCharacterId == character.id || it.targetCharacterId == character.id
                    }

                    CharacterCard(
                        character = character,
                        appearancesCount = appearancesCount,
                        relationshipCount = charRelationships.size,
                        onClick = { selectedCharacter = character },
                        onEdit = {
                            characterToEdit = character
                            showEditDialog = true
                        },
                        onDelete = { viewModel.deleteCharacter(character) }
                    )
                }
            }
        }
    }

    // Detail Modal Dialog
    if (selectedCharacter != null) {
        val char = selectedCharacter!!
        val charRelationships = allRelationships.filter {
            it.sourceCharacterId == char.id || it.targetCharacterId == char.id
        }
        val appearancesCount = remember(char, allScenes) {
            allScenes.sumOf { scene -> viewModel.lexiconEngine.countCharacterAppearances(scene.content, char) }
        }

        AlertDialog(
            onDismissRequest = { selectedCharacter = null },
            containerColor = VeyronisPanel,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = try { Color(android.graphics.Color.parseColor(char.primaryColorHex)) } catch (e: Exception) { VeyronisPrimary },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = char.name, color = VeyronisTextPrimary, fontWeight = FontWeight.Bold)
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
                    if (char.aliases.isNotBlank()) {
                        Text(text = "Aliases: ${char.aliases}", style = MaterialTheme.typography.bodySmall, color = VeyronisTextSecondary)
                    }
                    Text(text = "Status: ${char.currentStatus} ${if (char.isDeceased) "(Deceased at Cosmic ${char.deathCosmicTime})" else ""}", style = MaterialTheme.typography.bodySmall, color = if (char.isDeceased) VeyronisWarning else VeyronisSuccess)
                    if (char.age.isNotBlank()) {
                        Text(text = "Age: ${char.age} • Origin: ${char.origin}", style = MaterialTheme.typography.bodySmall, color = VeyronisTextSecondary)
                    }

                    HorizontalDivider(color = VeyronisSurfaceHighlight)

                    DetailSectionItem("Manuscript Appearances", "$appearancesCount mentions across loaded scenes")
                    if (char.abilities.isNotBlank()) DetailSectionItem("Abilities & Powers", char.abilities)
                    if (char.appearance.isNotBlank()) DetailSectionItem("Appearance", char.appearance)
                    if (char.personality.isNotBlank()) DetailSectionItem("Personality", char.personality)
                    if (char.strengths.isNotBlank()) DetailSectionItem("Strengths", char.strengths)
                    if (char.weaknesses.isNotBlank()) DetailSectionItem("Weaknesses", char.weaknesses)
                    if (char.notes.isNotBlank()) DetailSectionItem("Notes", char.notes)

                    if (charRelationships.isNotEmpty()) {
                        Text(text = "Relationships:", style = MaterialTheme.typography.labelMedium, color = VeyronisTertiary, fontWeight = FontWeight.Bold)
                        for (rel in charRelationships) {
                            val otherId = if (rel.sourceCharacterId == char.id) rel.targetCharacterId else rel.sourceCharacterId
                            val otherName = allCharacters.find { it.id == otherId }?.name ?: "Unknown"
                            Text(text = "• $otherName — ${rel.relationshipType} (${rel.notes})", style = MaterialTheme.typography.bodySmall, color = VeyronisTextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCharacter = null }) {
                    Text("Close", color = VeyronisPrimary)
                }
            }
        )
    }

    // Edit/Create Character Dialog
    if (showEditDialog && characterToEdit != null) {
        val editing = characterToEdit!!
        var name by remember { mutableStateOf(editing.name) }
        var aliases by remember { mutableStateOf(editing.aliases) }
        var age by remember { mutableStateOf(editing.age) }
        var origin by remember { mutableStateOf(editing.origin) }
        var status by remember { mutableStateOf(editing.currentStatus) }
        var isDeceased by remember { mutableStateOf(editing.isDeceased) }
        var deathTimeStr by remember { mutableStateOf(editing.deathCosmicTime?.toString() ?: "") }
        var abilities by remember { mutableStateOf(editing.abilities) }
        var appearance by remember { mutableStateOf(editing.appearance) }
        var personality by remember { mutableStateOf(editing.personality) }
        var strengths by remember { mutableStateOf(editing.strengths) }
        var weaknesses by remember { mutableStateOf(editing.weaknesses) }
        var notes by remember { mutableStateOf(editing.notes) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = VeyronisPanel,
            title = { Text(if (editing.id == 0L) "Register Character" else "Edit Character", color = VeyronisTextPrimary) },
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
                        label = { Text("Character Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("character_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = aliases,
                        onValueChange = { aliases = it },
                        label = { Text("Aliases (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                        )
                        OutlinedTextField(
                            value = origin,
                            onValueChange = { origin = it },
                            label = { Text("Origin / World") },
                            modifier = Modifier.weight(1.5f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isDeceased,
                            onCheckedChange = { isDeceased = it },
                            colors = CheckboxDefaults.colors(checkedColor = VeyronisWarning)
                        )
                        Text("Is Deceased (Temporal Engine Anchor)", color = VeyronisTextPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                    if (isDeceased) {
                        OutlinedTextField(
                            value = deathTimeStr,
                            onValueChange = { deathTimeStr = it },
                            label = { Text("Demise Cosmic Year (e.g., 1220.0)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisWarning, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                        )
                    }
                    OutlinedTextField(
                        value = abilities,
                        onValueChange = { abilities = it },
                        label = { Text("Abilities & Powers") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = appearance,
                        onValueChange = { appearance = it },
                        label = { Text("Appearance") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = personality,
                        onValueChange = { personality = it },
                        label = { Text("Personality") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Story Notes & Arc") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val updated = editing.copy(
                                name = name,
                                aliases = aliases,
                                age = age,
                                origin = origin,
                                currentStatus = if (isDeceased) "Deceased" else status,
                                isDeceased = isDeceased,
                                deathCosmicTime = deathTimeStr.toDoubleOrNull(),
                                abilities = abilities,
                                appearance = appearance,
                                personality = personality,
                                strengths = strengths,
                                weaknesses = weaknesses,
                                notes = notes,
                                updatedAt = System.currentTimeMillis()
                            )
                            viewModel.saveCharacter(updated)
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
                    modifier = Modifier.testTag("save_character_button")
                ) {
                    Text("Save")
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
fun CharacterCard(
    character: Character,
    appearancesCount: Int,
    relationshipCount: Int,
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
            .testTag("character_card_${character.name.lowercase().replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = try {
                            Color(android.graphics.Color.parseColor(character.primaryColorHex)).copy(alpha = 0.25f)
                        } catch (e: Exception) {
                            VeyronisPrimary.copy(alpha = 0.25f)
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = character.name.take(1).uppercase(),
                                color = VeyronisTextPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = VeyronisTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (character.origin.isNotBlank()) {
                            Text(
                                text = character.origin,
                                style = MaterialTheme.typography.bodySmall,
                                color = VeyronisTextSecondary
                            )
                        }
                    }
                }

                Surface(
                    color = if (character.isDeceased) VeyronisWarningContainer else VeyronisPrimaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (character.isDeceased) "Deceased" else character.currentStatus,
                        color = if (character.isDeceased) VeyronisWarning else VeyronisPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (character.abilities.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Abilities: ${character.abilities}",
                    style = MaterialTheme.typography.bodySmall,
                    color = VeyronisTextSecondary,
                    maxLines = 1
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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "📖 $appearancesCount scenes",
                        style = MaterialTheme.typography.labelSmall,
                        color = VeyronisTertiary
                    )
                    Text(
                        text = "🔗 $relationshipCount bonds",
                        style = MaterialTheme.typography.labelSmall,
                        color = VeyronisSecondary
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
        }
    }
}

@Composable
fun DetailSectionItem(label: String, content: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = VeyronisTextMuted, fontWeight = FontWeight.Bold)
        Text(text = content, style = MaterialTheme.typography.bodySmall, color = VeyronisTextPrimary)
    }
}
