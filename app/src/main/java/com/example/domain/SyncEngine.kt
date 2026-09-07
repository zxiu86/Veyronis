package com.example.domain

import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncEngine {
    private val _syncState = MutableStateFlow(SyncState.SYNCED)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _pendingConflicts = MutableStateFlow<List<SyncConflict>>(emptyList())
    val pendingConflicts: StateFlow<List<SyncConflict>> = _pendingConflicts.asStateFlow()

    private val _lastSyncedTimestamp = MutableStateFlow<Long?>(System.currentTimeMillis() - 3600000)
    val lastSyncedTimestamp: StateFlow<Long?> = _lastSyncedTimestamp.asStateFlow()

    suspend fun performSync(
        unsyncedLogs: List<SyncLog>,
        onLogsSynced: suspend (List<Long>) -> Unit
    ): Boolean {
        _syncState.value = SyncState.SYNCING
        try {
            // Simulate network transit / sync protocol
            delay(1200)

            if (unsyncedLogs.isNotEmpty()) {
                val syncedIds = unsyncedLogs.map { it.id }
                onLogsSynced(syncedIds)
            }

            _lastSyncedTimestamp.value = System.currentTimeMillis()
            _syncState.value = SyncState.SYNCED
            return true
        } catch (e: Exception) {
            _syncState.value = SyncState.ERROR
            return false
        }
    }

    fun triggerConflictSimulation() {
        val simulatedConflict = SyncConflict(
            entityType = "Scene",
            entityId = 1L,
            localTitle = "Scene 1: Starlight Pulse (Local edits)",
            remoteTitle = "Scene 1: Starlight Pulse (Cloud v1.0.2)",
            localTimestamp = System.currentTimeMillis(),
            remoteTimestamp = System.currentTimeMillis() - 120000,
            localSummary = "Added dialogue regarding the Temporal Distortion escalation.",
            remoteSummary = "Refined description of the Chronos-Compass observatory."
        )
        _pendingConflicts.value = listOf(simulatedConflict)
        _syncState.value = SyncState.CONFLICT_DETECTED
    }

    fun resolveConflict(conflict: SyncConflict, strategy: ConflictStrategy) {
        _pendingConflicts.value = _pendingConflicts.value.filter { it.entityId != conflict.entityId }
        if (_pendingConflicts.value.isEmpty()) {
            _syncState.value = SyncState.SYNCED
        }
    }
}
