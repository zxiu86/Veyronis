package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Series::class,
        Book::class,
        Chapter::class,
        Scene::class,
        SceneVersion::class,
        Character::class,
        CharacterRelationship::class,
        CodexEntry::class,
        LexiconTerm::class,
        Timeline::class,
        WorldRule::class,
        StoryEvent::class,
        CausalEdge::class,
        SyncLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun seriesDao(): SeriesDao
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun sceneDao(): SceneDao
    abstract fun sceneVersionDao(): SceneVersionDao
    abstract fun characterDao(): CharacterDao
    abstract fun characterRelationshipDao(): CharacterRelationshipDao
    abstract fun codexDao(): CodexDao
    abstract fun lexiconDao(): LexiconDao
    abstract fun timelineDao(): TimelineDao
    abstract fun worldRuleDao(): WorldRuleDao
    abstract fun storyEventDao(): StoryEventDao
    abstract fun causalEdgeDao(): CausalEdgeDao
    abstract fun syncLogDao(): SyncLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "veyronis_universe.db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Clean slate by default: No demo or sample clutter is seeded automatically.
            }
        }

        private suspend fun populateInitialUniverse(db: AppDatabase) {
            // Seed Series
            val seriesId = db.seriesDao().insertSeries(
                Series(
                    title = "The Veyronis Chronicles",
                    description = "An epic space-opera and temporal hard science-fiction saga exploring the fracture of cosmic causality."
                )
            )

            // Seed Book
            val bookId = db.bookDao().insertBook(
                Book(
                    seriesId = seriesId,
                    title = "Book I: The Shattered Horizon",
                    subtitle = "Echoes of the Chrono-Lattice",
                    description = "When a temporal singularity destabilizes Sector 9, Aria Vance discovers the Cosmic Lattice is decaying.",
                    targetWordCount = 90000,
                    status = "In Progress"
                )
            )

            // Seed Chapters
            val ch1Id = db.chapterDao().insertChapter(
                Chapter(
                    bookId = bookId,
                    title = "Chapter 1: The Singularity Gate",
                    summary = "Aria arrives at the celestial gateway as temporal tremors begin.",
                    orderIndex = 0
                )
            )

            val ch2Id = db.chapterDao().insertChapter(
                Chapter(
                    bookId = bookId,
                    title = "Chapter 2: The Lattice Fracture",
                    summary = "Lord Valerius attempts to stabilize the dimensional rift.",
                    orderIndex = 1
                )
            )

            // Seed Scenes
            val sc1Id = db.sceneDao().insertScene(
                Scene(
                    chapterId = ch1Id,
                    title = "Scene 1: Starlight Pulse",
                    content = "The astral observatory of Veyronis hummed with resonant frequency. Aria Vance adjusted the chrono-compass, watching golden ley-lines fracture across the deep horizon.\n\n\"Lord Valerius warns that the Temporal Distortion is escalating,\" murmured Kael, standing near the Quantum Tunnel terminal.\n\nAria traced the glowing glyphs of the Cosmic Lattice. If the causal chain severed here, cosmic time itself would unravel.",
                    orderIndex = 0,
                    wordCount = 59,
                    characterCount = 412,
                    status = "Revised"
                )
            )

            db.sceneVersionDao().insertVersion(
                SceneVersion(
                    sceneId = sc1Id,
                    title = "Initial Draft",
                    content = "The astral observatory of Veyronis hummed with resonant frequency. Aria Vance adjusted the chrono-compass.",
                    wordCount = 16,
                    changeSummary = "Initial outline snapshot"
                )
            )

            db.sceneDao().insertScene(
                Scene(
                    chapterId = ch1Id,
                    title = "Scene 2: Echoes in the Void",
                    content = "Deep within the quantum containment vault, the Temporal Distortion accelerated. Every five seconds experienced inside mirrored thirteen terrestrial years in the cosmic timeline outside.",
                    orderIndex = 1,
                    wordCount = 27,
                    characterCount = 188,
                    status = "Draft"
                )
            )

            // Seed Timelines
            val cosmicPrimeId = db.timelineDao().insertTimeline(
                Timeline(
                    name = "Cosmic Prime",
                    description = "The universal coordinate baseline for standard cosmic time.",
                    isCosmicPrime = true,
                    cosmicTimeOffset = 0.0,
                    dilationMultiplier = 1.0,
                    colorHex = "#38BDF8"
                )
            )

            val slipstreamId = db.timelineDao().insertTimeline(
                Timeline(
                    name = "Sector 4 Dilated Slipstream",
                    description = "A severe temporal gradient pocket where time flows at accelerated rates.",
                    isCosmicPrime = false,
                    cosmicTimeOffset = 100.0,
                    dilationMultiplier = 0.00001,
                    colorHex = "#FBBF24",
                    parentTimelineId = cosmicPrimeId,
                    divergencePointCosmic = 1204.5
                )
            )

            // Seed World Rules
            db.worldRuleDao().insertRule(
                WorldRule(
                    name = "Temporal Distortion",
                    category = "Temporal",
                    description = "Severe time dilation pocket where local seconds translate to cosmic years.",
                    localTimeEquivalentSeconds = 5.0,
                    externalTimeEquivalentYears = 13.0,
                    efficiencyPercent = 98.5,
                    energyType = "Chrono-Gravitational",
                    enablesTimeTravel = true,
                    affectsConsistency = true
                )
            )

            db.worldRuleDao().insertRule(
                WorldRule(
                    name = "Quantum Tunnel Resonance",
                    category = "Physics",
                    description = "Instantaneous entanglement pathway traversing hyperspace coordinates.",
                    localTimeEquivalentSeconds = 1.0,
                    externalTimeEquivalentYears = 0.0,
                    efficiencyPercent = 50.0,
                    energyType = "Dimensional Energy",
                    enablesTimeTravel = false,
                    affectsConsistency = true
                )
            )

            db.worldRuleDao().insertRule(
                WorldRule(
                    name = "Cosmic Lattice Conservation",
                    category = "Law",
                    description = "No event may precede its own prime cause without generating causal cascade warnings.",
                    efficiencyPercent = 100.0,
                    energyType = "Cosmic Energy",
                    affectsConsistency = true
                )
            )

            // Seed Characters
            val char1Id = db.characterDao().insertCharacter(
                Character(
                    name = "Aria Vance",
                    aliases = "The Weaver of Hours, Chrono-Nav",
                    age = "28",
                    origin = "Solaris Citadel, Neo-Veyronis",
                    appearance = "Silver braided hair, obsidian mantle etched with luminescent star-charts.",
                    personality = "Tenacious, analytical, deeply protective of timeline integrity.",
                    abilities = "Chrono-perception, Quantum navigation, Lattice weaving",
                    strengths = "Can perceive branch divergences before they collapse",
                    weaknesses = "Prone to temporal disorientation when crossing dilated barriers",
                    currentStatus = "Alive",
                    primaryColorHex = "#38BDF8",
                    notes = "Protagonist investigating the fracture of the Cosmic Lattice."
                )
            )

            val char2Id = db.characterDao().insertCharacter(
                Character(
                    name = "Lord Valerius",
                    aliases = "The Arch-Conservator",
                    age = "64",
                    birthInfo = "Epoch 1140 Cosmic",
                    deathInfo = "Reported fallen at the Battle of the Chrono-Rift, Epoch 1220",
                    isDeceased = false,
                    deathCosmicTime = null,
                    origin = "High Bastion of Kaelen",
                    appearance = "Imposing build, dark armored tunic adorned with Chrono-Lattice crests.",
                    personality = "Pragmatic, secretive, uncompromising.",
                    abilities = "Dimensional manipulation, High command, Temporal anchoring",
                    currentStatus = "Alive",
                    primaryColorHex = "#F87171",
                    notes = "Former mentor to Aria, now suspected of altering past causal anchors."
                )
            )

            val char3Id = db.characterDao().insertCharacter(
                Character(
                    name = "Kaelen Voss",
                    aliases = "Kael, The Wanderer",
                    age = "31",
                    origin = "Outer Ring Free Colonies",
                    appearance = "Rugged flight jacket, cybernetic left eye calibrated for tachyon spectrums.",
                    abilities = "Slipstream piloting, Kinetic harmonics",
                    currentStatus = "Alive",
                    primaryColorHex = "#34D399"
                )
            )

            // Relationships
            db.characterRelationshipDao().insertRelationship(
                CharacterRelationship(
                    sourceCharacterId = char1Id,
                    targetCharacterId = char2Id,
                    relationshipType = "Mentor",
                    notes = "Trained together at the Bastion Academy; now estranged over ethical boundaries.",
                    intensity = 4
                )
            )

            db.characterRelationshipDao().insertRelationship(
                CharacterRelationship(
                    sourceCharacterId = char1Id,
                    targetCharacterId = char3Id,
                    relationshipType = "Partner",
                    notes = "Co-pilots aboard the astral vessel Chronos One.",
                    intensity = 5
                )
            )

            // Seed Codex Entries
            db.codexDao().insertEntry(
                CodexEntry(
                    title = "Cosmic Lattice",
                    category = "Physics",
                    summary = "The metaphysical substrate connecting all spatial dimensions and timelines.",
                    description = "The Cosmic Lattice represents the foundational fabric of reality in the Veyronis universe. Composed of entangled tachyon filaments, it sustains causality and coordinates cosmic synchronization across multiversal sectors.",
                    relatedCharacters = "Aria Vance, Lord Valerius",
                    relatedLocations = "Solaris Citadel, Neo-Veyronis"
                )
            )

            db.codexDao().insertEntry(
                CodexEntry(
                    title = "Solaris Citadel",
                    category = "Locations",
                    summary = "Capital planetary bastion of the Chrono-Guard.",
                    description = "Anchored at the apex of the prime sector, Solaris Citadel houses the Great Astrolabe and the primary singularity stabilization engines.",
                    relatedCharacters = "Lord Valerius, Aria Vance"
                )
            )

            db.codexDao().insertEntry(
                CodexEntry(
                    title = "Temporal Distortion",
                    category = "Powers",
                    summary = "Localized warp where time flows at asymmetric rates.",
                    description = "Phenomenon in which local and cosmic clocks diverge exponentially due to gravitational and dimensional compression."
                )
            )

            // Seed Lexicon
            db.lexiconDao().insertTerm(
                LexiconTerm(
                    term = "Temporal Distortion",
                    category = "Power",
                    shortDefinition = "Localized warp of time dilation.",
                    highlightColorHex = "#FBBF24"
                )
            )

            db.lexiconDao().insertTerm(
                LexiconTerm(
                    term = "Cosmic Lattice",
                    category = "Lore",
                    shortDefinition = "Universal substrate binding causal threads.",
                    highlightColorHex = "#818CF8"
                )
            )

            db.lexiconDao().insertTerm(
                LexiconTerm(
                    term = "Aria Vance",
                    category = "Character",
                    shortDefinition = "Navigator of the Chrono-Lattice.",
                    highlightColorHex = "#38BDF8"
                )
            )

            db.lexiconDao().insertTerm(
                LexiconTerm(
                    term = "Lord Valerius",
                    category = "Character",
                    shortDefinition = "Arch-Conservator of Bastion.",
                    highlightColorHex = "#F87171"
                )
            )

            // Seed Events
            val ev1Id = db.storyEventDao().insertEvent(
                StoryEvent(
                    title = "The Lattice Fracture",
                    summary = "Cataclysmic rupture at Sector 9 causing cosmic temporal desynchronization.",
                    timelineId = cosmicPrimeId,
                    cosmicTimestamp = 1200.0,
                    localTimestamp = 1200.0,
                    durationYears = 0.2,
                    characterIds = "$char1Id,$char2Id",
                    locationNames = "Sector 9 Gate, Solaris Citadel",
                    causes = "Excessive quantum tunneling in deep slipstream",
                    consequences = "Destabilization of the universal timeline",
                    canvasPosX = 120f,
                    canvasPosY = 220f
                )
            )

            val ev2Id = db.storyEventDao().insertEvent(
                StoryEvent(
                    title = "Expedition into the Dilated Pocket",
                    summary = "Aria and Kael enter the slipstream to retrieve the chronometer seed.",
                    timelineId = slipstreamId,
                    cosmicTimestamp = 1205.0,
                    localTimestamp = 1.0,
                    durationYears = 13.0,
                    characterIds = "$char1Id,$char3Id",
                    locationNames = "Sector 4 Slipstream",
                    causes = "$ev1Id",
                    consequences = "5 seconds inside equals 13 years outside",
                    canvasPosX = 360f,
                    canvasPosY = 180f
                )
            )

            val ev3Id = db.storyEventDao().insertEvent(
                StoryEvent(
                    title = "The Return to Solaris",
                    summary = "Aria re-emerges into the cosmic timeline only to find a decade has passed.",
                    timelineId = cosmicPrimeId,
                    cosmicTimestamp = 1218.0,
                    localTimestamp = 1205.5,
                    durationYears = 1.0,
                    characterIds = "$char1Id,$char2Id",
                    locationNames = "Solaris Citadel",
                    causes = "$ev2Id",
                    consequences = "Lord Valerius has declared martial continuity law",
                    canvasPosX = 600f,
                    canvasPosY = 240f
                )
            )

            // Seed Causal Edges
            db.causalEdgeDao().insertEdge(
                CausalEdge(
                    sourceEventId = ev1Id,
                    targetEventId = ev2Id,
                    relationshipType = "Causes",
                    description = "Fracture mandates retrieval expedition"
                )
            )

            db.causalEdgeDao().insertEdge(
                CausalEdge(
                    sourceEventId = ev2Id,
                    targetEventId = ev3Id,
                    relationshipType = "Leads To",
                    description = "Emergence into changed cosmic epoch"
                )
            )
        }
    }
}
