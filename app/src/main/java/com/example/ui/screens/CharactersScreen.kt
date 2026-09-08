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
import com.example.ui.AppLanguage
import com.example.ui.Strings
import com.example.ui.VeyronisViewModel
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.components.LuxuryGradientButton
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
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCharacter by remember { mutableStateOf<Character?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var characterToEdit by remember { mutableStateOf<Character?>(null) }

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
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    characterToEdit = Character(name = "")
                    showEditDialog = true
                },
                containerColor = LuxuryElectricCyan,
                contentColor = LuxuryVoidBackground,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_character_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = Strings.get("char_add", language))
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
                    text = Strings.get("char_db_title", language),
                    style = MaterialTheme.typography.headlineSmall,
                    color = LuxuryTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = LuxurySurfaceElevated,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${allCharacters.size} ${Strings.get("char_registered_count", language)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LuxuryTextSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(Strings.get("char_search_hint", language), color = LuxuryTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LuxuryTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuxuryElectricCyan,
                    focusedTextColor = LuxuryTextPrimary,
                    unfocusedTextColor = LuxuryTextPrimary,
                    unfocusedContainerColor = LuxurySurface,
                    focusedContainerColor = LuxurySurface
                ),
                shape = RoundedCornerShape(14.dp),
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

                    LuxuryCharacterCard(
                        character = character,
                        appearancesCount = appearancesCount,
                        relationshipCount = charRelationships.size,
                        language = language,
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
            containerColor = LuxurySurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(
                                color = try { Color(android.graphics.Color.parseColor(char.primaryColorHex)) } catch (e: Exception) { LuxuryElectricCyan },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = char.name, color = LuxuryTextPrimary, fontWeight = FontWeight.Bold)
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
                        Text(text = "${Strings.get("char_alias", language)}: ${char.aliases}", style = MaterialTheme.typography.bodySmall, color = LuxuryTextSecondary)
                    }
                    Text(text = "${Strings.get("char_status", language)}: ${char.currentStatus} ${if (char.isDeceased) "(${if (language == AppLanguage.ARABIC) "متوفى في السنة الكونية" else "Deceased at Cosmic"} ${char.deathCosmicTime})" else ""}", style = MaterialTheme.typography.bodySmall, color = if (char.isDeceased) LuxuryWarning else LuxurySuccess)
                    if (char.age.isNotBlank()) {
                        Text(text = "${Strings.get("char_birth", language)}: ${char.age} • ${Strings.get("char_origin", language)}: ${char.origin}", style = MaterialTheme.typography.bodySmall, color = LuxuryTextSecondary)
                    }

                    HorizontalDivider(color = LuxurySurfaceHighlight)

                    DetailSectionItem(Strings.get("char_appearances", language), "$appearancesCount ${Strings.get("scenes", language)}")
                    if (char.abilities.isNotBlank()) DetailSectionItem(Strings.get("char_abilities", language), char.abilities)
                    if (char.appearance.isNotBlank()) DetailSectionItem(if (language == AppLanguage.ARABIC) "المظهر الخارجي" else "Appearance", char.appearance)
                    if (char.personality.isNotBlank()) DetailSectionItem(if (language == AppLanguage.ARABIC) "السمات الشخصية" else "Personality", char.personality)
                    if (char.strengths.isNotBlank()) DetailSectionItem(if (language == AppLanguage.ARABIC) "نقاط القوة" else "Strengths", char.strengths)
                    if (char.weaknesses.isNotBlank()) DetailSectionItem(if (language == AppLanguage.ARABIC) "نقاط الضعف" else "Weaknesses", char.weaknesses)
                    if (char.notes.isNotBlank()) DetailSectionItem(Strings.get("char_notes", language), char.notes)

                    if (charRelationships.isNotEmpty()) {
                        Text(text = "${Strings.get("char_relationships", language)}:", style = MaterialTheme.typography.labelMedium, color = LuxuryAuroraViolet, fontWeight = FontWeight.Bold)
                        for (rel in charRelationships) {
                            val otherId = if (rel.sourceCharacterId == char.id) rel.targetCharacterId else rel.sourceCharacterId
                            val otherName = allCharacters.find { it.id == otherId }?.name ?: "Unknown"
                            Text(text = "• $otherName — ${rel.relationshipType} (${rel.notes})", style = MaterialTheme.typography.bodySmall, color = LuxuryTextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCharacter = null }) {
                    Text(Strings.get("close", language), color = LuxuryElectricCyan, fontWeight = FontWeight.Bold)
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
            containerColor = LuxurySurface,
            title = { Text(if (editing.id == 0L) Strings.get("char_add", language) else Strings.get("char_edit", language), color = LuxuryTextPrimary, fontWeight = FontWeight.Bold) },
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
                        label = { Text(Strings.get("char_name", language) + " *") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryElectricCyan,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("char_dialog_name_input")
                    )

                    OutlinedTextField(
                        value = aliases,
                        onValueChange = { aliases = it },
                        label = { Text(Strings.get("char_alias", language)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryElectricCyan,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text(Strings.get("char_birth", language)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryElectricCyan,
                                focusedTextColor = LuxuryTextPrimary,
                                unfocusedTextColor = LuxuryTextPrimary,
                                unfocusedContainerColor = LuxurySurfaceElevated,
                                focusedContainerColor = LuxurySurfaceElevated
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = origin,
                            onValueChange = { origin = it },
                            label = { Text(Strings.get("char_origin", language)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryElectricCyan,
                                focusedTextColor = LuxuryTextPrimary,
                                unfocusedTextColor = LuxuryTextPrimary,
                                unfocusedContainerColor = LuxurySurfaceElevated,
                                focusedContainerColor = LuxurySurfaceElevated
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text(Strings.get("char_status", language)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryElectricCyan,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isDeceased,
                            onCheckedChange = { isDeceased = it },
                            colors = CheckboxDefaults.colors(checkedColor = LuxuryWarning)
                        )
                        Text(text = if (language == AppLanguage.ARABIC) "متوفى" else "Deceased", color = LuxuryTextPrimary)
                    }

                    if (isDeceased) {
                        OutlinedTextField(
                            value = deathTimeStr,
                            onValueChange = { deathTimeStr = it },
                            label = { Text(if (language == AppLanguage.ARABIC) "السنة الكونية للوفاة" else "Cosmic Year of Death") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryElectricCyan,
                                focusedTextColor = LuxuryTextPrimary,
                                unfocusedTextColor = LuxuryTextPrimary,
                                unfocusedContainerColor = LuxurySurfaceElevated,
                                focusedContainerColor = LuxurySurfaceElevated
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = abilities,
                        onValueChange = { abilities = it },
                        label = { Text(Strings.get("char_abilities", language)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryElectricCyan,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = appearance,
                        onValueChange = { appearance = it },
                        label = { Text(if (language == AppLanguage.ARABIC) "المظهر الخارجي" else "Appearance") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryElectricCyan,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = personality,
                        onValueChange = { personality = it },
                        label = { Text(if (language == AppLanguage.ARABIC) "السمات الشخصية" else "Personality") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryElectricCyan,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(Strings.get("char_notes", language)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryElectricCyan,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        ),
                        modifier = Modifier.fillMaxWidth()
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
                                currentStatus = status,
                                isDeceased = isDeceased,
                                deathCosmicTime = deathTimeStr.toDoubleOrNull(),
                                abilities = abilities,
                                appearance = appearance,
                                personality = personality,
                                strengths = strengths,
                                weaknesses = weaknesses,
                                notes = notes
                            )
                            viewModel.saveCharacter(updated)
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryElectricCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("char_dialog_save_btn")
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
fun LuxuryCharacterCard(
    character: Character,
    appearancesCount: Int,
    relationshipCount: Int,
    language: AppLanguage,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val charColor = try {
        Color(android.graphics.Color.parseColor(character.primaryColorHex))
    } catch (e: Exception) {
        LuxuryElectricCyan
    }

    LuxuryGlassCard(
        glowColor = charColor,
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
                        color = charColor.copy(alpha = 0.2f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, charColor),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = character.name.take(1).uppercase(),
                                color = LuxuryTextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = LuxuryTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (character.origin.isNotBlank()) {
                            Text(
                                text = character.origin,
                                style = MaterialTheme.typography.bodySmall,
                                color = LuxuryTextSecondary
                            )
                        }
                    }
                }

                Surface(
                    color = if (character.isDeceased) LuxuryWarning.copy(alpha = 0.2f) else LuxurySurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (character.isDeceased) LuxuryWarning else LuxuryElectricCyan.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (character.isDeceased) (if (language == AppLanguage.ARABIC) "متوفى" else "Deceased") else character.currentStatus,
                        color = if (character.isDeceased) LuxuryWarning else LuxuryElectricCyan,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (character.abilities.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${Strings.get("char_abilities", language)}: ${character.abilities}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxuryTextSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LuxurySurfaceHighlight)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "📖 $appearancesCount ${Strings.get("scenes", language)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LuxuryAuroraViolet,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "🔗 $relationshipCount ${if (language == AppLanguage.ARABIC) "روابط" else "bonds"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LuxuryCyberIndigo,
                        fontWeight = FontWeight.SemiBold
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
        }
    }
}

@Composable
fun DetailSectionItem(label: String, content: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = LuxuryTextMuted, fontWeight = FontWeight.Bold)
        Text(text = content, style = MaterialTheme.typography.bodySmall, color = LuxuryTextPrimary)
    }
}
