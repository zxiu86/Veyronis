package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Book
import com.example.data.model.Chapter
import com.example.data.model.Scene
import com.example.data.model.Series
import com.example.domain.TermOccurrence
import com.example.ui.AppLanguage
import com.example.ui.Strings
import com.example.ui.VeyronisViewModel
import com.example.ui.components.BookCoverCard
import com.example.ui.components.LuxuryCoverPicker
import com.example.ui.components.LuxuryStepPill
import com.example.ui.components.LuxuryGradientButton
import com.example.ui.components.LuxurySecondaryButton
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.theme.*

enum class WriterWorkflowStep {
    PROJECT_TYPE_CHOICE,    // Choice between Standalone Novel or Multi-Book Series
    STANDALONE_SETUP,       // Setup standalone novel (Cover, Name, Blurb, Target Words)
    SERIES_SETUP,           // Setup series info (Cover, Name, Blurb, Universe Lore)
    SERIES_SELECTION,       // View list of existing series with covers & info
    BOOKS_SELECTION,        // View books inside chosen series with covers & info
    BOOK_SETUP,             // Add or Edit a book inside a series (Cover, Title, Subtitle, Info)
    CHAPTERS_SELECTION,     // Manage Chapters & Scenes of current book / novel
    EDITOR                  // The writing canvas
}

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
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()

    val searchVisible by viewModel.editorSearchVisible.collectAsStateWithLifecycle()
    val searchQuery by viewModel.editorSearchQuery.collectAsStateWithLifecycle()
    val replaceQuery by viewModel.editorReplaceQuery.collectAsStateWithLifecycle()
    val sceneVersions by viewModel.sceneVersions.collectAsStateWithLifecycle()

    // Determine navigation step
    var currentStep by remember {
        mutableStateOf(
            if (allSeries.isEmpty()) WriterWorkflowStep.PROJECT_TYPE_CHOICE
            else if (currentScene != null) WriterWorkflowStep.EDITOR
            else WriterWorkflowStep.SERIES_SELECTION
        )
    }

    // Forms State
    var formTitle by remember { mutableStateOf("") }
    var formSubtitle by remember { mutableStateOf("") }
    var formDescription by remember { mutableStateOf("") }
    var formCoverUri by remember { mutableStateOf("") }
    var formTargetWords by remember { mutableStateOf("80000") }

    // Dialogs state
    var showCreateChapterDialog by remember { mutableStateOf(false) }
    var showCreateSceneDialog by remember { mutableStateOf(false) }
    var showVersionHistorySheet by remember { mutableStateOf(false) }
    var showSnapshotDialog by remember { mutableStateOf(false) }

    var newChapterTitle by remember { mutableStateOf("") }
    var newChapterSummary by remember { mutableStateOf("") }
    var newSceneTitle by remember { mutableStateOf("") }
    var snapshotNote by remember { mutableStateOf("") }

    val currentSeries = allSeries.find { it.id == selectedSeriesId } ?: allSeries.firstOrNull()
    val currentBook = allBooks.find { it.id == selectedBookId } ?: allBooks.firstOrNull { it.seriesId == currentSeries?.id }
    val currentChapter = allChapters.find { it.id == selectedChapterId } ?: allChapters.firstOrNull { it.bookId == currentBook?.id }

    val booksForSeries = allBooks.filter { it.seriesId == currentSeries?.id }
    val chaptersForBook = allChapters.filter { it.bookId == currentBook?.id }
    val scenesForChapter = allScenes.filter { it.chapterId == currentChapter?.id }

    // Metrics
    val words = if (editorContent.isBlank()) 0 else editorContent.trim().split("\\s+".toRegex()).size
    val chars = editorContent.length
    val readingTimeMinutes = (words / 200.0).coerceAtLeast(1.0).toInt()

    // Recognized terms
    val recognizedTerms: List<TermOccurrence> = remember(editorContent, allLexicon) {
        viewModel.lexiconEngine.findTermOccurrences(editorContent, allLexicon)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VeyronisBackground)
    ) {
        // Navigation Header Bar (Breadcrumbs & Step Pills)
        Surface(
            color = VeyronisPanel,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LuxuryStepPill(
                        number = 1,
                        title = Strings.get("project_hub_title", language),
                        isActive = currentStep == WriterWorkflowStep.PROJECT_TYPE_CHOICE || currentStep == WriterWorkflowStep.SERIES_SELECTION || currentStep == WriterWorkflowStep.STANDALONE_SETUP || currentStep == WriterWorkflowStep.SERIES_SETUP,
                        isDone = allSeries.isNotEmpty() && currentStep != WriterWorkflowStep.PROJECT_TYPE_CHOICE && currentStep != WriterWorkflowStep.STANDALONE_SETUP && currentStep != WriterWorkflowStep.SERIES_SETUP,
                        onClick = { currentStep = if (allSeries.isEmpty()) WriterWorkflowStep.PROJECT_TYPE_CHOICE else WriterWorkflowStep.SERIES_SELECTION }
                    )

                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = LuxuryTextMuted, modifier = Modifier.size(16.dp))

                    LuxuryStepPill(
                        number = 2,
                        title = Strings.get("step_books", language),
                        isActive = currentStep == WriterWorkflowStep.BOOKS_SELECTION || currentStep == WriterWorkflowStep.BOOK_SETUP,
                        isDone = currentBook != null && (currentStep == WriterWorkflowStep.CHAPTERS_SELECTION || currentStep == WriterWorkflowStep.EDITOR),
                        onClick = {
                            if (currentSeries != null) currentStep = WriterWorkflowStep.BOOKS_SELECTION
                        }
                    )

                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = LuxuryTextMuted, modifier = Modifier.size(16.dp))

                    LuxuryStepPill(
                        number = 3,
                        title = Strings.get("step_chapters", language),
                        isActive = currentStep == WriterWorkflowStep.CHAPTERS_SELECTION,
                        isDone = currentChapter != null && currentStep == WriterWorkflowStep.EDITOR,
                        onClick = {
                            if (currentBook != null) currentStep = WriterWorkflowStep.CHAPTERS_SELECTION
                        }
                    )

                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = LuxuryTextMuted, modifier = Modifier.size(16.dp))

                    LuxuryStepPill(
                        number = 4,
                        title = Strings.get("step_writer", language),
                        isActive = currentStep == WriterWorkflowStep.EDITOR,
                        isDone = false,
                        onClick = {
                            if (currentScene != null) currentStep = WriterWorkflowStep.EDITOR
                        }
                    )
                }
                HorizontalDivider(color = VeyronisSurfaceHighlight)
            }
        }

        // Screen Content with Animated Crossfade
        AnimatedContent(
            targetState = currentStep,
            label = "writer_workflow_anim",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { step ->
            when (step) {
                // CHOICE: STANDALONE NOVEL VS SERIES
                WriterWorkflowStep.PROJECT_TYPE_CHOICE -> {
                    ProjectTypeChoiceView(
                        language = language,
                        hasExistingProjects = allSeries.isNotEmpty(),
                        onSelectStandalone = {
                            formTitle = ""
                            formSubtitle = ""
                            formDescription = ""
                            formCoverUri = ""
                            formTargetWords = "80000"
                            currentStep = WriterWorkflowStep.STANDALONE_SETUP
                        },
                        onSelectSeries = {
                            formTitle = ""
                            formDescription = ""
                            formCoverUri = ""
                            currentStep = WriterWorkflowStep.SERIES_SETUP
                        },
                        onViewExisting = {
                            currentStep = WriterWorkflowStep.SERIES_SELECTION
                        }
                    )
                }

                // SETUP: STANDALONE NOVEL
                WriterWorkflowStep.STANDALONE_SETUP -> {
                    StandaloneNovelSetupView(
                        language = language,
                        title = formTitle,
                        description = formDescription,
                        coverUri = formCoverUri,
                        targetWords = formTargetWords,
                        onTitleChange = { formTitle = it },
                        onDescriptionChange = { formDescription = it },
                        onCoverUriChange = { formCoverUri = it },
                        onTargetWordsChange = { formTargetWords = it },
                        onBack = { currentStep = WriterWorkflowStep.PROJECT_TYPE_CHOICE },
                        onConfirm = {
                            if (formTitle.isNotBlank()) {
                                val wordsCount = formTargetWords.toIntOrNull() ?: 80000
                                viewModel.createStandaloneNovel(
                                    title = formTitle,
                                    desc = formDescription,
                                    coverUri = formCoverUri,
                                    targetWordCount = wordsCount
                                )
                                currentStep = WriterWorkflowStep.CHAPTERS_SELECTION
                            }
                        }
                    )
                }

                // SETUP: EPIC SERIES
                WriterWorkflowStep.SERIES_SETUP -> {
                    SeriesSetupView(
                        language = language,
                        title = formTitle,
                        description = formDescription,
                        coverUri = formCoverUri,
                        onTitleChange = { formTitle = it },
                        onDescriptionChange = { formDescription = it },
                        onCoverUriChange = { formCoverUri = it },
                        onBack = { currentStep = if (allSeries.isEmpty()) WriterWorkflowStep.PROJECT_TYPE_CHOICE else WriterWorkflowStep.SERIES_SELECTION },
                        onConfirm = {
                            if (formTitle.isNotBlank()) {
                                viewModel.createSeries(
                                    title = formTitle,
                                    desc = formDescription,
                                    coverUri = formCoverUri
                                )
                                formTitle = ""
                                formSubtitle = ""
                                formDescription = ""
                                formCoverUri = ""
                                currentStep = WriterWorkflowStep.BOOKS_SELECTION
                            }
                        }
                    )
                }

                // VIEW: SERIES LIST (With Covers & Info)
                WriterWorkflowStep.SERIES_SELECTION -> {
                    SeriesSelectionView(
                        seriesList = allSeries,
                        selectedSeriesId = selectedSeriesId,
                        language = language,
                        onSelectSeries = { series ->
                            viewModel.selectSeries(series.id)
                            currentStep = WriterWorkflowStep.BOOKS_SELECTION
                        },
                        onCreateSeries = {
                            formTitle = ""
                            formDescription = ""
                            formCoverUri = ""
                            currentStep = WriterWorkflowStep.SERIES_SETUP
                        },
                        onDeleteSeries = { series -> viewModel.deleteSeries(series) },
                        onNewProjectChoice = { currentStep = WriterWorkflowStep.PROJECT_TYPE_CHOICE }
                    )
                }

                // VIEW: BOOKS LIST (With Covers & Volume Info)
                WriterWorkflowStep.BOOKS_SELECTION -> {
                    BooksSelectionView(
                        series = currentSeries,
                        books = booksForSeries,
                        selectedBookId = selectedBookId,
                        language = language,
                        onBack = { currentStep = WriterWorkflowStep.SERIES_SELECTION },
                        onSelectBook = { book ->
                            viewModel.selectBook(book.id)
                            currentStep = WriterWorkflowStep.CHAPTERS_SELECTION
                        },
                        onCreateBook = {
                            formTitle = ""
                            formSubtitle = "الجزء ${booksForSeries.size + 1}"
                            formDescription = ""
                            formCoverUri = ""
                            formTargetWords = "80000"
                            currentStep = WriterWorkflowStep.BOOK_SETUP
                        },
                        onDeleteBook = { book -> viewModel.deleteBook(book) }
                    )
                }

                // SETUP: ADD OR EDIT BOOK
                WriterWorkflowStep.BOOK_SETUP -> {
                    BookSetupView(
                        seriesTitle = currentSeries?.title ?: "",
                        language = language,
                        title = formTitle,
                        subtitle = formSubtitle,
                        description = formDescription,
                        coverUri = formCoverUri,
                        targetWords = formTargetWords,
                        onTitleChange = { formTitle = it },
                        onSubtitleChange = { formSubtitle = it },
                        onDescriptionChange = { formDescription = it },
                        onCoverUriChange = { formCoverUri = it },
                        onTargetWordsChange = { formTargetWords = it },
                        onBack = { currentStep = WriterWorkflowStep.BOOKS_SELECTION },
                        onConfirm = {
                            if (formTitle.isNotBlank()) {
                                val wordsCount = formTargetWords.toIntOrNull() ?: 80000
                                viewModel.createBook(
                                    title = formTitle,
                                    subtitle = formSubtitle,
                                    desc = formDescription,
                                    coverUri = formCoverUri,
                                    targetWordCount = wordsCount
                                )
                                currentStep = WriterWorkflowStep.CHAPTERS_SELECTION
                            }
                        }
                    )
                }

                // VIEW: CHAPTERS & SCENES
                WriterWorkflowStep.CHAPTERS_SELECTION -> {
                    ChaptersSelectionView(
                        book = currentBook,
                        series = currentSeries,
                        chapters = chaptersForBook,
                        allScenes = allScenes,
                        selectedChapterId = selectedChapterId,
                        selectedSceneId = selectedSceneId,
                        language = language,
                        onBack = { currentStep = WriterWorkflowStep.BOOKS_SELECTION },
                        onSelectChapter = { chapter -> viewModel.selectChapter(chapter.id) },
                        onSelectScene = { scene ->
                            viewModel.selectScene(scene)
                            currentStep = WriterWorkflowStep.EDITOR
                        },
                        onCreateChapter = { showCreateChapterDialog = true },
                        onCreateScene = { chapter ->
                            viewModel.selectChapter(chapter.id)
                            showCreateSceneDialog = true
                        },
                        onDeleteChapter = { chapter -> viewModel.deleteChapter(chapter) },
                        onDeleteScene = { scene -> viewModel.deleteScene(scene) },
                        onDirectWrite = {
                            if (scenesForChapter.isNotEmpty()) {
                                viewModel.selectScene(scenesForChapter.first())
                                currentStep = WriterWorkflowStep.EDITOR
                            } else if (chaptersForBook.isNotEmpty()) {
                                viewModel.selectChapter(chaptersForBook.first().id)
                                showCreateSceneDialog = true
                            } else {
                                showCreateChapterDialog = true
                            }
                        }
                    )
                }

                // VIEW: FOCUSED WRITER CANVAS
                WriterWorkflowStep.EDITOR -> {
                    FocusedEditorView(
                        viewModel = viewModel,
                        currentSeries = currentSeries,
                        currentBook = currentBook,
                        currentChapter = currentChapter,
                        currentScene = currentScene,
                        editorTitle = editorTitle,
                        editorContent = editorContent,
                        words = words,
                        chars = chars,
                        readingTimeMinutes = readingTimeMinutes,
                        isAutosaving = isAutosaving,
                        lastSavedTime = lastSavedTime,
                        searchVisible = searchVisible,
                        searchQuery = searchQuery,
                        replaceQuery = replaceQuery,
                        recognizedTerms = recognizedTerms,
                        language = language,
                        onBackToHierarchy = { currentStep = WriterWorkflowStep.CHAPTERS_SELECTION },
                        onTitleChange = { viewModel.onEditorTitleChange(it) },
                        onContentChange = { viewModel.onEditorContentChange(it) },
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() },
                        onToggleSearch = { viewModel.toggleEditorSearch() },
                        onSearchChange = { viewModel.setEditorSearchQuery(it) },
                        onReplaceChange = { viewModel.setEditorReplaceQuery(it) },
                        onReplaceOne = { viewModel.executeReplaceOne() },
                        onReplaceAll = { viewModel.executeReplaceAll() },
                        onOpenSnapshots = { showVersionHistorySheet = true },
                        onCreateSnapshot = { showSnapshotDialog = true }
                    )
                }
            }
        }
    }

    // DIALOG: Create Chapter
    if (showCreateChapterDialog) {
        AlertDialog(
            onDismissRequest = { showCreateChapterDialog = false },
            containerColor = VeyronisPanel,
            title = {
                Text(
                    text = Strings.get("create_chapter", language),
                    color = VeyronisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newChapterTitle,
                        onValueChange = { newChapterTitle = it },
                        label = { Text(Strings.get("chapter_name", language)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VeyronisPrimary,
                            focusedTextColor = VeyronisTextPrimary,
                            unfocusedTextColor = VeyronisTextPrimary,
                            unfocusedContainerColor = VeyronisPanelVariant,
                            focusedContainerColor = VeyronisPanelVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_chapter_title_input")
                    )
                    OutlinedTextField(
                        value = newChapterSummary,
                        onValueChange = { newChapterSummary = it },
                        label = { Text(Strings.get("chapter_desc", language)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VeyronisPrimary,
                            focusedTextColor = VeyronisTextPrimary,
                            unfocusedTextColor = VeyronisTextPrimary,
                            unfocusedContainerColor = VeyronisPanelVariant,
                            focusedContainerColor = VeyronisPanelVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newChapterTitle.isNotBlank()) {
                            viewModel.createChapter(newChapterTitle, newChapterSummary)
                            newChapterTitle = ""
                            newChapterSummary = ""
                            showCreateChapterDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
                    modifier = Modifier.testTag("confirm_create_chapter_btn")
                ) {
                    Text(Strings.get("save", language), color = VeyronisBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateChapterDialog = false }) {
                    Text(Strings.get("cancel", language), color = VeyronisTextSecondary)
                }
            }
        )
    }

    // DIALOG: Create Scene
    if (showCreateSceneDialog) {
        AlertDialog(
            onDismissRequest = { showCreateSceneDialog = false },
            containerColor = VeyronisPanel,
            title = {
                Text(
                    text = Strings.get("create_scene", language),
                    color = VeyronisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = newSceneTitle,
                    onValueChange = { newSceneTitle = it },
                    label = { Text(Strings.get("scene_name", language)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VeyronisPrimary,
                        focusedTextColor = VeyronisTextPrimary,
                        unfocusedTextColor = VeyronisTextPrimary,
                        unfocusedContainerColor = VeyronisPanelVariant,
                        focusedContainerColor = VeyronisPanelVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_scene_title_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSceneTitle.isNotBlank()) {
                            viewModel.createScene(newSceneTitle)
                            newSceneTitle = ""
                            showCreateSceneDialog = false
                            currentStep = WriterWorkflowStep.EDITOR
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
                    modifier = Modifier.testTag("confirm_create_scene_btn")
                ) {
                    Text(Strings.get("save", language), color = VeyronisBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSceneDialog = false }) {
                    Text(Strings.get("cancel", language), color = VeyronisTextSecondary)
                }
            }
        )
    }

    // DIALOG: Snapshot
    if (showSnapshotDialog) {
        AlertDialog(
            onDismissRequest = { showSnapshotDialog = false },
            containerColor = VeyronisPanel,
            title = {
                Text("حفظ لقطة إصدار للمشهد", color = VeyronisTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = snapshotNote,
                    onValueChange = { snapshotNote = it },
                    label = { Text("ملاحظة النسخة (مثال: قبل تعديل الحوار)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VeyronisPrimary,
                        focusedTextColor = VeyronisTextPrimary,
                        unfocusedTextColor = VeyronisTextPrimary,
                        unfocusedContainerColor = VeyronisPanelVariant,
                        focusedContainerColor = VeyronisPanelVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val note = snapshotNote.ifBlank { "لقطة حفظ يدوية" }
                        viewModel.createManualSnapshot(note)
                        snapshotNote = ""
                        showSnapshotDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VeyronisTertiary)
                ) {
                    Text(Strings.get("save", language), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSnapshotDialog = false }) {
                    Text(Strings.get("cancel", language), color = VeyronisTextSecondary)
                }
            }
        )
    }

    // BottomSheet: Version History
    if (showVersionHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showVersionHistorySheet = false },
            containerColor = VeyronisPanel
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "سجل إصدارات المشهد (${sceneVersions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = VeyronisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (sceneVersions.isEmpty()) {
                    Text("لا توجد لقطات محفوظة بعد.", color = VeyronisTextMuted)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sceneVersions) { ver ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = VeyronisPanelVariant),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(ver.title, color = VeyronisTextPrimary, fontWeight = FontWeight.Bold)
                                        Text(ver.changeSummary, color = VeyronisTertiary, style = MaterialTheme.typography.labelSmall)
                                        Text("${ver.wordCount} كلمة", color = VeyronisTextSecondary, style = MaterialTheme.typography.labelSmall)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.restoreVersion(ver)
                                            showVersionHistorySheet = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimaryContainer)
                                    ) {
                                        Text("استرجاع", color = VeyronisPrimary)
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

// -------------------------------------------------------------------------------------------------
// 1. PROJECT TYPE CHOICE SCREEN (Standalone Novel vs Epic Series)
// -------------------------------------------------------------------------------------------------
@Composable
fun ProjectTypeChoiceView(
    language: AppLanguage,
    hasExistingProjects: Boolean,
    onSelectStandalone: () -> Unit,
    onSelectSeries: () -> Unit,
    onViewExisting: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            color = VeyronisPrimaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "⚡ " + Strings.get("project_hub_title", language),
                color = VeyronisPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Text(
            text = Strings.get("project_hub_sub", language),
            style = MaterialTheme.typography.bodyMedium,
            color = VeyronisTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Option A: Standalone Novel Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
            border = BorderStroke(1.5.dp, VeyronisPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectStandalone() }
                .testTag("choice_standalone_novel_btn")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookCoverCard(
                    coverUri = null,
                    title = Strings.get("type_standalone_novel", language),
                    subtitle = "كتاب واحد مستقل",
                    width = 100.dp,
                    height = 140.dp,
                    gradientIndex = 0
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = VeyronisPrimaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "سريع ومباشر",
                            color = VeyronisPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = Strings.get("type_standalone_novel", language),
                        style = MaterialTheme.typography.titleLarge,
                        color = VeyronisTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = Strings.get("type_standalone_novel_desc", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = VeyronisTextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onSelectStandalone,
                        colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = Strings.get("start_standalone", language),
                            color = VeyronisBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Option B: Epic Series Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
            border = BorderStroke(1.5.dp, VeyronisSecondary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectSeries() }
                .testTag("choice_series_btn")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookCoverCard(
                    coverUri = null,
                    title = Strings.get("type_series", language),
                    subtitle = "سلسلة أجزاء متتابعة",
                    width = 100.dp,
                    height = 140.dp,
                    gradientIndex = 1
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = VeyronisSecondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "ملحمي متعدد الأجزاء",
                            color = VeyronisSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = Strings.get("type_series", language),
                        style = MaterialTheme.typography.titleLarge,
                        color = VeyronisTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = Strings.get("type_series_desc", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = VeyronisTextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onSelectSeries,
                        colors = ButtonDefaults.buttonColors(containerColor = VeyronisSecondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = Strings.get("start_series", language),
                            color = VeyronisBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (hasExistingProjects) {
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = onViewExisting,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, VeyronisPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = VeyronisPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.ARABIC) "استعراض المشاريع والسلاسل الحالية" else "View Existing Projects",
                    color = VeyronisPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 2. STANDALONE NOVEL SETUP VIEW
// -------------------------------------------------------------------------------------------------
@Composable
fun StandaloneNovelSetupView(
    language: AppLanguage,
    title: String,
    description: String,
    coverUri: String,
    targetWords: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCoverUriChange: (String) -> Unit,
    onTargetWordsChange: (String) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VeyronisPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = Strings.get("start_standalone", language),
                    style = MaterialTheme.typography.titleLarge,
                    color = VeyronisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "حدد الغلاف، الاسم والنبذة للانطلاق مباشرة للفصول والكتابة",
                    style = MaterialTheme.typography.bodySmall,
                    color = VeyronisTextSecondary
                )
            }
        }

        // Live Preview of Novel Cover & Title
        Card(
            colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookCoverCard(
                    coverUri = coverUri,
                    title = title.ifBlank { "عنوان الرواية" },
                    subtitle = "رواية مستقلة",
                    badgeText = "NOVEL",
                    badgeColor = VeyronisPrimary,
                    width = 110.dp,
                    height = 160.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.ifBlank { "اسم الرواية سيظهر هنا" },
                        style = MaterialTheme.typography.titleMedium,
                        color = VeyronisTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description.ifBlank { "ملخص ونبذة الرواية سيظهر هنا..." },
                        style = MaterialTheme.typography.bodySmall,
                        color = VeyronisTextSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "الهدف: $targetWords كلمة",
                        style = MaterialTheme.typography.labelSmall,
                        color = VeyronisTertiary
                    )
                }
            }
        }

        // Interactive Cover Selection (Phone Gallery, Presets, Custom Link)
        LuxuryCoverPicker(
            selectedCoverUri = coverUri,
            onCoverSelected = onCoverUriChange,
            titleHint = Strings.get("choose_cover_preset", language)
        )

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(Strings.get("novel_name", language) + " *") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VeyronisPrimary,
                focusedTextColor = VeyronisTextPrimary,
                unfocusedTextColor = VeyronisTextPrimary,
                unfocusedContainerColor = VeyronisPanelVariant,
                focusedContainerColor = VeyronisPanelVariant
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("standalone_novel_title_input")
        )

        // Description / Blurb Input
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(Strings.get("novel_summary", language)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VeyronisPrimary,
                focusedTextColor = VeyronisTextPrimary,
                unfocusedTextColor = VeyronisTextPrimary,
                unfocusedContainerColor = VeyronisPanelVariant,
                focusedContainerColor = VeyronisPanelVariant
            ),
            shape = RoundedCornerShape(10.dp),
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        // Target Word Count
        OutlinedTextField(
            value = targetWords,
            onValueChange = onTargetWordsChange,
            label = { Text(Strings.get("target_words", language)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VeyronisPrimary,
                focusedTextColor = VeyronisTextPrimary,
                unfocusedTextColor = VeyronisTextPrimary,
                unfocusedContainerColor = VeyronisPanelVariant,
                focusedContainerColor = VeyronisPanelVariant
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Save & Open Chapters Button
        Button(
            onClick = onConfirm,
            enabled = title.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("confirm_standalone_novel_btn")
        ) {
            Text(
                text = if (language == AppLanguage.ARABIC) "حفظ والانطلاق للفصول والكتابة 🚀" else "Save & Launch Chapters 🚀",
                color = VeyronisBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 3. SERIES SETUP VIEW
// -------------------------------------------------------------------------------------------------
@Composable
fun SeriesSetupView(
    language: AppLanguage,
    title: String,
    description: String,
    coverUri: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCoverUriChange: (String) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VeyronisPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = Strings.get("series_info_title", language),
                    style = MaterialTheme.typography.titleLarge,
                    color = VeyronisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "حدد كافر السلسلة واسمها ونبذة عامة، ثم أضف أجزاءها لاحقاً",
                    style = MaterialTheme.typography.bodySmall,
                    color = VeyronisTextSecondary
                )
            }
        }

        // Live Series Card Preview
        Card(
            colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookCoverCard(
                    coverUri = coverUri,
                    title = title.ifBlank { "اسم السلسلة" },
                    subtitle = "سلسلة روائية",
                    badgeText = "SERIES",
                    badgeColor = VeyronisSecondary,
                    width = 110.dp,
                    height = 160.dp,
                    gradientIndex = 1
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.ifBlank { "اسم السلسلة سيظهر هنا" },
                        style = MaterialTheme.typography.titleMedium,
                        color = VeyronisTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description.ifBlank { "نبذة ومعلومات السلسلة العامة..." },
                        style = MaterialTheme.typography.bodySmall,
                        color = VeyronisTextSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Interactive Cover Selection (Phone Gallery, Presets, Custom Link)
        LuxuryCoverPicker(
            selectedCoverUri = coverUri,
            onCoverSelected = onCoverUriChange,
            titleHint = Strings.get("choose_cover_preset", language)
        )

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(Strings.get("series_name", language) + " *") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VeyronisPrimary,
                focusedTextColor = VeyronisTextPrimary,
                unfocusedTextColor = VeyronisTextPrimary,
                unfocusedContainerColor = VeyronisPanelVariant,
                focusedContainerColor = VeyronisPanelVariant
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("series_title_input")
        )

        // Description / Lore Input
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(Strings.get("series_desc", language)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VeyronisPrimary,
                focusedTextColor = VeyronisTextPrimary,
                unfocusedTextColor = VeyronisTextPrimary,
                unfocusedContainerColor = VeyronisPanelVariant,
                focusedContainerColor = VeyronisPanelVariant
            ),
            shape = RoundedCornerShape(10.dp),
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Confirm Button
        Button(
            onClick = onConfirm,
            enabled = title.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = VeyronisSecondary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("confirm_series_btn")
        ) {
            Text(
                text = if (language == AppLanguage.ARABIC) "حفظ ودخول لإدارة الأجزاء 📚" else "Save & Manage Books 📚",
                color = VeyronisBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 4. BOOK / VOLUME SETUP VIEW (Inside Series)
// -------------------------------------------------------------------------------------------------
@Composable
fun BookSetupView(
    seriesTitle: String,
    language: AppLanguage,
    title: String,
    subtitle: String,
    description: String,
    coverUri: String,
    targetWords: String,
    onTitleChange: (String) -> Unit,
    onSubtitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCoverUriChange: (String) -> Unit,
    onTargetWordsChange: (String) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VeyronisPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = Strings.get("book_info_title", language),
                    style = MaterialTheme.typography.titleLarge,
                    color = VeyronisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ضمن سلسلة: $seriesTitle",
                    style = MaterialTheme.typography.bodySmall,
                    color = VeyronisTertiary
                )
            }
        }

        // Live Book Cover Preview
        Card(
            colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookCoverCard(
                    coverUri = coverUri,
                    title = title.ifBlank { "عنوان الجزء" },
                    subtitle = subtitle.ifBlank { "الجزء الجديد" },
                    badgeText = "BOOK",
                    badgeColor = VeyronisPrimary,
                    width = 110.dp,
                    height = 160.dp,
                    gradientIndex = 2
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.ifBlank { "اسم الجزء سيظهر هنا" },
                        style = MaterialTheme.typography.titleMedium,
                        color = VeyronisTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = VeyronisTertiary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description.ifBlank { "ملخص أحداث هذا الجزء..." },
                        style = MaterialTheme.typography.bodySmall,
                        color = VeyronisTextSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Interactive Cover Selection (Phone Gallery, Presets, Custom Link)
        LuxuryCoverPicker(
            selectedCoverUri = coverUri,
            onCoverSelected = onCoverUriChange,
            titleHint = Strings.get("choose_cover_preset", language)
        )

        // Book Title Input
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(Strings.get("book_name", language) + " *") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VeyronisPrimary,
                focusedTextColor = VeyronisTextPrimary,
                unfocusedTextColor = VeyronisTextPrimary,
                unfocusedContainerColor = VeyronisPanelVariant,
                focusedContainerColor = VeyronisPanelVariant
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("book_title_input")
        )

        // Book Subtitle (e.g. Volume 1)
        OutlinedTextField(
            value = subtitle,
            onValueChange = onSubtitleChange,
            label = { Text(Strings.get("book_subtitle", language)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VeyronisPrimary,
                focusedTextColor = VeyronisTextPrimary,
                unfocusedTextColor = VeyronisTextPrimary,
                unfocusedContainerColor = VeyronisPanelVariant,
                focusedContainerColor = VeyronisPanelVariant
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Description / Blurb Input
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(Strings.get("book_desc", language)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VeyronisPrimary,
                focusedTextColor = VeyronisTextPrimary,
                unfocusedTextColor = VeyronisTextPrimary,
                unfocusedContainerColor = VeyronisPanelVariant,
                focusedContainerColor = VeyronisPanelVariant
            ),
            shape = RoundedCornerShape(10.dp),
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Confirm Button
        Button(
            onClick = onConfirm,
            enabled = title.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("confirm_book_btn")
        ) {
            Text(
                text = if (language == AppLanguage.ARABIC) "حفظ الجزء ودخول الفصول 📖" else "Save Book & Manage Chapters 📖",
                color = VeyronisBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 5. SERIES SELECTION VIEW (Visually stunning cards with covers, titles, & descriptions)
// -------------------------------------------------------------------------------------------------
@Composable
fun SeriesSelectionView(
    seriesList: List<Series>,
    selectedSeriesId: Long?,
    language: AppLanguage,
    onSelectSeries: (Series) -> Unit,
    onCreateSeries: () -> Unit,
    onDeleteSeries: (Series) -> Unit,
    onNewProjectChoice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Strings.get("select_series_title", language),
                    style = MaterialTheme.typography.titleLarge,
                    color = VeyronisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = Strings.get("select_series_sub", language),
                    style = MaterialTheme.typography.bodySmall,
                    color = VeyronisTextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onNewProjectChoice,
                    colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_new_series_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = VeyronisBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.ARABIC) "مشروع جديد" else "New Project",
                        color = VeyronisBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (seriesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onNewProjectChoice() },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = VeyronisPrimary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = Strings.get("no_series_prompt", language),
                            style = MaterialTheme.typography.titleMedium,
                            color = VeyronisTextPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(seriesList) { series ->
                    val isSelected = series.id == selectedSeriesId
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) VeyronisPrimaryContainer.copy(alpha = 0.35f) else VeyronisPanel
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (isSelected) VeyronisPrimary else VeyronisSurfaceHighlight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSeries(series) }
                            .testTag("series_card_${series.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Cover
                                BookCoverCard(
                                    coverUri = series.coverUri,
                                    title = series.title,
                                    subtitle = "سلسلة",
                                    badgeText = "SERIES",
                                    badgeColor = VeyronisSecondary,
                                    width = 85.dp,
                                    height = 120.dp
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = VeyronisSecondaryContainer,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = Strings.get("step_series", language).uppercase(),
                                                color = VeyronisSecondary,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDeleteSeries(series) },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = VeyronisTextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Clear Series Name under cover
                                    Text(
                                        text = series.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = VeyronisTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (series.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = series.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = VeyronisTextSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.ARABIC) "دخول للأجزاء ⬅" else "Enter Books ➔",
                                            color = VeyronisPrimary,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
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
}

// -------------------------------------------------------------------------------------------------
// 6. BOOKS SELECTION VIEW (Volume cards with covers, names, and subtitles)
// -------------------------------------------------------------------------------------------------
@Composable
fun BooksSelectionView(
    series: Series?,
    books: List<Book>,
    selectedBookId: Long?,
    language: AppLanguage,
    onBack: () -> Unit,
    onSelectBook: (Book) -> Unit,
    onCreateBook: () -> Unit,
    onDeleteBook: (Book) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = VeyronisPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = Strings.get("select_book_title", language),
                        style = MaterialTheme.typography.titleLarge,
                        color = VeyronisTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${Strings.get("step_series", language)}: ${series?.title ?: "---"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = VeyronisTertiary
                    )
                }
            }

            Button(
                onClick = onCreateBook,
                colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_new_book_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = VeyronisBackground)
                Spacer(modifier = Modifier.width(6.dp))
                Text(Strings.get("create_book", language), color = VeyronisBackground, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (books.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onCreateBook() },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = VeyronisSecondary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = Strings.get("no_books_prompt", language),
                            style = MaterialTheme.typography.titleMedium,
                            color = VeyronisTextPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(books) { book ->
                    val isSelected = book.id == selectedBookId
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) VeyronisPrimaryContainer.copy(alpha = 0.35f) else VeyronisPanel
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (isSelected) VeyronisPrimary else VeyronisSurfaceHighlight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectBook(book) }
                            .testTag("book_card_${book.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Book Cover
                                BookCoverCard(
                                    coverUri = book.coverUri,
                                    title = book.title,
                                    subtitle = book.subtitle.ifBlank { "الجزء #${book.orderIndex + 1}" },
                                    badgeText = "VOL #${book.orderIndex + 1}",
                                    badgeColor = VeyronisPrimary,
                                    width = 85.dp,
                                    height = 120.dp
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
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
                                                text = "${Strings.get("step_books", language)} #${book.orderIndex + 1}",
                                                color = VeyronisPrimary,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDeleteBook(book) },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = VeyronisTextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Book Title clearly written
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = VeyronisTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (book.subtitle.isNotBlank()) {
                                        Text(
                                            text = book.subtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = VeyronisTertiary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (book.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = book.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = VeyronisTextSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.ARABIC) "دخول للفصول ⬅" else "Enter Chapters ➔",
                                            color = VeyronisPrimary,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
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
}

// -------------------------------------------------------------------------------------------------
// 7. CHAPTERS & SCENES VIEW (With direct write & visual hierarchy)
// -------------------------------------------------------------------------------------------------
@Composable
fun ChaptersSelectionView(
    book: Book?,
    series: Series?,
    chapters: List<Chapter>,
    allScenes: List<Scene>,
    selectedChapterId: Long?,
    selectedSceneId: Long?,
    language: AppLanguage,
    onBack: () -> Unit,
    onSelectChapter: (Chapter) -> Unit,
    onSelectScene: (Scene) -> Unit,
    onCreateChapter: () -> Unit,
    onCreateScene: (Chapter) -> Unit,
    onDeleteChapter: (Chapter) -> Unit,
    onDeleteScene: (Scene) -> Unit,
    onDirectWrite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Book Banner Header with Cover
        Card(
            colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = VeyronisPrimary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                BookCoverCard(
                    coverUri = book?.coverUri,
                    title = book?.title ?: "الكتاب",
                    subtitle = book?.subtitle,
                    badgeText = "CURRENT",
                    width = 65.dp,
                    height = 90.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book?.title ?: "---",
                        style = MaterialTheme.typography.titleMedium,
                        color = VeyronisTextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${series?.title ?: ""} • ${chapters.size} فصول",
                        style = MaterialTheme.typography.bodySmall,
                        color = VeyronisTertiary
                    )
                }

                Button(
                    onClick = onDirectWrite,
                    colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "كتابة فورية ✍️" else "Write ✍️",
                        color = VeyronisBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Strings.get("select_chapter_title", language),
                style = MaterialTheme.typography.titleMedium,
                color = VeyronisTextPrimary,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onCreateChapter,
                colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimaryContainer),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_new_chapter_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = VeyronisPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(Strings.get("create_chapter", language), color = VeyronisPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (chapters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onCreateChapter() },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryBooks,
                            contentDescription = null,
                            tint = VeyronisTertiary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = Strings.get("no_chapters_prompt", language),
                            style = MaterialTheme.typography.titleMedium,
                            color = VeyronisTextPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(chapters) { chapter ->
                    val scenes = allScenes.filter { it.chapterId == chapter.id }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, VeyronisSurfaceHighlight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = VeyronisTertiaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${Strings.get("step_chapters", language)} #${chapter.orderIndex + 1}",
                                            color = VeyronisTertiary,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = chapter.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = VeyronisTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { onCreateScene(chapter) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddCircleOutline,
                                            contentDescription = "Add Scene",
                                            tint = VeyronisPrimary
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteChapter(chapter) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Chapter",
                                            tint = VeyronisTextMuted
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Scenes List within Chapter
                            if (scenes.isEmpty()) {
                                Surface(
                                    color = VeyronisPanelVariant,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onCreateScene(chapter) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = null,
                                            tint = VeyronisPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = Strings.get("no_scenes_prompt", language),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = VeyronisPrimary
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    for (scene in scenes) {
                                        val isSceneSelected = scene.id == selectedSceneId
                                        Surface(
                                            color = if (isSceneSelected) VeyronisPrimaryContainer.copy(alpha = 0.6f) else VeyronisPanelVariant,
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, if (isSceneSelected) VeyronisPrimary else Color.Transparent),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onSelectScene(scene) }
                                                .testTag("scene_item_${scene.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.EditNote,
                                                        contentDescription = null,
                                                        tint = if (isSceneSelected) VeyronisPrimary else VeyronisTextSecondary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            text = scene.title,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = VeyronisTextPrimary,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            text = "${scene.wordCount} ${Strings.get("words_count", language)} • ${scene.status}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = VeyronisTextSecondary
                                                        )
                                                    }
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        color = VeyronisPrimary,
                                                        shape = RoundedCornerShape(6.dp),
                                                        modifier = Modifier.clickable { onSelectScene(scene) }
                                                    ) {
                                                        Text(
                                                            text = if (language == AppLanguage.ARABIC) "اكتب الآن ✍️" else "Write ✍️",
                                                            color = VeyronisBackground,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(6.dp))

                                                    IconButton(
                                                        onClick = { onDeleteScene(scene) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Delete Scene",
                                                            tint = VeyronisTextMuted,
                                                            modifier = Modifier.size(16.dp)
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
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 8. FOCUSED WRITER CANVAS
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusedEditorView(
    viewModel: VeyronisViewModel,
    currentSeries: Series?,
    currentBook: Book?,
    currentChapter: Chapter?,
    currentScene: Scene?,
    editorTitle: String,
    editorContent: String,
    words: Int,
    chars: Int,
    readingTimeMinutes: Int,
    isAutosaving: Boolean,
    lastSavedTime: Long?,
    searchVisible: Boolean,
    searchQuery: String,
    replaceQuery: String,
    recognizedTerms: List<TermOccurrence>,
    language: AppLanguage,
    onBackToHierarchy: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleSearch: () -> Unit,
    onSearchChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onReplaceOne: () -> Unit,
    onReplaceAll: () -> Unit,
    onOpenSnapshots: () -> Unit,
    onCreateSnapshot: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VeyronisBackground)
    ) {
        // Top Focused Bar
        Surface(
            color = VeyronisPanel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Navigation Back to Hierarchy
                    Button(
                        onClick = onBackToHierarchy,
                        colors = ButtonDefaults.buttonColors(containerColor = VeyronisPanelVariant),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("editor_back_to_hierarchy_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = VeyronisPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${currentChapter?.title ?: "الفصل"} / ${currentScene?.title ?: "المشهد"}",
                            color = VeyronisTextPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }

                    // Autosave Pill & Undo/Redo/Search
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
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
                                        .clip(CircleShape)
                                        .background(if (isAutosaving) VeyronisTertiary else VeyronisSuccess)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAutosaving) Strings.get("saving", language) else Strings.get("autosaved", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isAutosaving) VeyronisTertiary else VeyronisSuccess,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = onUndo, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = VeyronisTextPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onRedo, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = VeyronisTextPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onToggleSearch, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.FindReplace, contentDescription = "Find & Replace", tint = if (searchVisible) VeyronisPrimary else VeyronisTextPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onCreateSnapshot, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Snapshot", tint = VeyronisTertiary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onOpenSnapshots, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.History, contentDescription = "History", tint = VeyronisTextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Search/Replace Bar (if toggled)
                if (searchVisible) {
                    Surface(
                        color = VeyronisPanelVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchChange,
                                placeholder = { Text("بحث...", color = VeyronisTextMuted, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f).height(44.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VeyronisPrimary,
                                    focusedTextColor = VeyronisTextPrimary,
                                    unfocusedTextColor = VeyronisTextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = replaceQuery,
                                onValueChange = onReplaceChange,
                                placeholder = { Text("استبدال...", color = VeyronisTextMuted, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f).height(44.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VeyronisPrimary,
                                    focusedTextColor = VeyronisTextPrimary,
                                    unfocusedTextColor = VeyronisTextPrimary
                                )
                            )
                            Button(
                                onClick = onReplaceOne,
                                colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) { Text("استبدل", fontSize = 11.sp, color = VeyronisBackground) }
                            Button(
                                onClick = onReplaceAll,
                                colors = ButtonDefaults.buttonColors(containerColor = VeyronisSecondary),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) { Text("الكل", fontSize = 11.sp, color = VeyronisBackground) }
                        }
                    }
                }

                // Formatting Quick Actions Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToolbarFormatButton(label = "B", fontWeight = FontWeight.Bold, onClick = { onContentChange("$editorContent **نص عريض**") })
                    ToolbarFormatButton(label = "I", fontStyle = FontStyle.Italic, onClick = { onContentChange("$editorContent *نص مائل*") })
                    ToolbarFormatButton(label = "H1", fontWeight = FontWeight.Bold, onClick = { onContentChange("$editorContent\n# عنوان رئيسي\n") })
                    ToolbarFormatButton(label = "H2", fontWeight = FontWeight.Bold, onClick = { onContentChange("$editorContent\n## عنوان فرعي\n") })
                    ToolbarFormatButton(label = "— فاصل", onClick = { onContentChange("$editorContent\n\n* * *\n\n") })
                    ToolbarFormatButton(label = "« » حوار", onClick = { onContentChange("$editorContent « »") })
                }
            }
        }

        // Live Detected Lore Pills
        if (recognizedTerms.isNotEmpty()) {
            Surface(
                color = VeyronisPanelVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "المصطلحات المرصودة:",
                        style = MaterialTheme.typography.labelSmall,
                        color = VeyronisTextSecondary
                    )
                    for (term in recognizedTerms) {
                        Surface(
                            color = VeyronisPrimaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "🪐 ${term.matchedWord} (${term.term.category})",
                                color = VeyronisPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Scene Title Input
        OutlinedTextField(
            value = editorTitle,
            onValueChange = onTitleChange,
            placeholder = {
                Text(
                    text = Strings.get("scene_title", language),
                    style = MaterialTheme.typography.titleLarge,
                    color = VeyronisTextMuted,
                    fontWeight = FontWeight.Bold
                )
            },
            textStyle = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = VeyronisTextPrimary
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("scene_title_input")
        )

        // Main Text Editor Area
        OutlinedTextField(
            value = editorContent,
            onValueChange = onContentChange,
            placeholder = {
                Text(
                    text = Strings.get("start_writing_hint", language),
                    color = VeyronisTextMuted,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp)
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = VeyronisTextPrimary,
                fontFamily = FontFamily.Default,
                fontSize = 17.sp,
                lineHeight = 30.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .testTag("manuscript_content_editor")
        )

        // Bottom Live Word Count & Metrics Bar
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
                        text = "$words ${Strings.get("words_count", language)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = VeyronisPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$chars ${Strings.get("chars_count", language)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = VeyronisTextSecondary
                    )
                    Text(
                        text = "≈ $readingTimeMinutes ${Strings.get("reading_time", language)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = VeyronisTertiary
                    )
                }

                Text(
                    text = if (language == AppLanguage.ARABIC) "محرر فيرونيس الذكي" else "Veyronis Smart Editor",
                    style = MaterialTheme.typography.labelSmall,
                    color = VeyronisTextMuted
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// COMPONENTS & TOOLBAR
// -------------------------------------------------------------------------------------------------

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
