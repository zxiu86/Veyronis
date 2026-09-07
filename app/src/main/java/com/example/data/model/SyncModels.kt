package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_log")
data class SyncLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String, // Series, Book, Chapter, Scene, Character, Codex, Timeline, Event, WorldRule
    val entityId: Long,
    val action: String, // INSERT, UPDATE, DELETE
    val payloadJson: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

enum class SyncState {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE,
    CONFLICT_DETECTED,
    ERROR
}

enum class ConflictStrategy {
    LOCAL_WINS,
    REMOTE_WINS,
    MANUAL_MERGE
}

data class SyncConflict(
    val entityType: String,
    val entityId: Long,
    val localTitle: String,
    val remoteTitle: String,
    val localTimestamp: Long,
    val remoteTimestamp: Long,
    val localSummary: String,
    val remoteSummary: String
)

data class GitHubReleaseInfo(
    val tagName: String,
    val name: String,
    val body: String,
    val publishedAt: String,
    val downloadUrl: String,
    val assetSize: Long,
    val isPrerelease: Boolean
)
