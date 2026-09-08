package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.VeyronisRepository
import com.example.domain.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppSection(val title: String) {
    DASHBOARD("Dashboard"),
    WRITER("Writer"),
    SETTINGS("Settings"),
    CHARACTERS("Characters"),
    CODEX("Codex"),
    TIMELINE("Timeline"),
    EVENTS("Events"),
    GRAPHS("Graphs"),
    WORLD_RULES("World Rules"),
    SEARCH("Search");

    fun getLocalizedTitle(language: AppLanguage): String {
        return when (this) {
            DASHBOARD -> Strings.get("nav_dashboard", language)
            WRITER -> Strings.get("nav_writer", language)
            SETTINGS -> Strings.get("nav_settings", language)
            CHARACTERS -> Strings.get("nav_characters", language)
            CODEX -> Strings.get("nav_codex", language)
            TIMELINE -> Strings.get("nav_timeline", language)
            EVENTS -> Strings.get("nav_events", language)
            GRAPHS -> Strings.get("nav_graphs", language)
            WORLD_RULES -> Strings.get("nav_rules", language)
            SEARCH -> Strings.get("nav_search", language)
        }
    }
}

class VeyronisViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = VeyronisRepository(database)

    val centralEntityEngine = CentralEntityEngine()
    val consistencyEngine = TemporalConsistencyEngine()
    val lexiconEngine = LexiconEngine()
    val exportEngine = ExportEngine()
    val syncEngine = SyncEngine()
    val updateEngine = UpdateEngine(currentVersion = "1.0.1")

    // Language State (Defaults to Arabic as requested by user, with instant toggling)
    private val _appLanguage = MutableStateFlow(AppLanguage.ARABIC)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _appLanguage.value = language
    }

    fun toggleLanguage() {
        _appLanguage.value = if (_appLanguage.value == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
    }

    // Navigation
    private val _currentSection = MutableStateFlow(AppSection.DASHBOARD)
    val currentSection: StateFlow<AppSection> = _currentSection.asStateFlow()

    fun navigateTo(section: AppSection) {
        _currentSection.value = section
    }

    // Hierarchy State - In-memory hot cache for instant navigation
    val allSeries = repository.allSeries.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allBooks = repository.allBooks.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allChapters = repository.allChapters.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allScenes = repository.allScenes.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedSeriesId = MutableStateFlow<Long?>(null)
    val selectedSeriesId: StateFlow<Long?> = _selectedSeriesId.asStateFlow()

    private val _selectedBookId = MutableStateFlow<Long?>(null)
    val selectedBookId: StateFlow<Long?> = _selectedBookId.asStateFlow()

    private val _selectedChapterId = MutableStateFlow<Long?>(null)
    val selectedChapterId: StateFlow<Long?> = _selectedChapterId.asStateFlow()

    private val _selectedSceneId = MutableStateFlow<Long?>(null)
    val selectedSceneId: StateFlow<Long?> = _selectedSceneId.asStateFlow()

    // Character, Codex, Lexicon - Hot in-memory caches
    val allCharacters = repository.allCharacters.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allRelationships = repository.allRelationships.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allCodexEntries = repository.allCodexEntries.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allLexiconTerms = repository.allLexiconTerms.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Timeline, World Rules, Events, Causal
    val allTimelines = repository.allTimelines.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allWorldRules = repository.allWorldRules.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allStoryEvents = repository.allStoryEvents.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allCausalEdges = repository.allCausalEdges.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Universal Entities: Locations, Decisions, Universal Relationships
    val allLocations = repository.allLocations.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allDecisions = repository.allDecisions.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allUniversalRelationships = repository.allUniversalRelationships.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Targeted Navigation & Deep Inspection State
    private val _inspectedEntity = MutableStateFlow<Pair<EntityType, Long>?>(null)
    val inspectedEntity: StateFlow<Pair<EntityType, Long>?> = _inspectedEntity.asStateFlow()

    fun inspectEntity(type: EntityType, id: Long) {
        _inspectedEntity.value = Pair(type, id)
    }

    fun clearInspectedEntity() {
        _inspectedEntity.value = null
    }

    // Consistency Warnings (Combine data sources)
    val temporalWarnings: StateFlow<List<TemporalWarning>> = combine(
        combine(allCharacters, allStoryEvents, allScenes) { c, e, s -> Triple(c, e, s) },
        combine(allTimelines, allWorldRules, allCausalEdges) { t, w, ed -> Triple(t, w, ed) },
        combine(allDecisions, allUniversalRelationships) { d, r -> Pair(d, r) }
    ) { (chars, evs, scs), (tls, rules, edges), (decisions, rels) ->
        consistencyEngine.analyzeContinuity(chars, evs, scs, tls, rules, edges, decisions, rels)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Editor state
    private val _currentEditingScene = MutableStateFlow<Scene?>(null)
    val currentEditingScene: StateFlow<Scene?> = _currentEditingScene.asStateFlow()

    private val _editorContent = MutableStateFlow("")
    val editorContent: StateFlow<String> = _editorContent.asStateFlow()

    private val _editorTitle = MutableStateFlow("")
    val editorTitle: StateFlow<String> = _editorTitle.asStateFlow()

    private val _isAutosaving = MutableStateFlow(false)
    val isAutosaving: StateFlow<Boolean> = _isAutosaving.asStateFlow()

    private val _lastSavedTime = MutableStateFlow(System.currentTimeMillis())
    val lastSavedTime: StateFlow<Long> = _lastSavedTime.asStateFlow()

    // Smart Context Panel in Editor: Live Detected Entity Matches & Suggestions
    val currentSceneDetectedMatches: StateFlow<List<DetectedEntityMatch>> = combine(
        combine(_editorContent, allCharacters, allLocations) { content, chars, locs -> Triple(content, chars, locs) },
        combine(allCodexEntries, allLexiconTerms, allStoryEvents) { codex, lexicon, events -> Triple(codex, lexicon, events) }
    ) { (content, chars, locs), (codex, lexicon, events) ->
        centralEntityEngine.scanTextForEntities(
            text = content,
            characters = chars,
            locations = locs,
            codexEntries = codex,
            lexiconTerms = lexicon,
            events = events
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSceneSuggestions: StateFlow<List<UniversalRelationship>> = combine(
        _currentEditingScene, currentSceneDetectedMatches, allUniversalRelationships
    ) { scene, matches, rels ->
        if (scene == null) emptyList()
        else centralEntityEngine.generateSuggestionsForScene(scene, matches, rels)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Universe-wide Graph Data
    val universeGraphData: StateFlow<UniverseGraphData> = combine(
        combine(allCharacters, allLocations, allStoryEvents) { c, l, e -> Triple(c, l, e) },
        combine(allCodexEntries, allUniversalRelationships, allCausalEdges, allRelationships) { cd, ur, ce, cr ->
            Tuple4(cd, ur, ce, cr)
        }
    ) { (chars, locs, evts), (codex, ur, ce, cr) ->
        centralEntityEngine.buildUniverseGraphData(
            characters = chars,
            locations = locs,
            events = evts,
            codexEntries = codex,
            universalRelationships = ur,
            causalEdges = ce,
            characterRelationships = cr
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UniverseGraphData(emptyList(), emptyList()))


    // Undo / Redo stacks
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()

    // Search and replace inside editor
    private val _editorSearchQuery = MutableStateFlow("")
    val editorSearchQuery: StateFlow<String> = _editorSearchQuery.asStateFlow()

    private val _editorReplaceQuery = MutableStateFlow("")
    val editorReplaceQuery: StateFlow<String> = _editorReplaceQuery.asStateFlow()

    private val _editorSearchVisible = MutableStateFlow(false)
    val editorSearchVisible: StateFlow<Boolean> = _editorSearchVisible.asStateFlow()

    private var autosaveJob: Job? = null

    // Scene Versions
    private val _sceneVersions = MutableStateFlow<List<SceneVersion>>(emptyList())
    val sceneVersions: StateFlow<List<SceneVersion>> = _sceneVersions.asStateFlow()

    // Global Search Query
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    init {
        // Auto-select first series, book, chapter, scene once loaded
        viewModelScope.launch {
            allSeries.collect { series ->
                if (_selectedSeriesId.value == null && series.isNotEmpty()) {
                    _selectedSeriesId.value = series.first().id
                }
            }
        }
        viewModelScope.launch {
            allBooks.collect { books ->
                val currSeriesId = _selectedSeriesId.value
                val matchedBooks = books.filter { it.seriesId == currSeriesId }
                if (_selectedBookId.value == null && matchedBooks.isNotEmpty()) {
                    _selectedBookId.value = matchedBooks.first().id
                }
            }
        }
        viewModelScope.launch {
            allChapters.collect { chapters ->
                val currBookId = _selectedBookId.value
                val matchedChapters = chapters.filter { it.bookId == currBookId }
                if (_selectedChapterId.value == null && matchedChapters.isNotEmpty()) {
                    _selectedChapterId.value = matchedChapters.first().id
                }
            }
        }
        viewModelScope.launch {
            allScenes.collect { scenes ->
                val currChId = _selectedChapterId.value
                val matchedScenes = scenes.filter { it.chapterId == currChId }
                if (_selectedSceneId.value == null && matchedScenes.isNotEmpty()) {
                    selectScene(matchedScenes.first())
                }
            }
        }
    }

    fun selectSeries(seriesId: Long) {
        _selectedSeriesId.value = seriesId
        val books = allBooks.value.filter { it.seriesId == seriesId }
        _selectedBookId.value = books.firstOrNull()?.id
        val chapters = allChapters.value.filter { it.bookId == _selectedBookId.value }
        _selectedChapterId.value = chapters.firstOrNull()?.id
        val scenes = allScenes.value.filter { it.chapterId == _selectedChapterId.value }
        scenes.firstOrNull()?.let { selectScene(it) }
    }

    fun selectBook(bookId: Long) {
        _selectedBookId.value = bookId
        val chapters = allChapters.value.filter { it.bookId == bookId }
        _selectedChapterId.value = chapters.firstOrNull()?.id
        val scenes = allScenes.value.filter { it.chapterId == _selectedChapterId.value }
        scenes.firstOrNull()?.let { selectScene(it) }
    }

    fun selectChapter(chapterId: Long) {
        _selectedChapterId.value = chapterId
        val scenes = allScenes.value.filter { it.chapterId == chapterId }
        scenes.firstOrNull()?.let { selectScene(it) }
    }

    fun selectScene(scene: Scene) {
        _selectedSceneId.value = scene.id
        _currentEditingScene.value = scene
        _editorTitle.value = scene.title
        _editorContent.value = scene.content
        undoStack.clear()
        redoStack.clear()
        loadSceneVersions(scene.id)
    }

    private fun loadSceneVersions(sceneId: Long) {
        viewModelScope.launch {
            repository.getSceneVersions(sceneId).collect {
                _sceneVersions.value = it
            }
        }
    }

    fun onEditorContentChange(newContent: String) {
        if (_editorContent.value != newContent) {
            undoStack.add(_editorContent.value)
            if (undoStack.size > 50) undoStack.removeAt(0)
            redoStack.clear()
            _editorContent.value = newContent
            scheduleAutosave()
        }
    }

    fun onEditorTitleChange(newTitle: String) {
        _editorTitle.value = newTitle
        scheduleAutosave()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(_editorContent.value)
            _editorContent.value = previous
            scheduleAutosave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(_editorContent.value)
            _editorContent.value = next
            scheduleAutosave()
        }
    }

    fun toggleEditorSearch() {
        _editorSearchVisible.value = !_editorSearchVisible.value
    }

    fun setEditorSearchQuery(query: String) {
        _editorSearchQuery.value = query
    }

    fun setEditorReplaceQuery(query: String) {
        _editorReplaceQuery.value = query
    }

    fun executeReplaceOne() {
        val query = _editorSearchQuery.value
        val replace = _editorReplaceQuery.value
        if (query.isNotEmpty() && _editorContent.value.contains(query, ignoreCase = true)) {
            val idx = _editorContent.value.indexOf(query, ignoreCase = true)
            if (idx >= 0) {
                val updated = _editorContent.value.substring(0, idx) + replace + _editorContent.value.substring(idx + query.length)
                onEditorContentChange(updated)
            }
        }
    }

    fun executeReplaceAll() {
        val query = _editorSearchQuery.value
        val replace = _editorReplaceQuery.value
        if (query.isNotEmpty()) {
            val updated = _editorContent.value.replace(query, replace, ignoreCase = true)
            onEditorContentChange(updated)
        }
    }

    private fun scheduleAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            _isAutosaving.value = true
            delay(800) // Debounce 800ms
            saveCurrentSceneInternal()
            _isAutosaving.value = false
            _lastSavedTime.value = System.currentTimeMillis()
        }
    }

    private suspend fun saveCurrentSceneInternal() {
        val scene = _currentEditingScene.value ?: return
        val text = _editorContent.value
        val words = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
        val chars = text.length

        val updated = scene.copy(
            title = _editorTitle.value.ifBlank { "Untitled Scene" },
            content = text,
            wordCount = words,
            characterCount = chars,
            updatedAt = System.currentTimeMillis()
        )
        repository.updateScene(updated)
        _currentEditingScene.value = updated
    }

    fun createManualSnapshot(summary: String) {
        viewModelScope.launch {
            saveCurrentSceneInternal()
            val scene = _currentEditingScene.value ?: return@launch
            repository.createSceneSnapshot(scene, summary)
            loadSceneVersions(scene.id)
        }
    }

    fun restoreVersion(version: SceneVersion) {
        undoStack.add(_editorContent.value)
        _editorContent.value = version.content
        _editorTitle.value = version.title
        scheduleAutosave()
    }

    // Hierarchy CRUD
    fun createSeries(title: String, desc: String = "", coverUri: String = "") {
        viewModelScope.launch {
            val id = repository.insertSeries(Series(title = title, description = desc, coverUri = coverUri))
            selectSeries(id)
        }
    }

    fun updateSeries(series: Series) {
        viewModelScope.launch {
            repository.updateSeries(series)
        }
    }

    fun deleteSeries(series: Series) {
        viewModelScope.launch {
            repository.deleteSeries(series)
        }
    }

    fun createBook(title: String, subtitle: String = "", desc: String = "", coverUri: String = "", targetWordCount: Int = 80000, status: String = "In Progress") {
        val seriesId = _selectedSeriesId.value ?: return
        viewModelScope.launch {
            val count = allBooks.value.filter { it.seriesId == seriesId }.size
            val id = repository.insertBook(
                Book(
                    seriesId = seriesId,
                    title = title,
                    subtitle = subtitle,
                    description = desc,
                    coverUri = coverUri,
                    targetWordCount = targetWordCount,
                    status = status,
                    orderIndex = count
                )
            )
            selectBook(id)
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            repository.updateBook(book)
        }
    }

    // Direct standalone novel creation helper
    fun createStandaloneNovel(title: String, desc: String = "", coverUri: String = "", targetWordCount: Int = 80000) {
        viewModelScope.launch {
            // Check if there's a standalone series container or create one with the novel name
            val seriesId = repository.insertSeries(Series(title = title, description = desc, coverUri = coverUri))
            selectSeries(seriesId)
            val bookId = repository.insertBook(
                Book(
                    seriesId = seriesId,
                    title = title,
                    subtitle = "رواية مفردة / Standalone Novel",
                    description = desc,
                    coverUri = coverUri,
                    targetWordCount = targetWordCount,
                    status = "In Progress",
                    orderIndex = 0
                )
            )
            selectBook(bookId)
            // Create default Chapter 1 and Scene 1 to let them write right away
            val chapterId = repository.insertChapter(
                Chapter(
                    bookId = bookId,
                    title = "الفصل الأول",
                    summary = "بداية الرواية",
                    orderIndex = 0
                )
            )
            selectChapter(chapterId)
            val scene = Scene(
                chapterId = chapterId,
                title = "المشهد 1",
                content = "",
                orderIndex = 0
            )
            val sceneId = repository.insertScene(scene)
            selectScene(scene.copy(id = sceneId))
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            repository.deleteBook(book)
        }
    }

    fun createChapter(title: String, summary: String = "") {
        val bookId = _selectedBookId.value ?: return
        viewModelScope.launch {
            val count = allChapters.value.filter { it.bookId == bookId }.size
            val id = repository.insertChapter(
                Chapter(
                    bookId = bookId,
                    title = title,
                    summary = summary,
                    orderIndex = count
                )
            )
            selectChapter(id)
        }
    }

    fun deleteChapter(chapter: Chapter) {
        viewModelScope.launch {
            repository.deleteChapter(chapter)
        }
    }

    fun createScene(title: String) {
        val chapterId = _selectedChapterId.value ?: return
        viewModelScope.launch {
            val count = allScenes.value.filter { it.chapterId == chapterId }.size
            val scene = Scene(
                chapterId = chapterId,
                title = title,
                content = "",
                orderIndex = count
            )
            val id = repository.insertScene(scene)
            selectScene(scene.copy(id = id))
        }
    }

    fun duplicateScene(scene: Scene) {
        viewModelScope.launch {
            val newScene = scene.copy(
                id = 0,
                title = "${scene.title} (Copy)",
                orderIndex = scene.orderIndex + 1,
                createdAt = System.currentTimeMillis()
            )
            val id = repository.insertScene(newScene)
            selectScene(newScene.copy(id = id))
        }
    }

    fun deleteScene(scene: Scene) {
        viewModelScope.launch {
            repository.deleteScene(scene)
            if (_selectedSceneId.value == scene.id) {
                val remaining = allScenes.value.filter { it.chapterId == scene.chapterId && it.id != scene.id }
                remaining.firstOrNull()?.let { selectScene(it) }
            }
        }
    }

    // Character CRUD
    fun saveCharacter(character: Character) {
        viewModelScope.launch {
            if (character.id == 0L) {
                repository.insertCharacter(character)
            } else {
                repository.updateCharacter(character)
            }
        }
    }

    fun deleteCharacter(character: Character) {
        viewModelScope.launch {
            repository.deleteCharacter(character)
        }
    }

    fun addRelationship(sourceId: Long, targetId: Long, type: String, notes: String = "") {
        viewModelScope.launch {
            repository.insertRelationship(
                CharacterRelationship(
                    sourceCharacterId = sourceId,
                    targetCharacterId = targetId,
                    relationshipType = type,
                    notes = notes
                )
            )
        }
    }

    fun deleteRelationship(relationship: CharacterRelationship) {
        viewModelScope.launch {
            repository.deleteRelationship(relationship)
        }
    }

    // Codex CRUD
    fun saveCodexEntry(entry: CodexEntry) {
        viewModelScope.launch {
            if (entry.id == 0L) {
                repository.insertCodexEntry(entry)
            } else {
                repository.updateCodexEntry(entry)
            }
        }
    }

    fun deleteCodexEntry(entry: CodexEntry) {
        viewModelScope.launch {
            repository.deleteCodexEntry(entry)
        }
    }

    // Lexicon CRUD
    fun saveLexiconTerm(term: LexiconTerm) {
        viewModelScope.launch {
            if (term.id == 0L) {
                repository.insertLexiconTerm(term)
            } else {
                repository.updateLexiconTerm(term)
            }
        }
    }

    fun deleteLexiconTerm(term: LexiconTerm) {
        viewModelScope.launch {
            repository.deleteLexiconTerm(term)
        }
    }

    // Timelines & World Rules
    fun saveTimeline(timeline: Timeline) {
        viewModelScope.launch {
            if (timeline.id == 0L) {
                repository.insertTimeline(timeline)
            } else {
                repository.updateTimeline(timeline)
            }
        }
    }

    fun deleteTimeline(timeline: Timeline) {
        viewModelScope.launch {
            repository.deleteTimeline(timeline)
        }
    }

    fun saveWorldRule(rule: WorldRule) {
        viewModelScope.launch {
            if (rule.id == 0L) {
                repository.insertWorldRule(rule)
            } else {
                repository.updateWorldRule(rule)
            }
        }
    }

    fun deleteWorldRule(rule: WorldRule) {
        viewModelScope.launch {
            repository.deleteWorldRule(rule)
        }
    }

    // Events & Causal Graph
    fun saveStoryEvent(event: StoryEvent) {
        viewModelScope.launch {
            if (event.id == 0L) {
                repository.insertStoryEvent(event)
            } else {
                repository.updateStoryEvent(event)
            }
        }
    }

    fun updateEventCanvasPosition(eventId: Long, posX: Float, posY: Float) {
        viewModelScope.launch {
            val event = allStoryEvents.value.find { it.id == eventId } ?: return@launch
            repository.updateStoryEvent(event.copy(canvasPosX = posX, canvasPosY = posY))
        }
    }

    fun deleteStoryEvent(event: StoryEvent) {
        viewModelScope.launch {
            repository.deleteStoryEvent(event)
        }
    }

    fun addCausalEdge(sourceId: Long, targetId: Long, relationType: String) {
        viewModelScope.launch {
            repository.insertCausalEdge(
                CausalEdge(
                    sourceEventId = sourceId,
                    targetEventId = targetId,
                    relationshipType = relationType
                )
            )
        }
    }

    fun deleteCausalEdge(edge: CausalEdge) {
        viewModelScope.launch {
            repository.deleteCausalEdge(edge)
        }
    }

    // Universal Relationships Operations
    fun confirmUniversalRelationship(id: Long) {
        viewModelScope.launch {
            repository.confirmUniversalRelationship(id)
        }
    }

    fun confirmSuggestion(suggestion: UniversalRelationship) {
        viewModelScope.launch {
            if (suggestion.id == 0L) {
                repository.insertUniversalRelationship(suggestion.copy(status = "Confirmed"))
            } else {
                repository.confirmUniversalRelationship(suggestion.id)
            }
        }
    }

    fun dismissUniversalRelationship(id: Long) {
        viewModelScope.launch {
            repository.dismissUniversalRelationship(id)
        }
    }

    fun addUniversalRelationship(
        sourceType: EntityType,
        sourceId: Long,
        targetType: EntityType,
        targetId: Long,
        relationType: String,
        notes: String = "",
        intensity: Int = 3
    ) {
        viewModelScope.launch {
            repository.insertUniversalRelationship(
                UniversalRelationship(
                    sourceType = sourceType.name,
                    sourceId = sourceId,
                    targetType = targetType.name,
                    targetId = targetId,
                    relationType = relationType,
                    status = "Confirmed",
                    notes = notes,
                    intensity = intensity
                )
            )
        }
    }

    fun deleteUniversalRelationship(rel: UniversalRelationship) {
        viewModelScope.launch {
            repository.deleteUniversalRelationship(rel)
        }
    }

    // Location CRUD
    fun saveLocation(location: StoryLocation) {
        viewModelScope.launch {
            if (location.id == 0L) {
                repository.insertLocation(location)
            } else {
                repository.updateLocation(location)
            }
        }
    }

    fun deleteLocation(location: StoryLocation) {
        viewModelScope.launch {
            repository.deleteLocation(location)
        }
    }

    // Decision CRUD
    fun saveDecision(decision: StoryDecision) {
        viewModelScope.launch {
            if (decision.id == 0L) {
                repository.insertDecision(decision)
            } else {
                repository.updateDecision(decision)
            }
        }
    }

    fun deleteDecision(decision: StoryDecision) {
        viewModelScope.launch {
            repository.deleteDecision(decision)
        }
    }

    // Rename Entity with Reference Propagation
    fun renameCharacter(character: Character, newName: String) {
        viewModelScope.launch {
            val oldName = character.name
            val updated = character.copy(name = newName)
            repository.updateCharacter(updated)

            // Update matching Lexicon terms if any
            val lexiconMatch = allLexiconTerms.value.find { it.term.equals(oldName, ignoreCase = true) }
            if (lexiconMatch != null) {
                repository.updateLexiconTerm(lexiconMatch.copy(term = newName))
            }
        }
    }

    fun renameLocation(location: StoryLocation, newName: String) {
        viewModelScope.launch {
            val oldName = location.name
            val updated = location.copy(name = newName)
            repository.updateLocation(updated)

            // Update matching Lexicon terms if any
            val lexiconMatch = allLexiconTerms.value.find { it.term.equals(oldName, ignoreCase = true) }
            if (lexiconMatch != null) {
                repository.updateLexiconTerm(lexiconMatch.copy(term = newName))
            }
        }
    }

    // Cross-Tool Navigation: seamlessly jumps to the appropriate tool with the entity selected
    fun navigateToEntity(entityType: EntityType, entityId: Long) {
        when (entityType) {
            EntityType.CHARACTER -> {
                inspectEntity(EntityType.CHARACTER, entityId)
                navigateTo(AppSection.CHARACTERS)
            }
            EntityType.LOCATION -> {
                inspectEntity(EntityType.LOCATION, entityId)
                navigateTo(AppSection.CODEX)
            }
            EntityType.EVENT -> {
                inspectEntity(EntityType.EVENT, entityId)
                navigateTo(AppSection.EVENTS)
            }
            EntityType.SCENE -> {
                val scene = allScenes.value.find { it.id == entityId }
                if (scene != null) {
                    val chapter = allChapters.value.find { it.id == scene.chapterId }
                    if (chapter != null) {
                        val book = allBooks.value.find { it.id == chapter.bookId }
                        if (book != null) {
                            _selectedSeriesId.value = book.seriesId
                            _selectedBookId.value = book.id
                            _selectedChapterId.value = chapter.id
                        }
                    }
                    selectScene(scene)
                    navigateTo(AppSection.WRITER)
                }
            }
            EntityType.CHAPTER -> {
                val chapter = allChapters.value.find { it.id == entityId }
                if (chapter != null) {
                    selectChapter(chapter.id)
                    navigateTo(AppSection.WRITER)
                }
            }
            EntityType.BOOK -> {
                val book = allBooks.value.find { it.id == entityId }
                if (book != null) {
                    selectBook(book.id)
                    navigateTo(AppSection.WRITER)
                }
            }
            EntityType.SERIES -> {
                selectSeries(entityId)
                navigateTo(AppSection.WRITER)
            }
            EntityType.CODEX -> {
                inspectEntity(EntityType.CODEX, entityId)
                navigateTo(AppSection.CODEX)
            }
            EntityType.LEXICON -> {
                inspectEntity(EntityType.LEXICON, entityId)
                navigateTo(AppSection.CODEX)
            }
            EntityType.TIMELINE -> {
                inspectEntity(EntityType.TIMELINE, entityId)
                navigateTo(AppSection.TIMELINE)
            }
            EntityType.WORLD_RULE -> {
                inspectEntity(EntityType.WORLD_RULE, entityId)
                navigateTo(AppSection.WORLD_RULES)
            }
            EntityType.DECISION -> {
                inspectEntity(EntityType.DECISION, entityId)
                navigateTo(AppSection.EVENTS)
            }
        }
    }

    // Search
    fun setGlobalSearchQuery(query: String) {
        _globalSearchQuery.value = query
    }

    // Cloud Sync
    fun triggerSync() {
        viewModelScope.launch {
            val logs = repository.unsyncedLogs.first()
            syncEngine.performSync(logs) { ids ->
                repository.markLogsSynced(ids)
            }
        }
    }

    // Export helper
    fun executeExport(options: ExportOptions): ExportResult? {
        val series = allSeries.value.find { it.id == _selectedSeriesId.value }
        val book = allBooks.value.find { it.id == _selectedBookId.value } ?: allBooks.value.firstOrNull() ?: return null
        val chapters = allChapters.value.filter { it.bookId == book.id }
        val scenesByCh = mutableMapOf<Long, List<Scene>>()
        for (ch in chapters) {
            scenesByCh[ch.id] = allScenes.value.filter { it.chapterId == ch.id }
        }
        return exportEngine.exportBook(series, book, chapters, scenesByCh, options)
    }

    // Purge all sample and demo items from the universe
    fun purgeSampleData(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearAllSampleData()
            _selectedSeriesId.value = null
            _selectedBookId.value = null
            _selectedChapterId.value = null
            _selectedSceneId.value = null
            _currentEditingScene.value = null
            _editorTitle.value = ""
            _editorContent.value = ""
            onComplete()
        }
    }
}
