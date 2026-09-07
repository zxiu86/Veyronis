package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "series")
data class Series(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val coverUri: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = Series::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["seriesId"])]
)
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seriesId: Long,
    val title: String,
    val subtitle: String = "",
    val description: String = "",
    val orderIndex: Int = 0,
    val coverUri: String = "",
    val targetWordCount: Int = 80000,
    val status: String = "In Progress", // Planned, In Progress, Revised, Completed
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"])]
)
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val title: String,
    val summary: String = "",
    val orderIndex: Int = 0,
    val status: String = "Draft",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "scenes",
    foreignKeys = [
        ForeignKey(
            entity = Chapter::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chapterId"])]
)
data class Scene(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val title: String,
    val content: String = "",
    val orderIndex: Int = 0,
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val status: String = "Draft",
    val timelineId: Long? = null,
    val cosmicTimestamp: Double? = null,
    val localTimestamp: Double? = null,
    val povCharacterId: Long? = null,
    val locationId: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "scene_versions",
    foreignKeys = [
        ForeignKey(
            entity = Scene::class,
            parentColumns = ["id"],
            childColumns = ["sceneId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sceneId"])]
)
data class SceneVersion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sceneId: Long,
    val title: String,
    val content: String,
    val wordCount: Int,
    val changeSummary: String = "Autosaved snapshot",
    val timestamp: Long = System.currentTimeMillis()
)
