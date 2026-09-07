package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.WarningSeverity
import com.example.ui.AppSection
import com.example.ui.VeyronisViewModel
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: VeyronisViewModel,
    modifier: Modifier = Modifier
) {
    val allSeries by viewModel.allSeries.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val allChapters by viewModel.allChapters.collectAsStateWithLifecycle()
    val allScenes by viewModel.allScenes.collectAsStateWithLifecycle()
    val allCharacters by viewModel.allCharacters.collectAsStateWithLifecycle()
    val allCodex by viewModel.allCodexEntries.collectAsStateWithLifecycle()
    val allEvents by viewModel.allStoryEvents.collectAsStateWithLifecycle()
    val warnings by viewModel.temporalWarnings.collectAsStateWithLifecycle()

    val selectedSeriesId by viewModel.selectedSeriesId.collectAsStateWithLifecycle()
    val selectedBookId by viewModel.selectedBookId.collectAsStateWithLifecycle()

    val currentSeries = allSeries.find { it.id == selectedSeriesId } ?: allSeries.firstOrNull()
    val currentBook = allBooks.find { it.id == selectedBookId } ?: allBooks.firstOrNull()

    val totalWords = allScenes.sumOf { it.wordCount }
    val targetWords = currentBook?.targetWordCount ?: 80000
    val progress = if (targetWords > 0) (totalWords.toFloat() / targetWords).coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VeyronisBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_header_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "VEYRONIS ARCHIVES",
                                style = MaterialTheme.typography.labelSmall,
                                color = VeyronisSecondary,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentSeries?.title ?: "No Series Selected",
                                style = MaterialTheme.typography.headlineSmall,
                                color = VeyronisTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentBook != null) {
                                Text(
                                    text = currentBook.title + if (currentBook.subtitle.isNotBlank()) " — ${currentBook.subtitle}" else "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = VeyronisTextSecondary
                                )
                            }
                        }

                        Surface(
                            color = VeyronisPrimaryContainer,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = currentBook?.status ?: "Active",
                                color = VeyronisPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Manuscript Progress: ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = VeyronisTextSecondary
                        )
                        Text(
                            text = "$totalWords / $targetWords words",
                            style = MaterialTheme.typography.bodySmall,
                            color = VeyronisTertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = VeyronisSecondary,
                        trackColor = VeyronisPanelVariant,
                    )
                }
            }
        }

        item {
            // Quick metrics grid
            Text(
                text = "Universe Metrics",
                style = MaterialTheme.typography.titleMedium,
                color = VeyronisTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Chapters",
                    value = allChapters.size.toString(),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    color = VeyronisPrimary
                ) { viewModel.navigateTo(AppSection.WRITER) }

                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Characters",
                    value = allCharacters.size.toString(),
                    icon = Icons.Default.People,
                    color = VeyronisSecondary
                ) { viewModel.navigateTo(AppSection.CHARACTERS) }

                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Codex Lore",
                    value = allCodex.size.toString(),
                    icon = Icons.Default.AutoStories,
                    color = VeyronisTertiary
                ) { viewModel.navigateTo(AppSection.CODEX) }

                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Events",
                    value = allEvents.size.toString(),
                    icon = Icons.Default.Timeline,
                    color = Color(0xFFE879F9)
                ) { viewModel.navigateTo(AppSection.EVENTS) }
            }
        }

        // Temporal Consistency Warning Alert Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (warnings.isNotEmpty()) VeyronisWarningContainer else VeyronisPanel
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppSection.WORLD_RULES) }
                    .testTag("temporal_engine_status_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (warnings.isNotEmpty()) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = "Temporal Status",
                        tint = if (warnings.isNotEmpty()) VeyronisWarning else VeyronisSuccess,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (warnings.isNotEmpty()) "TEMPORAL WARNINGS (${warnings.size})" else "TEMPORAL CONTINUITY VERIFIED",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (warnings.isNotEmpty()) VeyronisWarning else VeyronisSuccess,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (warnings.isNotEmpty())
                                "${warnings.count { it.severity == WarningSeverity.CRITICAL }} critical causal paradoxes detected."
                            else
                                "All event sequences and character appearances are temporally consistent.",
                            style = MaterialTheme.typography.bodySmall,
                            color = VeyronisTextSecondary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(AppSection.WORLD_RULES) },
                        modifier = Modifier.testTag("inspect_warnings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Inspect Warnings",
                            tint = VeyronisTextSecondary
                        )
                    }
                }
            }
        }

        // Active Temporal Warnings List Preview
        if (warnings.isNotEmpty()) {
            item {
                Text(
                    text = "Continuity Engine Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    color = VeyronisTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            items(warnings.take(3)) { warning ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (warning.severity == WarningSeverity.CRITICAL) VeyronisWarning else VeyronisTertiary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = warning.severity.name,
                                    color = Color.Black,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = warning.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = VeyronisTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = warning.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = VeyronisTextSecondary
                        )
                    }
                }
            }
        }

        // Quick Navigation Launchpad
        item {
            Text(
                text = "Creative Workspaces",
                style = MaterialTheme.typography.titleMedium,
                color = VeyronisTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkspaceTile(
                    title = "Manuscript Editor",
                    subtitle = "Write, format, autosave, and view Lexicon links",
                    icon = Icons.Default.Edit,
                    color = VeyronisPrimary
                ) { viewModel.navigateTo(AppSection.WRITER) }

                WorkspaceTile(
                    title = "Multi-Timeline & Dilation",
                    subtitle = "Manage cosmic vs local clocks and temporal branches",
                    icon = Icons.Default.HourglassBottom,
                    color = VeyronisSecondary
                ) { viewModel.navigateTo(AppSection.TIMELINE) }

                WorkspaceTile(
                    title = "Causal & Relationship Graphs",
                    subtitle = "Interactive node canvas for cause/effect & character bonds",
                    icon = Icons.Default.Hub,
                    color = VeyronisTertiary
                ) { viewModel.navigateTo(AppSection.GRAPHS) }

                WorkspaceTile(
                    title = "Export & GitHub Sync",
                    subtitle = "PDF, EPUB, Markdown export, Cloud sync & In-App updates",
                    icon = Icons.Default.CloudSync,
                    color = Color(0xFF34D399)
                ) { viewModel.navigateTo(AppSection.SETTINGS) }
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("metric_card_${title.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = VeyronisTextPrimary, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = VeyronisTextMuted)
        }
    }
}

@Composable
fun WorkspaceTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("workspace_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = VeyronisTextPrimary, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = VeyronisTextSecondary)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Navigate", tint = VeyronisTextMuted)
        }
    }
}
