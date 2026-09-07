package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timelines")
data class Timeline(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val isCosmicPrime: Boolean = false,
    val cosmicTimeOffset: Double = 0.0, // Offset from global cosmic year 0.0
    val dilationMultiplier: Double = 1.0, // Local seconds per Cosmic second (1.0 = normal)
    val colorHex: String = "#818CF8",
    val parentTimelineId: Long? = null,
    val divergencePointCosmic: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "world_rules")
data class WorldRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g., Temporal Distortion, Quantum Tunnel, Dimensional Energy
    val category: String = "Physics", // Physics, Magic/Energy, Dimensional, Temporal, Technology, Law
    val description: String = "",
    val localTimeEquivalentSeconds: Double = 5.0, // e.g. 5 seconds
    val externalTimeEquivalentYears: Double = 13.0, // e.g. 13 years
    val efficiencyPercent: Double = 100.0, // e.g. 50% Time Machine Efficiency
    val energyType: String = "Cosmic / Chrono", // Dimensional Energy, Cosmic Energy, etc.
    val enablesTimeTravel: Boolean = false,
    val affectsConsistency: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
