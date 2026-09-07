package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lexicon_terms")
data class LexiconTerm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val term: String,
    val category: String, // Character, Location, Event, Power, Technology, Lore, Organization, Species, Artifact, Custom
    val shortDefinition: String = "",
    val highlightColorHex: String = "#38BDF8", // Cyan default or category-based
    val autoLinkEnabled: Boolean = true,
    val codexEntryId: Long? = null,
    val characterId: Long? = null,
    val ignoredOccurrencesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
