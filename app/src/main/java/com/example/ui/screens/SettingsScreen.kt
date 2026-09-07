package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ConflictStrategy
import com.example.data.model.SyncState
import com.example.domain.ExportFormat
import com.example.domain.ExportOptions
import com.example.domain.ExportResult
import com.example.domain.UpdateCheckStatus
import com.example.ui.VeyronisViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: VeyronisViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val allSeries by viewModel.allSeries.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val selectedSeriesId by viewModel.selectedSeriesId.collectAsStateWithLifecycle()
    val selectedBookId by viewModel.selectedBookId.collectAsStateWithLifecycle()

    val currentSeries = allSeries.find { it.id == selectedSeriesId } ?: allSeries.firstOrNull()
    val currentBook = allBooks.find { it.id == selectedBookId } ?: allBooks.firstOrNull()

    // Sync state
    val syncState by viewModel.syncEngine.syncState.collectAsStateWithLifecycle()
    val pendingConflicts by viewModel.syncEngine.pendingConflicts.collectAsStateWithLifecycle()
    val lastSyncedTime by viewModel.syncEngine.lastSyncedTimestamp.collectAsStateWithLifecycle()

    // Update state
    val updateStatus by viewModel.updateEngine.status.collectAsStateWithLifecycle()
    val latestRelease by viewModel.updateEngine.latestRelease.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.updateEngine.downloadProgress.collectAsStateWithLifecycle()
    val updateError by viewModel.updateEngine.errorMessage.collectAsStateWithLifecycle()

    // Export state
    var exportFormat by remember { mutableStateOf(ExportFormat.MARKDOWN) }
    var authorName by remember { mutableStateOf("Aria Vance") }
    var includeSceneTitles by remember { mutableStateOf(true) }
    var exportResult by remember { mutableStateOf<ExportResult?>(null) }
    var showExportPreviewDialog by remember { mutableStateOf(false) }

    // Cover Customization
    var bookTitleInput by remember { mutableStateOf(currentBook?.title ?: "Book I: The Shattered Horizon") }
    var bookSubtitleInput by remember { mutableStateOf(currentBook?.subtitle ?: "Echoes of the Chrono-Lattice") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VeyronisBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Book Cover & Presentation
        item {
            Text(
                text = "Book Cover & Presentation",
                style = MaterialTheme.typography.titleMedium,
                color = VeyronisTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Celestial Book Cover Mockup
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1127)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(100.dp)
                            .height(150.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentSeries?.title?.uppercase() ?: "VEYRONIS",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                                color = VeyronisSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = VeyronisPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = bookTitleInput,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = VeyronisTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )
                                Text(
                                    text = authorName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp),
                                    color = VeyronisTertiary
                                )
                            }
                        }
                    }

                    // Metadata Inputs
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = bookTitleInput,
                            onValueChange = { bookTitleInput = it },
                            label = { Text("Display Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                        )
                        OutlinedTextField(
                            value = bookSubtitleInput,
                            onValueChange = { bookSubtitleInput = it },
                            label = { Text("Subtitle") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                        )
                    }
                }
            }
        }

        // Section: Manuscript Export
        item {
            Text(
                text = "Publishing & Manuscript Export",
                style = MaterialTheme.typography.titleMedium,
                color = VeyronisTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("export_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Compile manuscript into standard publishing formats:",
                        style = MaterialTheme.typography.bodySmall,
                        color = VeyronisTextSecondary
                    )

                    OutlinedTextField(
                        value = authorName,
                        onValueChange = { authorName = it },
                        label = { Text("Author Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VeyronisPrimary, focusedTextColor = VeyronisTextPrimary, unfocusedTextColor = VeyronisTextPrimary)
                    )

                    Text(text = "Target Format:", style = MaterialTheme.typography.labelMedium, color = VeyronisTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (fmt in listOf(ExportFormat.MARKDOWN, ExportFormat.EPUB, ExportFormat.PDF, ExportFormat.DOCX, ExportFormat.TXT, ExportFormat.JSON)) {
                            val isSelected = exportFormat == fmt
                            FilterChip(
                                selected = isSelected,
                                onClick = { exportFormat = fmt },
                                label = { Text(fmt.name, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VeyronisPrimary,
                                    selectedLabelColor = VeyronisTextPrimary,
                                    containerColor = VeyronisPanelVariant,
                                    labelColor = VeyronisTextSecondary
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val res = viewModel.executeExport(
                                ExportOptions(
                                    format = exportFormat,
                                    authorName = authorName,
                                    includeSceneTitles = includeSceneTitles
                                )
                            )
                            if (res != null) {
                                exportResult = res
                                showExportPreviewDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("export_generate_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compile & Export (${exportFormat.displayName})")
                    }
                }
            }
        }

        // Section: Cloud Synchronization
        item {
            Text(
                text = "Cloud Sync & Local-First Resilience",
                style = MaterialTheme.typography.titleMedium,
                color = VeyronisTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("cloud_sync_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (syncState) {
                                    SyncState.SYNCED -> Icons.Default.CloudDone
                                    SyncState.SYNCING -> Icons.Default.Sync
                                    SyncState.CONFLICT_DETECTED -> Icons.Default.Warning
                                    else -> Icons.Default.CloudQueue
                                },
                                contentDescription = null,
                                tint = when (syncState) {
                                    SyncState.SYNCED -> VeyronisSuccess
                                    SyncState.SYNCING -> VeyronisTertiary
                                    SyncState.CONFLICT_DETECTED -> VeyronisWarning
                                    else -> VeyronisTextSecondary
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Sync Status: ${syncState.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = VeyronisTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                val lastSyncStr = if (lastSyncedTime != null) {
                                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastSyncedTime!!))
                                } else "Never"
                                Text(
                                    text = "Last synced at $lastSyncStr • SQLite local replica active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VeyronisTextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.triggerSync() },
                            colors = ButtonDefaults.buttonColors(containerColor = VeyronisSecondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("sync_now_button")
                        ) {
                            Text("Sync Now", color = VeyronisBackground)
                        }
                    }

                    if (pendingConflicts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = VeyronisWarningContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Conflict Detected (Preserving Data)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = VeyronisWarning,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Remote modification detected on '${pendingConflicts.first().localTitle}'.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VeyronisTextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalButton(
                                        onClick = { viewModel.syncEngine.resolveConflict(pendingConflicts.first(), ConflictStrategy.LOCAL_WINS) }
                                    ) { Text("Keep Local") }
                                    FilledTonalButton(
                                        onClick = { viewModel.syncEngine.resolveConflict(pendingConflicts.first(), ConflictStrategy.REMOTE_WINS) }
                                    ) { Text("Use Cloud") }
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.syncEngine.triggerConflictSimulation() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VeyronisTextSecondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simulate Cloud Conflict Test", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Section: In-App Updates via GitHub Releases
        item {
            Text(
                text = "In-App Updates (GitHub Releases)",
                style = MaterialTheme.typography.titleMedium,
                color = VeyronisTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("in_app_update_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Installed Version: v${viewModel.updateEngine.currentVersion}",
                                style = MaterialTheme.typography.titleSmall,
                                color = VeyronisTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Persistent signing verified • Zero data loss guarantee",
                                style = MaterialTheme.typography.labelSmall,
                                color = VeyronisTextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.updateEngine.checkForUpdates()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
                            modifier = Modifier.testTag("check_updates_button")
                        ) {
                            Text("Check Updates")
                        }
                    }

                    if (updateStatus == UpdateCheckStatus.CHECKING) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = VeyronisPrimary)
                    }

                    if (updateStatus == UpdateCheckStatus.UPDATE_AVAILABLE && latestRelease != null) {
                        val rel = latestRelease!!
                        Card(
                            colors = CardDefaults.cardColors(containerColor = VeyronisPrimaryContainer.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("update_available_banner")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "New Version Available: Veyronis v${rel.tagName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = VeyronisPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rel.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VeyronisTextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.updateEngine.startDownload { }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = VeyronisPrimary),
                                        modifier = Modifier.testTag("download_update_button")
                                    ) {
                                        Text("Update Now")
                                    }
                                    TextButton(onClick = { viewModel.updateEngine.dismissUpdate() }) {
                                        Text("Later", color = VeyronisTextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    if (updateStatus == UpdateCheckStatus.DOWNLOADING) {
                        Column {
                            Text(
                                text = "Downloading release artifact... ${(downloadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = VeyronisTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = VeyronisPrimary
                            )
                        }
                    }

                    if (updateStatus == UpdateCheckStatus.READY_TO_INSTALL) {
                        Text(
                            text = "Artifact verified with SHA-256. System package installer initiated.",
                            style = MaterialTheme.typography.bodySmall,
                            color = VeyronisSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (updateStatus == UpdateCheckStatus.UP_TO_DATE) {
                        Text(
                            text = "Veyronis is up to date (v${viewModel.updateEngine.currentVersion}).",
                            style = MaterialTheme.typography.bodySmall,
                            color = VeyronisSuccess
                        )
                    }
                }
            }
        }
    }

    // Modal Dialog: Export Output Preview
    if (showExportPreviewDialog && exportResult != null) {
        val res = exportResult!!
        AlertDialog(
            onDismissRequest = { showExportPreviewDialog = false },
            containerColor = VeyronisPanel,
            title = {
                Text("Export Ready: ${res.fileName}", color = VeyronisTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Total Words: ${res.wordCount} • Format: ${res.mimeType}",
                        style = MaterialTheme.typography.labelMedium,
                        color = VeyronisTertiary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = res.content.take(1500) + if (res.content.length > 1500) "\n\n[... Remaining content generated successfully ...]" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = VeyronisTextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportPreviewDialog = false }) {
                    Text("Done", color = VeyronisPrimary)
                }
            }
        )
    }
}
