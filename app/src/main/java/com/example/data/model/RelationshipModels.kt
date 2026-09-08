package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Universal Entity Types supported across all Veyronis subsystems.
 */
enum class EntityType(val labelEn: String, val labelAr: String) {
    CHARACTER("Character", "شخصية"),
    LOCATION("Location", "موقع / مكان"),
    EVENT("Event", "حدث"),
    SCENE("Scene", "مشهد"),
    CHAPTER("Chapter", "فصل"),
    BOOK("Book", "كتاب"),
    SERIES("Series", "سلسلة"),
    CODEX("Codex / Lore", "موسوعة / لور"),
    LEXICON("Lexicon Term", "مصطلح"),
    WORLD_RULE("World Rule", "قاعدة كونية"),
    TIMELINE("Timeline", "خط زمني"),
    DECISION("Decision", "قرار مصيري");

    companion object {
        fun fromString(type: String): EntityType {
            return entries.find { it.name.equals(type, ignoreCase = true) } ?: CODEX
        }
    }
}

/**
 * Relationship confirmation status.
 * Confirmed: Explicitly confirmed or created by the user / authoritative.
 * Suggested: Automatically detected by the AI / NLP entity detection engine from manuscript text.
 * Dismissed: Rejected by the user.
 */
enum class RelationshipStatus {
    CONFIRMED,
    SUGGESTED,
    DISMISSED
}

/**
 * Central Universal Relationship Entity linking any two entities in the Veyronis Universe.
 * Single Source of Truth for cross-system interconnectedness.
 */
@Entity(
    tableName = "universal_relationships",
    indices = [
        Index(value = ["sourceType", "sourceId"]),
        Index(value = ["targetType", "targetId"]),
        Index(value = ["status"])
    ]
)
data class UniversalRelationship(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceType: String,      // EntityType name
    val sourceId: Long,
    val targetType: String,      // EntityType name
    val targetId: Long,
    val relationType: String,    // "AppearsIn", "LocatedAt", "Mentions", "Causes", "ConsequenceOf", "Friend", "Enemy", "Mentor", "Rival", "GovernedBy", "RelatesTo", "Participant", "Violates", "CreatedIn"
    val status: String = "Confirmed", // Confirmed, Suggested, Dismissed
    val confidenceScore: Float = 1.0f,
    val sourceTextSnippet: String = "",
    val notes: String = "",
    val intensity: Int = 3,      // 1-5 scale for relationship depth
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Dedicated first-class Location Entity with geographical, planetary, and dimensional parameters.
 */
@Entity(tableName = "locations")
data class StoryLocation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val aliases: String = "",
    val type: String = "Citadel", // Planet, Sector, City, Citadel, Realm, Space Station, Dimension, Landmark, Sanctuary, Custom
    val realmOrWorld: String = "Neo-Veyronis Prime",
    val description: String = "",
    val coordinates: String = "",
    val eraOrTimeline: String = "Cosmic Epoch",
    val imageUri: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Significant character/faction decisions that trigger causal cascades.
 */
@Entity(tableName = "story_decisions")
data class StoryDecision(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val characterId: Long,
    val sceneId: Long? = null,
    val eventId: Long? = null,
    val description: String = "",
    val motivation: String = "",
    val consequenceSummary: String = "",
    val cosmicTimestamp: Double? = null,
    val impactLevel: String = "High", // Critical, High, Medium, Subtle
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Unified lightweight entity representation for search, quick navigation, and context panels.
 */
data class UnifiedEntityItem(
    val entityType: EntityType,
    val id: Long,
    val title: String,
    val subtitle: String = "",
    val snippet: String = "",
    val secondaryInfo: String = "",
    val status: String = "",
    val colorHex: String = "#38BDF8"
)

data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

