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
import com.example.ui.AppLanguage
import com.example.ui.Strings
import com.example.ui.VeyronisViewModel
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.components.LuxuryGradientButton
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
            .background(Color.Transparent)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Language Preferences & UI Direction
        item {
            val language by viewModel.appLanguage.collectAsStateWithLifecycle()
            Text(
                text = Strings.get("language_settings", language),
                style = MaterialTheme.typography.titleMedium,
                color = LuxuryTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LuxuryGlassCard(
                glowColor = LuxuryElectricCyan,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("language_settings_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = Strings.get("select_language", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxuryTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // English option
                        FilterChip(
                            selected = language == AppLanguage.ENGLISH,
                            onClick = { if (language != AppLanguage.ENGLISH) viewModel.toggleLanguage() },
                            label = { Text(Strings.get("english", language), fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                if (language == AppLanguage.ENGLISH) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LuxuryElectricCyan,
                                selectedLabelColor = LuxuryVoidBackground,
                                selectedLeadingIconColor = LuxuryVoidBackground,
                                containerColor = LuxurySurfaceElevated,
                                labelColor = LuxuryTextSecondary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_english_chip")
                        )

                        // Arabic option
                        FilterChip(
                            selected = language == AppLanguage.ARABIC,
                            onClick = { if (language != AppLanguage.ARABIC) viewModel.toggleLanguage() },
                            label = { Text(Strings.get("arabic", language), fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                if (language == AppLanguage.ARABIC) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LuxuryElectricCyan,
                                selectedLabelColor = LuxuryVoidBackground,
                                selectedLeadingIconColor = LuxuryVoidBackground,
                                containerColor = LuxurySurfaceElevated,
                                labelColor = LuxuryTextSecondary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_arabic_chip")
                        )
                    }

                    Surface(
                        color = LuxurySurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatTextdirectionLToR,
                                contentDescription = null,
                                tint = LuxuryElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Strings.get("enforce_ltr_note", language),
                                style = MaterialTheme.typography.labelSmall,
                                color = LuxuryTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Section: Sample Data & Content Reset
        item {
            val language by viewModel.appLanguage.collectAsStateWithLifecycle()
            var showConfirmPurgeDialog by remember { mutableStateOf(false) }

            Text(
                text = Strings.get("data_management", language),
                style = MaterialTheme.typography.titleMedium,
                color = LuxuryTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LuxuryGlassCard(
                glowColor = LuxuryWarning,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("data_management_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = Strings.get("delete_sample_desc", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxuryTextSecondary
                    )

                    OutlinedButton(
                        onClick = { showConfirmPurgeDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = LuxuryWarning
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryWarning.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("purge_sample_data_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = LuxuryWarning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.get("clear_sample_data", language),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (showConfirmPurgeDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmPurgeDialog = false },
                    containerColor = LuxurySurface,
                    title = {
                        Text(
                            text = Strings.get("confirm_delete", language),
                            color = LuxuryTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = Strings.get("delete_warning_msg", language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LuxuryTextSecondary
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.purgeSampleData()
                                showConfirmPurgeDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryWarning),
                            modifier = Modifier.testTag("confirm_purge_btn")
                        ) {
                            Text(
                                text = Strings.get("clear_sample_data", language),
                                color = LuxuryVoidBackground,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmPurgeDialog = false }) {
                            Text(
                                text = Strings.get("cancel", language),
                                color = LuxuryTextSecondary
                            )
                        }
                    }
                )
            }
        }

        // Section: Book Cover & Presentation
        item {
            Text(
                text = "Book Cover & Presentation",
                style = MaterialTheme.typography.titleMedium,
                color = LuxuryTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LuxuryGlassCard(
                glowColor = LuxuryAuroraViolet,
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
                        colors = CardDefaults.cardColors(containerColor = LuxuryVoidBackground),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryAuroraViolet.copy(alpha = 0.5f)),
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
                                color = LuxuryElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = LuxurySubtleGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = bookTitleInput,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = LuxuryTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )
                                Text(
                                    text = authorName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp),
                                    color = LuxuryAuroraViolet
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
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryAuroraViolet,
                                focusedTextColor = LuxuryTextPrimary,
                                unfocusedTextColor = LuxuryTextPrimary,
                                unfocusedContainerColor = LuxurySurfaceElevated,
                                focusedContainerColor = LuxurySurfaceElevated
                            )
                        )
                        OutlinedTextField(
                            value = bookSubtitleInput,
                            onValueChange = { bookSubtitleInput = it },
                            label = { Text("Subtitle") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuxuryAuroraViolet,
                                focusedTextColor = LuxuryTextPrimary,
                                unfocusedTextColor = LuxuryTextPrimary,
                                unfocusedContainerColor = LuxurySurfaceElevated,
                                focusedContainerColor = LuxurySurfaceElevated
                            )
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
                color = LuxuryTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LuxuryGlassCard(
                glowColor = LuxuryElectricCyan,
                modifier = Modifier.fillMaxWidth().testTag("export_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Compile manuscript into standard publishing formats:",
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxuryTextSecondary
                    )

                    OutlinedTextField(
                        value = authorName,
                        onValueChange = { authorName = it },
                        label = { Text("Author Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryElectricCyan,
                            focusedTextColor = LuxuryTextPrimary,
                            unfocusedTextColor = LuxuryTextPrimary,
                            unfocusedContainerColor = LuxurySurfaceElevated,
                            focusedContainerColor = LuxurySurfaceElevated
                        )
                    )

                    Text(text = "Target Format:", style = MaterialTheme.typography.labelMedium, color = LuxuryTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (fmt in listOf(ExportFormat.MARKDOWN, ExportFormat.EPUB, ExportFormat.PDF, ExportFormat.DOCX, ExportFormat.TXT, ExportFormat.JSON)) {
                            val isSelected = exportFormat == fmt
                            FilterChip(
                                selected = isSelected,
                                onClick = { exportFormat = fmt },
                                label = { Text(fmt.name, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LuxuryElectricCyan,
                                    selectedLabelColor = LuxuryVoidBackground,
                                    containerColor = LuxurySurfaceElevated,
                                    labelColor = LuxuryTextSecondary
                                )
                            )
                        }
                    }

                    LuxuryGradientButton(
                        text = "Compile & Export (${exportFormat.displayName})",
                        icon = Icons.Default.Download,
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
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("export_generate_button")
                    )
                }
            }
        }

        // Section: Cloud Synchronization
        item {
            Text(
                text = "Cloud Sync & Local-First Resilience",
                style = MaterialTheme.typography.titleMedium,
                color = LuxuryTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LuxuryGlassCard(
                glowColor = LuxuryAuroraViolet,
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
                                    SyncState.SYNCED -> LuxurySuccess
                                    SyncState.SYNCING -> LuxuryElectricCyan
                                    SyncState.CONFLICT_DETECTED -> LuxuryWarning
                                    else -> LuxuryTextSecondary
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Sync Status: ${syncState.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = LuxuryTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                val lastSyncStr = if (lastSyncedTime != null) {
                                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastSyncedTime!!))
                                } else "Never"
                                Text(
                                    text = "Last synced at $lastSyncStr • SQLite local replica active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LuxuryTextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.triggerSync() },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryElectricCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("sync_now_button")
                        ) {
                            Text("Sync Now", color = LuxuryVoidBackground, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (pendingConflicts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LuxuryWarning.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Conflict Detected (Preserving Data)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = LuxuryWarning,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Remote modification detected on '${pendingConflicts.first().localTitle}'.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LuxuryTextPrimary
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
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LuxuryTextSecondary),
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
                color = LuxuryTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LuxuryGlassCard(
                glowColor = LuxuryCyberIndigo,
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
                                color = LuxuryTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Persistent signing verified • Zero data loss guarantee",
                                style = MaterialTheme.typography.labelSmall,
                                color = LuxuryTextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.updateEngine.checkForUpdates()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryCyberIndigo),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("check_updates_button")
                        ) {
                            Text("Check Updates", color = LuxuryTextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (updateStatus == UpdateCheckStatus.CHECKING) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = LuxuryElectricCyan)
                    }

                    if (updateStatus == UpdateCheckStatus.UPDATE_AVAILABLE && latestRelease != null) {
                        val rel = latestRelease!!
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LuxuryElectricCyan.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("update_available_banner")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "New Version Available: Veyronis v${rel.tagName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = LuxuryElectricCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rel.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LuxuryTextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                viewModel.updateEngine.startDownload { }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryElectricCyan),
                                        modifier = Modifier.testTag("download_update_button")
                                    ) {
                                        Text("Update Now", color = LuxuryVoidBackground, fontWeight = FontWeight.Bold)
                                    }
                                    TextButton(onClick = { viewModel.updateEngine.dismissUpdate() }) {
                                        Text("Later", color = LuxuryTextSecondary)
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
                                color = LuxuryTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = LuxuryElectricCyan
                            )
                        }
                    }

                    if (updateStatus == UpdateCheckStatus.READY_TO_INSTALL) {
                        Text(
                            text = "Artifact verified with SHA-256. System package installer initiated.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxurySuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (updateStatus == UpdateCheckStatus.UP_TO_DATE) {
                        Text(
                            text = "Veyronis is up to date (v${viewModel.updateEngine.currentVersion}).",
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxurySuccess
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
            containerColor = LuxurySurface,
            title = {
                Text("Export Ready: ${res.fileName}", color = LuxuryTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Total Words: ${res.wordCount} • Format: ${res.mimeType}",
                        style = MaterialTheme.typography.labelMedium,
                        color = LuxuryAuroraViolet,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = res.content.take(1500) + if (res.content.length > 1500) "\n\n[... Remaining content generated successfully ...]" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxuryTextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportPreviewDialog = false }) {
                    Text("Done", color = LuxuryElectricCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
