package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Chapter
import com.example.data.model.LexiconTerm
import com.example.data.model.Scene
import com.example.domain.TermOccurrence
import com.example.ui.VeyronisViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriterScreen(
    viewModel: VeyronisViewModel,
    modifier: Modifier = Modifier
) {
    val allSeries by viewModel.allSeries.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val allChapters by viewModel.allChapters.collectAsStateWithLifecycle()
    val allScenes by viewModel.allScenes.collectAsStateWithLifecycle()
    val allLexicon by viewModel.allLexiconTerms.collectAsStateWithLifecycle()
    val allCodex by viewModel.allCodexEntries.collectAsStateWithLifecycle()

    val selectedSeriesId by viewModel.selectedSeriesId.collectAsStateWithLifecycle()
    val selectedBookId by viewModel.selectedBookId.collectAsStateWithLifecycle()
    val selectedChapterId by viewModel.selectedChapterId.collectAsStateWithLifecycle()
    val selectedSceneId by viewModel.selectedSceneId.collectAsStateWithLifecycle()

    val currentScene by viewModel.currentEditingScene.collectAsStateWithLifecycle()
    val editorTitle by viewModel.editorTitle.collectAsStateWithLifecycle()
    val editorContent by viewModel.editorContent.collectAsStateWithLifecycle()
    val isAutosaving by viewModel.isAutosaving.collectAsStateWithLifecycle()
    val lastSavedTime by viewModel.lastSavedTime.collectAsStateWithLifecycle()

    val searchVisible by viewModel.editorSearchVisible.collectAsStateWithLifecycle()
    val searchQuery by viewModel.editorSearchQuery.collectAsStateWithLifecycle()
    val replaceQuery by viewModel.editorReplaceQuery.collectAsStateWithLifecycle()

    val sceneVersions by viewModel.sceneVersions.collectAsStateWithLifecycle()

    var showHierarchySheet by remember { mutableStateOf(false) }
    var showVersionHistorySheet by remember { mutableStateOf(false) }
    var showNewSceneDialog by remember { mutableStateOf(false) }
    var showNewChapterDialog by remember { mutableStateOf(false) }
    var showLexiconDetailDialog by remember { mutableStateOf<LexiconTerm?>(null) }
    var newSceneTitle by remember { mutableStateOf("") }
    var newChapterTitle by remember { mutableStateOf("") }
    var snapshotNote by remember { mutableStateOf("") }
    var showSnapshotDialog by remember { mutableStateOf(false) }

    // Metrics
    val words = if (editorContent.isBlank()) 0 else editorContent.trim().split("\\s+".toRegex()).size
    val chars = editorContent.length
    val readingTimeMinutes = (words / 200.0).coerceAtLeast(1.0).toInt()

    // Find recognized lexicon terms in current manuscript
    val recognizedTerms: List<TermOccurrence> = remember(editorContent, allLexicon) {
        viewModel.lexiconEngine.findTermOccurrences(editorContent, allLexicon)
    }

    val chaptersForCurrentBook = allChapters.filter { it.bookId == selectedBookId }
    val scenesForCurrentChapter = allScenes.filter { it.chapterId == selectedChapterId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VeyronisBackground)
    ) {
        // Top Writing Navigation & Status Bar
        Surface(
            color = VeyronisPanel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Structure Selector Button
                    Button(
                        onClick = { showHierarchySheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = VeyronisPanelVariant),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("hierarchy_selector_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Structure",
                            tint = VeyronisPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = (chaptersForCurrentBook.find { it.id == selectedChapterId }?.title ?: "Select Chapter") +
                                    " / " + (currentScene?.title ?: "Select Scene"),
                            color = VeyronisTextPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = VeyronisTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Autosave & Recovery Indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (isAutosaving) VeyronisTertiaryContainer else Color(0xFF0F2E1E),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            if (isAutosaving) VeyronisTertiary else VeyronisSuccess,
                                            shape = RoundedCornerShape(3.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAutosaving) "Saving..." else "Autosaved",
                                    color = if (isAutosaving) VeyronisTertiary else VeyronisSuccess,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        IconButton(
                            onClick = { showVersionHistorySheet = true },
                            modifier = Modifier.testTag("version_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Version History",
                                tint = VeyronisTextSecondary
                            )
                        }
                    }
                }

                // Formatting & Action Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { viewModel.undo() }, modifier = Modifier.size(36.dp).testTag("undo_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = VeyronisTextPrimary)
                    }
                    IconButton(onClick = { viewModel.redo() }, modifier = Modifier.size(36.dp).testTag("redo_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = VeyronisTextPrimary)
                    }

                    VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 2.dp), color = VeyronisSurfaceHighlight)

                    // Rich text helpers (insert formatting markers)
                    ToolbarFormatButton(label = "B", fontWeight = FontWeight.Bold) {
                        viewModel.onEditorContentChange(editorContent + "**Bold Text**")
                    }
                    ToolbarFormatButton(label = "I", fontStyle = FontStyle.Italic) {
                        viewModel.onEditorContentChange(editorContent + "*Italic Text*")
                    }
                    ToolbarFormatButton(label = "H1") {
                        viewModel.onEditorContentChange(editorContent + "\n\n# Chapter Heading\n")
                    }
                    ToolbarFormatButton(label = "H2") {
                        viewModel.onEditorContentChange(editorContent + "\n\n## Scene Subheading\n")
                    }
                    ToolbarFormatButton(label = "* * *") {
                        viewModel.onEditorContentChange(editorContent + "\n\n* * *\n\n")
                    }

                    VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 2.dp), color = VeyronisSurfaceHighlight)

                    IconButton(
                        onClick = { viewModel.toggleEditorSearch() },
                        modifier = Modifier.size(36.dp).testTag("toggle_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FindReplace,
                            contentDescription = "Search & Replace",
                            tint = if (searchVisible) VeyronisSecondary else VeyronisTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { showSnapshotDialog = true },
                        modifier = Modifier.size(36.dp).testTag("manual_snapshot_button")
                    ) {
                        Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = "Snapshot", tint = VeyronisTertiary)
                    }
                }

                // Search & Replace expandable bar
                AnimatedVisibility(visible = searchVisible) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VeyronisPanelVariant)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setEditorSearchQuery(it) },
                                label = { Text("Find") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("search_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VeyronisSecondary,
                                    focusedTextColor = VeyronisTextPrimary,
                                    unfocusedTextColor = VeyronisTextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = replaceQuery,
                                onValueChange = { viewModel.setEditorReplaceQuery(it) },
                                label = { Text("Replace") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("replace_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VeyronisSecondary,
                                    focusedTextColor = VeyronisTextPrimary,
                                    unfocusedTextColor = VeyronisTextPrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val count = if (searchQuery.isNotEmpty()) {
                                "\\b${Regex.escape(searchQuery)}\\b".toRegex(RegexOption.IGNORE_CASE).findAll(editorContent).count()
                            } else 0
                            Text(
                                text = "$count match(es)",
                                style = MaterialTheme.typography.labelMedium,
                                color = VeyronisTextSecondary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            FilledTonalButton(
                                onClick = { viewModel.executeReplaceOne() },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = VeyronisPanel),
                                modifier = Modifier.testTag("replace_one_button")
                            ) {
                                Text("Replace")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = { viewModel.executeReplaceAll() },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = VeyronisPrimaryContainer),
                                modifier = Modifier.testTag("replace_all_button")
                            ) {
                                Text("Replace All", color = VeyronisTextPrimary)
                            }
                        }
                    }
                }

                // Recognized Lexicon Bar (if terms exist in text)
                if (recognizedTerms.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VeyronisBackground)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            Text(
                                text = "LEXICON:",
                                style = MaterialTheme.typography.labelSmall,
                                color = VeyronisTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(recognizedTerms.distinctBy { it.term.id }) { occurrence ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(occurrence.term.highlightColorHex))
                            } catch (e: Exception) {
                                VeyronisSecondary
                            }
                            Surface(
                                color = color.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .clickable { showLexiconDetailDialog = occurrence.term }
                                    .testTag("lexicon_pill_${occurrence.term.term.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = occurrence.term.term,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VeyronisTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Scene Title TextField
        TextField(
            value = editorTitle,
            onValueChange = { viewModel.onEditorTitleChange(it) },
            placeholder = { Text("Scene Title...", color = VeyronisTextMuted, style = MaterialTheme.typography.titleLarge) },
            textStyle = MaterialTheme.typography.titleLarge.copy(color = VeyronisTextPrimary, fontWeight = FontWeight.Bold),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = VeyronisBackground,
                unfocusedContainerColor = VeyronisBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("scene_title_input")
        )

        HorizontalDivider(color = VeyronisPanel)

        // Main Long-Form Writing Editor
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TextField(
                value = editorContent,
                onValueChange = { viewModel.onEditorContentChange(it) },
                placeholder = {
                    Text(
                        text = "Begin weaving your chapter here...\n\nVeyronis automatically tracks characters, lorebook entities, world physics, and temporal causality as you write.",
                        color = VeyronisTextMuted,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp)
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = VeyronisTextPrimary,
                    lineHeight = 30.sp,
                    fontFamily = FontFamily.Serif
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = VeyronisBackground,
                    unfocusedContainerColor = VeyronisBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("novel_editor_canvas")
            )
        }

        // Bottom Editor Statistics Bar
        Surface(
            color = VeyronisPanel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "$words words",
                        style = MaterialTheme.typography.labelMedium,
                        color = VeyronisTextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("word_counter_text")
                    )
                    Text(
                        text = "$chars chars",
                        style = MaterialTheme.typography.labelMedium,
                        color = VeyronisTextSecondary,
                        modifier = Modifier.testTag("char_counter_text")
                    )
                    Text(
                        text = "~$readingTimeMinutes min read",
                        style = MaterialTheme.typography.labelMedium,
                        color = VeyronisTertiary
                    )
                }

                Text(
                    text = "Veyronis Local Engine v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = VeyronisTextMuted
                )
            }
        }
    }

    // Modal: Structure Hierarchy BottomSheet
    if (showHierarchySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHierarchySheet = false },
            containerColor = VeyronisPanel,
            modifier = Modifier.testTag("hierarchy_modal_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manuscript Hierarchy",
                        style = MaterialTheme.typography.titleLarge,
                        color = VeyronisTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        IconButton(onClick = { showNewChapterDialog = true }) {
                            Icon(Icons.Default.PostAdd, contentDescription = "Add Chapter", tint = VeyronisSecondary)
                        }
                        IconButton(onClick = { showNewSceneDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Scene", tint = VeyronisPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chaptersForCurrentBook) { chapter ->
                        val isCurrentChapter = chapter.id == selectedChapterId
                        val scenesInChapter = allScenes.filter { it.chapterId == chapter.id }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentChapter) VeyronisPanelVariant else VeyronisSurfaceHighlight
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = chapter.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isCurrentChapter) VeyronisPrimary else VeyronisTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { viewModel.selectChapter(chapter.id) }
                                    )
                                    Text(
                                        text = "${scenesInChapter.sumOf { it.wordCount }} words",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VeyronisTextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Scenes in chapter
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    for (scene in scenesInChapter) {
                                        val isSelectedScene = scene.id == selectedSceneId
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    if (isSelectedScene) VeyronisPrimaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .clickable {
                                                    viewModel.selectChapter(chapter.id)
                                                    viewModel.selectScene(scene)
                                                    showHierarchySheet = false
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "• ${scene.title}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isSelectedScene) VeyronisPrimary else VeyronisTextPrimary
                                            )
                                            Row {
                                                IconButton(
                                                    onClick = { viewModel.duplicateScene(scene) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = VeyronisTextMuted, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = { viewModel.deleteScene(scene) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = VeyronisWarning, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Version History / Recovery BottomSheet
    if (showVersionHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showVersionHistorySheet = false },
            containerColor = VeyronisPanel,
            modifier = Modifier.testTag("version_history_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Local Version Snapshots & Recovery",
                    style = MaterialTheme.typography.titleLarge,
                    color = VeyronisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Restore previous iterations of '${currentScene?.title ?: "current scene"}' safely.",
                    style = MaterialTheme.typography.bodySmall,
                    color = VeyronisTextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (sceneVersions.isEmpty()) {
                    Text(
                        text = "No snapshots recorded yet. Create one via the Snapshot button above.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VeyronisTextMuted,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sceneVersions) { version ->
                            val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(version.timestamp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = VeyronisPanelVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = version.changeSummary,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = VeyronisTextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "$dateStr • ${version.wordCount} words",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = VeyronisTextSecondary
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.restoreVersion(version)
                                            showVersionHistorySheet = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Restore", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Lexicon Detail / Link Dialog
    if (showLexiconDetailDialog != null) {
        val term = showLexiconDetailDialog!!
        val matchedCodex = allCodex.find { it.id == term.codexEntryId || it.title.equals(term.term, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { showLexiconDetailDialog = null },
            containerColor = VeyronisPanel,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = VeyronisSecondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = term.category.uppercase(),
                            color = VeyronisSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = term.term, color = VeyronisTextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = term.shortDefinition.ifBlank { "No definition provided." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = VeyronisTextPrimary
                    )
                    if (matchedCodex != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Codex Entry:",
                            style = MaterialTheme.typography.labelMedium,
                            color = VeyronisTertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = matchedCodex.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = VeyronisTextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLexiconDetailDialog = null }) {
                    Text("Close", color = VeyronisPrimary)
                }
            }
        )
    }

    // Dialog: Create New Chapter
    if (showNewChapterDialog) {
        AlertDialog(
            onDismissRequest = { showNewChapterDialog = false },
            containerColor = VeyronisPanel,
            title = { Text("New Chapter", color = VeyronisTextPrimary) },
            text = {
                OutlinedTextField(
                    value = newChapterTitle,
                    onValueChange = { newChapterTitle = it },
                    label = { Text("Chapter Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VeyronisPrimary,
                        focusedTextColor = VeyronisTextPrimary,
                        unfocusedTextColor = VeyronisTextPrimary
                    ),
                    modifier = Modifier.testTag("new_chapter_title_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newChapterTitle.isNotBlank()) {
                            viewModel.createChapter(newChapterTitle)
                            newChapterTitle = ""
                            showNewChapterDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChapterDialog = false }) {
                    Text("Cancel", color = VeyronisTextSecondary)
                }
            }
        )
    }

    // Dialog: Create New Scene
    if (showNewSceneDialog) {
        AlertDialog(
            onDismissRequest = { showNewSceneDialog = false },
            containerColor = VeyronisPanel,
            title = { Text("New Scene", color = VeyronisTextPrimary) },
            text = {
                OutlinedTextField(
                    value = newSceneTitle,
                    onValueChange = { newSceneTitle = it },
                    label = { Text("Scene Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VeyronisPrimary,
                        focusedTextColor = VeyronisTextPrimary,
                        unfocusedTextColor = VeyronisTextPrimary
                    ),
                    modifier = Modifier.testTag("new_scene_title_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSceneTitle.isNotBlank()) {
                            viewModel.createScene(newSceneTitle)
                            newSceneTitle = ""
                            showNewSceneDialog = false
                            showHierarchySheet = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewSceneDialog = false }) {
                    Text("Cancel", color = VeyronisTextSecondary)
                }
            }
        )
    }

    // Dialog: Manual Snapshot
    if (showSnapshotDialog) {
        AlertDialog(
            onDismissRequest = { showSnapshotDialog = false },
            containerColor = VeyronisPanel,
            title = { Text("Record Version Snapshot", color = VeyronisTextPrimary) },
            text = {
                OutlinedTextField(
                    value = snapshotNote,
                    onValueChange = { snapshotNote = it },
                    label = { Text("Change Note (e.g., Pre-reorganization)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VeyronisTertiary,
                        focusedTextColor = VeyronisTextPrimary,
                        unfocusedTextColor = VeyronisTextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val note = snapshotNote.ifBlank { "Manual user checkpoint" }
                        viewModel.createManualSnapshot(note)
                        snapshotNote = ""
                        showSnapshotDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VeyronisTertiary)
                ) {
                    Text("Save Snapshot", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSnapshotDialog = false }) {
                    Text("Cancel", color = VeyronisTextSecondary)
                }
            }
        )
    }
}

@Composable
fun ToolbarFormatButton(
    label: String,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    onClick: () -> Unit
) {
    Surface(
        color = VeyronisPanelVariant,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = VeyronisTextPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = fontWeight,
                fontStyle = fontStyle
            )
        }
    }
}
