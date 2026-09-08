package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesDao {
    @Query("SELECT * FROM series ORDER BY updatedAt DESC")
    fun getAllSeries(): Flow<List<Series>>

    @Query("SELECT * FROM series WHERE id = :id")
    suspend fun getSeriesById(id: Long): Series?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: Series): Long

    @Update
    suspend fun updateSeries(series: Series)

    @Delete
    suspend fun deleteSeries(series: Series)
}

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE seriesId = :seriesId ORDER BY orderIndex ASC")
    fun getBooksForSeries(seriesId: Long): Flow<List<Book>>

    @Query("SELECT * FROM books ORDER BY orderIndex ASC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY orderIndex ASC")
    fun getChaptersForBook(bookId: Long): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters ORDER BY orderIndex ASC")
    fun getAllChapters(): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: Long): Chapter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter): Long

    @Update
    suspend fun updateChapter(chapter: Chapter)

    @Delete
    suspend fun deleteChapter(chapter: Chapter)
}

@Dao
interface SceneDao {
    @Query("SELECT * FROM scenes WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    fun getScenesForChapter(chapterId: Long): Flow<List<Scene>>

    @Query("SELECT * FROM scenes ORDER BY orderIndex ASC")
    fun getAllScenes(): Flow<List<Scene>>

    @Query("SELECT * FROM scenes WHERE id = :id")
    suspend fun getSceneById(id: Long): Scene?

    @Query("SELECT * FROM scenes WHERE id = :id")
    fun observeSceneById(id: Long): Flow<Scene?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScene(scene: Scene): Long

    @Update
    suspend fun updateScene(scene: Scene)

    @Delete
    suspend fun deleteScene(scene: Scene)
}

@Dao
interface SceneVersionDao {
    @Query("SELECT * FROM scene_versions WHERE sceneId = :sceneId ORDER BY timestamp DESC")
    fun getVersionsForScene(sceneId: Long): Flow<List<SceneVersion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: SceneVersion): Long

    @Query("DELETE FROM scene_versions WHERE sceneId = :sceneId")
    suspend fun deleteVersionsForScene(sceneId: Long)
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters ORDER BY name ASC")
    fun getAllCharacters(): Flow<List<Character>>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: Long): Character?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: Character): Long

    @Update
    suspend fun updateCharacter(character: Character)

    @Delete
    suspend fun deleteCharacter(character: Character)
}

@Dao
interface CharacterRelationshipDao {
    @Query("SELECT * FROM character_relationships")
    fun getAllRelationships(): Flow<List<CharacterRelationship>>

    @Query("SELECT * FROM character_relationships WHERE sourceCharacterId = :charId OR targetCharacterId = :charId")
    fun getRelationshipsForCharacter(charId: Long): Flow<List<CharacterRelationship>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: CharacterRelationship): Long

    @Delete
    suspend fun deleteRelationship(relationship: CharacterRelationship)

    @Query("DELETE FROM character_relationships WHERE id = :id")
    suspend fun deleteRelationshipById(id: Long)
}

@Dao
interface CodexDao {
    @Query("SELECT * FROM codex_entries ORDER BY title ASC")
    fun getAllEntries(): Flow<List<CodexEntry>>

    @Query("SELECT * FROM codex_entries WHERE category = :category ORDER BY title ASC")
    fun getEntriesByCategory(category: String): Flow<List<CodexEntry>>

    @Query("SELECT * FROM codex_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): CodexEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CodexEntry): Long

    @Update
    suspend fun updateEntry(entry: CodexEntry)

    @Delete
    suspend fun deleteEntry(entry: CodexEntry)
}

@Dao
interface LexiconDao {
    @Query("SELECT * FROM lexicon_terms ORDER BY term ASC")
    fun getAllTerms(): Flow<List<LexiconTerm>>

    @Query("SELECT * FROM lexicon_terms WHERE id = :id")
    suspend fun getTermById(id: Long): LexiconTerm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTerm(term: LexiconTerm): Long

    @Update
    suspend fun updateTerm(term: LexiconTerm)

    @Delete
    suspend fun deleteTerm(term: LexiconTerm)
}

@Dao
interface TimelineDao {
    @Query("SELECT * FROM timelines ORDER BY id ASC")
    fun getAllTimelines(): Flow<List<Timeline>>

    @Query("SELECT * FROM timelines WHERE id = :id")
    suspend fun getTimelineById(id: Long): Timeline?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeline(timeline: Timeline): Long

    @Update
    suspend fun updateTimeline(timeline: Timeline)

    @Delete
    suspend fun deleteTimeline(timeline: Timeline)
}

@Dao
interface WorldRuleDao {
    @Query("SELECT * FROM world_rules ORDER BY name ASC")
    fun getAllRules(): Flow<List<WorldRule>>

