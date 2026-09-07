package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "codex_entries")
data class CodexEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // Characters, Locations, Worlds, Universes, Species, Organizations, Technology, Powers, Weapons, Artifacts, Events, History, Physics, Terminology, Custom
    val summary: String = "",
    val description: String = "",
    val aliases: String = "",
    val relatedCharacters: String = "", // Comma-separated names or IDs
    val relatedLocations: String = "",
    val relatedEvents: String = "",
    val relatedLore: String = "",
    val imageUri: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
