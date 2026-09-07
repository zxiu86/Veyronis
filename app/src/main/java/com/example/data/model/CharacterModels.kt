package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class Character(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val aliases: String = "", // Comma-separated or multiline
    val age: String = "",
    val birthInfo: String = "",
    val deathInfo: String = "",
    val isDeceased: Boolean = false,
    val deathCosmicTime: Double? = null,
    val origin: String = "",
    val appearance: String = "",
    val personality: String = "",
    val abilities: String = "",
    val strengths: String = "",
    val weaknesses: String = "",
    val imageUri: String = "",
    val notes: String = "",
    val currentStatus: String = "Alive", // Alive, Deceased, Missing, Ascended, Displaced
    val primaryColorHex: String = "#818CF8",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "character_relationships")
data class CharacterRelationship(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceCharacterId: Long,
    val targetCharacterId: Long,
    val relationshipType: String, // Friend, Enemy, Brother, Sister, Parent, Child, Mentor, Student, Partner, Rival, Unknown, Custom
    val notes: String = "",
    val originatingEventTitle: String = "",
    val changedByEventId: Long? = null,
    val intensity: Int = 3 // 1 to 5 scale
)