    @Query("SELECT * FROM world_rules WHERE id = :id")
    suspend fun getRuleById(id: Long): WorldRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: WorldRule): Long

    @Update
    suspend fun updateRule(rule: WorldRule)

    @Delete
    suspend fun deleteRule(rule: WorldRule)
}

@Dao
interface StoryEventDao {
    @Query("SELECT * FROM story_events ORDER BY cosmicTimestamp ASC")
    fun getAllEvents(): Flow<List<StoryEvent>>

    @Query("SELECT * FROM story_events WHERE timelineId = :timelineId ORDER BY localTimestamp ASC")
    fun getEventsForTimeline(timelineId: Long): Flow<List<StoryEvent>>

    @Query("SELECT * FROM story_events WHERE id = :id")
    suspend fun getEventById(id: Long): StoryEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: StoryEvent): Long

    @Update
    suspend fun updateEvent(event: StoryEvent)

    @Delete
    suspend fun deleteEvent(event: StoryEvent)
}

@Dao
interface CausalEdgeDao {
    @Query("SELECT * FROM causal_edges")
    fun getAllEdges(): Flow<List<CausalEdge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEdge(edge: CausalEdge): Long

    @Delete
    suspend fun deleteEdge(edge: CausalEdge)

    @Query("DELETE FROM causal_edges WHERE id = :id")
    suspend fun deleteEdgeById(id: Long)
}

@Dao
interface SyncLogDao {
    @Query("SELECT * FROM sync_log WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedLogs(): Flow<List<SyncLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SyncLog): Long

    @Query("UPDATE sync_log SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("DELETE FROM sync_log WHERE isSynced = 1")
    suspend fun clearSyncedLogs()
}

@Dao
interface UniversalRelationshipDao {
    @Query("SELECT * FROM universal_relationships ORDER BY id DESC")
    fun getAllRelationships(): Flow<List<UniversalRelationship>>

    @Query("SELECT * FROM universal_relationships WHERE status = :status ORDER BY id DESC")
    fun getRelationshipsByStatus(status: String): Flow<List<UniversalRelationship>>

    @Query("SELECT * FROM universal_relationships WHERE (sourceType = :entityType AND sourceId = :entityId) OR (targetType = :entityType AND targetId = :entityId)")
    fun getRelationshipsForEntity(entityType: String, entityId: Long): Flow<List<UniversalRelationship>>

    @Query("SELECT * FROM universal_relationships WHERE (sourceType = :entityType AND sourceId = :entityId) OR (targetType = :entityType AND targetId = :entityId)")
    suspend fun getRelationshipsForEntitySync(entityType: String, entityId: Long): List<UniversalRelationship>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(rel: UniversalRelationship): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationships(rels: List<UniversalRelationship>): List<Long>

    @Update
    suspend fun updateRelationship(rel: UniversalRelationship)

    @Delete
    suspend fun deleteRelationship(rel: UniversalRelationship)

    @Query("DELETE FROM universal_relationships WHERE id = :id")
    suspend fun deleteRelationshipById(id: Long)

    @Query("UPDATE universal_relationships SET status = 'Confirmed' WHERE id = :id")
    suspend fun confirmRelationship(id: Long)

    @Query("UPDATE universal_relationships SET status = 'Dismissed' WHERE id = :id")
    suspend fun dismissRelationship(id: Long)

    @Query("DELETE FROM universal_relationships WHERE (sourceType = :entityType AND sourceId = :entityId) OR (targetType = :entityType AND targetId = :entityId)")
    suspend fun deleteAllForEntity(entityType: String, entityId: Long)
}

@Dao
interface StoryLocationDao {
    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<StoryLocation>>

    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getLocationById(id: Long): StoryLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: StoryLocation): Long

    @Update
    suspend fun updateLocation(location: StoryLocation)

    @Delete
    suspend fun deleteLocation(location: StoryLocation)
}

@Dao
interface StoryDecisionDao {
    @Query("SELECT * FROM story_decisions ORDER BY id DESC")
    fun getAllDecisions(): Flow<List<StoryDecision>>

    @Query("SELECT * FROM story_decisions WHERE characterId = :characterId ORDER BY id DESC")
    fun getDecisionsForCharacter(characterId: Long): Flow<List<StoryDecision>>

    @Query("SELECT * FROM story_decisions WHERE sceneId = :sceneId ORDER BY id DESC")
    fun getDecisionsForScene(sceneId: Long): Flow<List<StoryDecision>>

    @Query("SELECT * FROM story_decisions WHERE id = :id")
    suspend fun getDecisionById(id: Long): StoryDecision?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecision(decision: StoryDecision): Long

    @Update
    suspend fun updateDecision(decision: StoryDecision)

    @Delete
    suspend fun deleteDecision(decision: StoryDecision)
}

