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
import com.example.data.model.StoryEvent
import com.example.ui.AppLanguage
import com.example.ui.Strings
import com.example.ui.VeyronisViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    viewModel: VeyronisViewModel,
    modifier: Modifier = Modifier
) {
    val allEvents by viewModel.allStoryEvents.collectAsStateWithLifecycle()
    val allTimelines by viewModel.allTimelines.collectAsStateWithLifecycle()
    val allCharacters by viewModel.allCharacters.collectAsStateWithLifecycle()
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedEvent by remember { mutableStateOf<StoryEvent?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<StoryEvent?>(null) }

    val filteredEvents = remember(allEvents, searchQuery) {
        val sorted = allEvents.sortedBy { it.cosmicTimestamp }
        if (searchQuery.isBlank()) sorted
        else sorted.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.summary.contains(searchQuery, ignoreCase = true) ||
                    it.locationNames.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VeyronisBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    eventToEdit = StoryEvent(
                        title = "",
                        timelineId = allTimelines.firstOrNull()?.id ?: 1L
                    )
                    showEditDialog = true
                },
                containerColor = Color(0xFFE879F9),
                contentColor = VeyronisBackground,
                modifier = Modifier.testTag("add_event_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = Strings.get("events_add", language))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("events_title", language),
                    style = MaterialTheme.typography.headlineSmall,
                    color = VeyronisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${allEvents.size} ${Strings.get("events_count", language)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = VeyronisTextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(Strings.get("events_search_hint", language), color = VeyronisTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VeyronisTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE879F9),
                    focusedTextColor = VeyronisTextPrimary,
                    unfocusedTextColor = VeyronisTextPrimary,
                    unfocusedContainerColor = VeyronisPanel,
                    focusedContainerColor = VeyronisPanel
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_search_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEvents) { event ->
                    val timeline = allTimelines.find { it.id == event.timelineId }
                    val charNames = event.characterIds.split(",")
                        .mapNotNull { idStr -> idStr.trim().toLongOrNull() }
                        .mapNotNull { id -> allCharacters.find { it.id == id }?.name }

                    EventCard(
                        event = event,
                        timelineName = timeline?.name ?: if (language == AppLanguage.ARABIC) "الخط الزمني الرئيسي" else "Unknown Timeline",
                        characterNames = charNames,
                        language = language,
                        onClick = { selectedEvent = event },
                        onEdit = {
                            eventToEdit = event
                            showEditDialog = true
                        },
                        onDelete = { viewModel.deleteStoryEvent(event) }
                    )
                }
            }
        }
    }

    // Detail Modal Dialog
    if (selectedEvent != null) {
        val ev = selectedEvent!!
        val timeline = allTimelines.find { it.id == ev.timelineId }

        AlertDialog(
            onDismissRequest = { selectedEvent = null },
            containerColor = VeyronisPanel,
            title = {
                Text(text = ev.title, color = VeyronisTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${Strings.get("events_timestamp", language)}: ${ev.cosmicTimestamp} • ${Strings.get("timeline_title", language)}: ${timeline?.name ?: if (language == AppLanguage.ARABIC) "الرئيسي" else "Prime"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = VeyronisTertiary,
                        fontWeight = FontWeight.Bold
                    )
                    if (ev.summary.isNotBlank()) {
                        Text(text = ev.summary, style = MaterialTheme.typography.bodyMedium, color = VeyronisTextPrimary)
                    }
                    HorizontalDivider(color = VeyronisSurfaceHighlight)
                    if (ev.locationNames.isNotBlank()) DetailSectionItem(Strings.get("events_locations", language), ev.locationNames)
                    if (ev.causes.isNotBlank()) DetailSectionItem(Strings.get("events_causes", language), ev.causes)
                    if (ev.consequences.isNotBlank()) DetailSectionItem(Strings.get("events_consequences", language), ev.consequences)
                    if (ev.relatedLore.isNotBlank()) DetailSectionItem(if (language == AppLanguage.ARABIC) "المعارف المرتبطة" else "Related Lore", ev.relatedLore)
                    if (ev.notes.isNotBlank()) DetailSectionItem(if (language == AppLanguage.ARABIC) "ملاحظات" else "Notes", ev.notes)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEvent = null }) {
                    Text(Strings.get("close", language), color = VeyronisPrimary)
                }
            }
        )
    }

    // Add / Edit Dialog
    if (showEditDialog && eventToEdit != null) {
        val editing = eventToEdit!!
        var title by remember { mutableStateOf(editing.title) }
        var summary by remember { mutableStateOf(editing.summary) }
        var timelineId by remember { mutableStateOf(editing.timelineId) }
        var cosmicTimeStr by remember { mutableStateOf(editing.cosmicTimestamp.toString()) }
        var durationStr by remember { mutableStateOf(editing.durationYears.toString()) }
        var locationNames by remember { mutableStateOf(editing.locationNames) }
        var causes by remember { mutableStateOf(editing.causes) }
        var consequences by remember { mutableStateOf(editing.consequences) }
        var notes by remember { mutableStateOf(editing.notes) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = VeyronisPanel,
            title = { Text(if (editing.id == 0L) Strings.get("events_add", language) else Strings.get("events_edit", language), color = VeyronisTextPrimary) },
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
                        label = { Text(Strings.get("events_name", language)) },
                        modifier = Modifier.fillMaxWidth().testTag("event_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE879F9), focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text(if (language == AppLanguage.ARABIC) "الملخص" else "Summary") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE879F9), focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cosmicTimeStr,
                            onValueChange = { cosmicTimeStr = it },
                            label = { Text(Strings.get("events_timestamp", language)) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE879F9), focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                        )
                        OutlinedTextField(
                            value = durationStr,
                            onValueChange = { durationStr = it },
                            label = { Text(if (language == AppLanguage.ARABIC) "المدة (سنوات)" else "Duration (Years)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE879F9), focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                        )
                    }
                    OutlinedTextField(
                        value = locationNames,
                        onValueChange = { locationNames = it },
                        label = { Text(Strings.get("events_locations", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE879F9), focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = causes,
                        onValueChange = { causes = it },
                        label = { Text(Strings.get("events_causes", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE879F9), focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = consequences,
                        onValueChange = { consequences = it },
                        label = { Text(Strings.get("events_consequences", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE879F9), focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (language == AppLanguage.ARABIC) "ملاحظات" else "Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE879F9), focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val updated = editing.copy(
                                title = title,
                                summary = summary,
                                timelineId = timelineId,
                                cosmicTimestamp = cosmicTimeStr.toDoubleOrNull() ?: 0.0,
                                durationYears = durationStr.toDoubleOrNull() ?: 0.0,
                                locationNames = locationNames,
                                causes = causes,
                                consequences = consequences,
                                notes = notes
                            )
                            viewModel.saveStoryEvent(updated)
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE879F9)),
                    modifier = Modifier.testTag("save_event_button")
                ) {
                    Text(Strings.get("save", language), color = VeyronisBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(Strings.get("cancel", language), color = VeyronisTextSecondary)
                }
            }
        )
    }
}

@Composable
fun EventCard(
    event: StoryEvent,
    timelineName: String,
    characterNames: List<String>,
    language: AppLanguage,
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
            .testTag("event_card_${event.title.lowercase().replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFE879F9).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${if (language == AppLanguage.ARABIC) "السنة الكونية" else "COSMIC"}: ${event.cosmicTimestamp}",
                        color = Color(0xFFE879F9),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = Strings.get("edit", language), tint = VeyronisTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = Strings.get("delete", language), tint = VeyronisWarning, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                color = VeyronisTextPrimary,
                fontWeight = FontWeight.Bold
            )

            if (event.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = VeyronisTextSecondary,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = VeyronisSurfaceHighlight)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${Strings.get("timeline_title", language)}: $timelineName",
                    style = MaterialTheme.typography.labelSmall,
                    color = VeyronisTertiary
                )
                if (event.locationNames.isNotBlank()) {
                    Text(
                        text = "📍 ${event.locationNames}",
                        style = MaterialTheme.typography.labelSmall,
                        color = VeyronisTextMuted,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
