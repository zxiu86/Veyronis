package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story_events")
data class StoryEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val summary: String = "",
    val timelineId: Long = 1,
    val cosmicTimestamp: Double = 0.0, // Universal year/epoch
    val localTimestamp: Double = 0.0,
    val durationYears: Double = 0.0,
    val characterIds: String = "", // Comma-separated character IDs
    val locationNames: String = "", // Comma-separated location names
    val causes: String = "", // Description or comma-separated event IDs
    val consequences: String = "",
    val relatedLore: String = "",
    val worldRuleIds: String = "", // Applied world rules IDs
    val notes: String = "",
    val canvasPosX: Float = 150f, // For interactive Causal Graph Canvas
    val canvasPosY: Float = 150f,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "causal_edges")
data class CausalEdge(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceEventId: Long,
    val targetEventId: Long,
    val relationshipType: String = "Causes", // Causes, Leads To, Depends On, Conflicts With, Prevents, Creates, Destroys, Changes, Related To
    val description: String = "",
    val weight: Float = 1.0f
)
