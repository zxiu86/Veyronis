package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class VeyronisRepository(private val database: AppDatabase) {
    // Series & Hierarchy
    val allSeries: Flow<List<Series>> = database.seriesDao().getAllSeries()
    val allBooks: Flow<List<Book>> = database.bookDao().getAllBooks()
    val allChapters: Flow<List<Chapter>> = database.chapterDao().getAllChapters()
    val allScenes: Flow<List<Scene>> = database.sceneDao().getAllScenes()

    fun getBooksForSeries(seriesId: Long): Flow<List<Book>> = database.bookDao().getBooksForSeries(seriesId)
    fun getChaptersForBook(bookId: Long): Flow<List<Chapter>> = database.chapterDao().getChaptersForBook(bookId)
    fun getScenesForChapter(chapterId: Long): Flow<List<Scene>> = database.sceneDao().getScenesForChapter(chapterId)
    fun observeScene(sceneId: Long): Flow<Scene?> = database.sceneDao().observeSceneById(sceneId)

    suspend fun getSceneById(sceneId: Long): Scene? = database.sceneDao().getSceneById(sceneId)
    suspend fun insertSeries(series: Series): Long = database.seriesDao().insertSeries(series)
    suspend fun updateSeries(series: Series) = database.seriesDao().updateSeries(series)
    suspend fun deleteSeries(series: Series) = database.seriesDao().deleteSeries(series)

    suspend fun insertBook(book: Book): Long = database.bookDao().insertBook(book)
    suspend fun updateBook(book: Book) = database.bookDao().updateBook(book)
    suspend fun deleteBook(book: Book) = database.bookDao().deleteBook(book)

    suspend fun insertChapter(chapter: Chapter): Long = database.chapterDao().insertChapter(chapter)
    suspend fun updateChapter(chapter: Chapter) = database.chapterDao().updateChapter(chapter)
    suspend fun deleteChapter(chapter: Chapter) = database.chapterDao().deleteChapter(chapter)

    suspend fun insertScene(scene: Scene): Long = database.sceneDao().insertScene(scene)
    suspend fun updateScene(scene: Scene) = database.sceneDao().updateScene(scene)
    suspend fun deleteScene(scene: Scene) = database.sceneDao().deleteScene(scene)

    // Version Snapshots
    fun getSceneVersions(sceneId: Long): Flow<List<SceneVersion>> = database.sceneVersionDao().getVersionsForScene(sceneId)
    suspend fun createSceneSnapshot(scene: Scene, summary: String = "Manual Snapshot") {
        database.sceneVersionDao().insertVersion(
            SceneVersion(
                sceneId = scene.id,
                title = scene.title,
                content = scene.content,
                wordCount = scene.wordCount,
                changeSummary = summary
            )
        )
    }

    // Characters
    val allCharacters: Flow<List<Character>> = database.characterDao().getAllCharacters()
    val allRelationships: Flow<List<CharacterRelationship>> = database.characterRelationshipDao().getAllRelationships()

    suspend fun getCharacterById(id: Long): Character? = database.characterDao().getCharacterById(id)
    suspend fun insertCharacter(character: Character): Long = database.characterDao().insertCharacter(character)
    suspend fun updateCharacter(character: Character) = database.characterDao().updateCharacter(character)
    suspend fun deleteCharacter(character: Character) {
        database.characterDao().deleteCharacter(character)
        database.universalRelationshipDao().deleteAllForEntity(EntityType.CHARACTER.name, character.id)
    }

    suspend fun insertRelationship(relationship: CharacterRelationship): Long = database.characterRelationshipDao().insertRelationship(relationship)
    suspend fun deleteRelationship(relationship: CharacterRelationship) = database.characterRelationshipDao().deleteRelationship(relationship)
    suspend fun deleteRelationshipById(id: Long) = database.characterRelationshipDao().deleteRelationshipById(id)

    // Codex
    val allCodexEntries: Flow<List<CodexEntry>> = database.codexDao().getAllEntries()
    fun getCodexByCategory(category: String): Flow<List<CodexEntry>> = database.codexDao().getEntriesByCategory(category)
    suspend fun insertCodexEntry(entry: CodexEntry): Long = database.codexDao().insertEntry(entry)
    suspend fun updateCodexEntry(entry: CodexEntry) = database.codexDao().updateEntry(entry)
    suspend fun deleteCodexEntry(entry: CodexEntry) {
        database.codexDao().deleteEntry(entry)
        database.universalRelationshipDao().deleteAllForEntity(EntityType.CODEX.name, entry.id)
    }

    // Lexicon
    val allLexiconTerms: Flow<List<LexiconTerm>> = database.lexiconDao().getAllTerms()
    suspend fun insertLexiconTerm(term: LexiconTerm): Long = database.lexiconDao().insertTerm(term)
    suspend fun updateLexiconTerm(term: LexiconTerm) = database.lexiconDao().updateTerm(term)
    suspend fun deleteLexiconTerm(term: LexiconTerm) = database.lexiconDao().deleteTerm(term)

    // Timelines & World Rules
    val allTimelines: Flow<List<Timeline>> = database.timelineDao().getAllTimelines()
    suspend fun insertTimeline(timeline: Timeline): Long = database.timelineDao().insertTimeline(timeline)
    suspend fun updateTimeline(timeline: Timeline) = database.timelineDao().updateTimeline(timeline)
    suspend fun deleteTimeline(timeline: Timeline) = database.timelineDao().deleteTimeline(timeline)

    val allWorldRules: Flow<List<WorldRule>> = database.worldRuleDao().getAllRules()
    suspend fun insertWorldRule(rule: WorldRule): Long = database.worldRuleDao().insertRule(rule)
    suspend fun updateWorldRule(rule: WorldRule) = database.worldRuleDao().updateRule(rule)
    suspend fun deleteWorldRule(rule: WorldRule) = database.worldRuleDao().deleteRule(rule)

    // Events & Causal Graph
    val allStoryEvents: Flow<List<StoryEvent>> = database.storyEventDao().getAllEvents()
    val allCausalEdges: Flow<List<CausalEdge>> = database.causalEdgeDao().getAllEdges()

    suspend fun insertStoryEvent(event: StoryEvent): Long = database.storyEventDao().insertEvent(event)
    suspend fun updateStoryEvent(event: StoryEvent) = database.storyEventDao().updateEvent(event)
    suspend fun deleteStoryEvent(event: StoryEvent) {
        database.storyEventDao().deleteEvent(event)
        database.universalRelationshipDao().deleteAllForEntity(EntityType.EVENT.name, event.id)
    }

    suspend fun insertCausalEdge(edge: CausalEdge): Long = database.causalEdgeDao().insertEdge(edge)
    suspend fun deleteCausalEdge(edge: CausalEdge) = database.causalEdgeDao().deleteEdge(edge)
    suspend fun deleteCausalEdgeById(id: Long) = database.causalEdgeDao().deleteEdgeById(id)

    // Universal Relationships
    val allUniversalRelationships: Flow<List<UniversalRelationship>> = database.universalRelationshipDao().getAllRelationships()
    fun getRelationshipsForEntity(entityType: EntityType, entityId: Long): Flow<List<UniversalRelationship>> =
        database.universalRelationshipDao().getRelationshipsForEntity(entityType.name, entityId)
    
    suspend fun insertUniversalRelationship(rel: UniversalRelationship): Long =
        database.universalRelationshipDao().insertRelationship(rel)

    suspend fun insertUniversalRelationships(rels: List<UniversalRelationship>): List<Long> =
        database.universalRelationshipDao().insertRelationships(rels)

    suspend fun updateUniversalRelationship(rel: UniversalRelationship) =
        database.universalRelationshipDao().updateRelationship(rel)

    suspend fun deleteUniversalRelationship(rel: UniversalRelationship) =
        database.universalRelationshipDao().deleteRelationship(rel)

    suspend fun deleteUniversalRelationshipById(id: Long) =
        database.universalRelationshipDao().deleteRelationshipById(id)

    suspend fun confirmUniversalRelationship(id: Long) =
        database.universalRelationshipDao().confirmRelationship(id)

    suspend fun dismissUniversalRelationship(id: Long) =
        database.universalRelationshipDao().dismissRelationship(id)

    // Locations
    val allLocations: Flow<List<StoryLocation>> = database.storyLocationDao().getAllLocations()
    suspend fun getLocationById(id: Long): StoryLocation? = database.storyLocationDao().getLocationById(id)
    suspend fun insertLocation(location: StoryLocation): Long = database.storyLocationDao().insertLocation(location)
    suspend fun updateLocation(location: StoryLocation) = database.storyLocationDao().updateLocation(location)
    suspend fun deleteLocation(location: StoryLocation) {
        database.storyLocationDao().deleteLocation(location)
        database.universalRelationshipDao().deleteAllForEntity(EntityType.LOCATION.name, location.id)
    }

    // Decisions
    val allDecisions: Flow<List<StoryDecision>> = database.storyDecisionDao().getAllDecisions()
    fun getDecisionsForCharacter(charId: Long): Flow<List<StoryDecision>> = database.storyDecisionDao().getDecisionsForCharacter(charId)
    fun getDecisionsForScene(sceneId: Long): Flow<List<StoryDecision>> = database.storyDecisionDao().getDecisionsForScene(sceneId)
    suspend fun insertDecision(decision: StoryDecision): Long = database.storyDecisionDao().insertDecision(decision)
    suspend fun updateDecision(decision: StoryDecision) = database.storyDecisionDao().updateDecision(decision)
    suspend fun deleteDecision(decision: StoryDecision) {
        database.storyDecisionDao().deleteDecision(decision)
        database.universalRelationshipDao().deleteAllForEntity(EntityType.DECISION.name, decision.id)
    }

    // Sync Log
    val unsyncedLogs: Flow<List<SyncLog>> = database.syncLogDao().getUnsyncedLogs()
    suspend fun recordSyncAction(entityType: String, entityId: Long, action: String, payloadJson: String = "") {
        database.syncLogDao().insertLog(
            SyncLog(
                entityType = entityType,
                entityId = entityId,
                action = action,
                payloadJson = payloadJson
            )
        )
    }
    suspend fun markLogsSynced(ids: List<Long>) = database.syncLogDao().markAsSynced(ids)

    // Clear all sample & demo data across the database
    suspend fun clearAllSampleData() {
        database.clearAllTables()
    }
}
