package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.WarningSeverity
import com.example.ui.AppLanguage
import com.example.ui.AppSection
import com.example.ui.Strings
import com.example.ui.VeyronisViewModel
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.components.LuxuryGradientButton
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
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()

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
            .background(Color.Transparent)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header Glass Card
            LuxuryGlassCard(
                glowColor = LuxuryElectricCyan,
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
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = LuxuryElectricCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = Strings.get("current_manuscript", language).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LuxuryElectricCyan,
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentSeries?.title ?: if (language == AppLanguage.ARABIC) "مشروع جديد نظيف" else "New Clean Project",
                                style = MaterialTheme.typography.headlineSmall,
                                color = LuxuryTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentBook != null) {
                                Text(
                                    text = currentBook.title + if (currentBook.subtitle.isNotBlank()) " — ${currentBook.subtitle}" else "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LuxuryTextSecondary
                                )
                            } else {
                                Text(
                                    text = Strings.get("clean_slate", language),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LuxuryTextMuted
                                )
                            }
                        }

                        Surface(
                            color = LuxurySurfaceElevated,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryElectricCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = currentBook?.status ?: if (language == AppLanguage.ARABIC) "نشط" else "Active",
                                color = LuxuryElectricCyan,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${Strings.get("overall_progress", language)}: ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxuryTextSecondary
                        )
                        Text(
                            text = "$totalWords / $targetWords ${Strings.get("words_count", language)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxuryAuroraViolet,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(LuxurySurfaceHighlight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(CircleShape)
                                .background(LuxuryPrimaryGradient)
                        )
                    }
                }
            }
        }

        item {
            // Quick metrics grid
            Text(
                text = if (language == AppLanguage.ARABIC) "إحصائيات المحتوى والأكوان" else "Universe Metrics",
                style = MaterialTheme.typography.titleMedium,
                color = LuxuryTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LuxuryMetricCard(
                    modifier = Modifier.weight(1f),
                    title = Strings.get("chapters", language),
                    value = allChapters.size.toString(),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    glowColor = LuxuryElectricCyan
                ) { viewModel.navigateTo(AppSection.WRITER) }

                LuxuryMetricCard(
                    modifier = Modifier.weight(1f),
                    title = Strings.get("scenes", language),
                    value = allScenes.size.toString(),
                    icon = Icons.Default.EditNote,
                    glowColor = LuxuryAuroraViolet
                ) { viewModel.navigateTo(AppSection.WRITER) }

                LuxuryMetricCard(
                    modifier = Modifier.weight(1f),
                    title = Strings.get("characters_count", language),
                    value = allCharacters.size.toString(),
                    icon = Icons.Default.People,
                    glowColor = LuxuryCyberIndigo
                ) { viewModel.navigateTo(AppSection.CHARACTERS) }

                LuxuryMetricCard(
                    modifier = Modifier.weight(1f),
                    title = Strings.get("codex_count", language),
                    value = allCodex.size.toString(),
                    icon = Icons.Default.AutoStories,
                    glowColor = LuxurySubtleGold
                ) { viewModel.navigateTo(AppSection.CODEX) }
            }
        }

        // Temporal Consistency Warning Alert Card
        item {
            val isWarning = warnings.isNotEmpty()
            LuxuryGlassCard(
                glowColor = if (isWarning) LuxuryWarning else LuxurySuccess,
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
                    Surface(
                        color = (if (isWarning) LuxuryWarning else LuxurySuccess).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = "Temporal Status",
                                tint = if (isWarning) LuxuryWarning else LuxurySuccess,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isWarning)
                                "${Strings.get("warnings_count", language)} (${warnings.size})"
                            else
                                (if (language == AppLanguage.ARABIC) "الاتساق الروائي والزمني مكتمل" else "TEMPORAL CONTINUITY VERIFIED"),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isWarning) LuxuryWarning else LuxurySuccess,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isWarning)
                                "${warnings.count { it.severity == WarningSeverity.CRITICAL }} critical paradoxes detected."
                            else
                                Strings.get("no_warnings", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxuryTextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Inspect Warnings",
                        tint = LuxuryTextMuted
                    )
                }
            }
        }

        // Quick Action: Open Writer
        item {
            LuxuryGradientButton(
                text = Strings.get("open_writer", language),
                icon = Icons.Default.EditNote,
                onClick = { viewModel.navigateTo(AppSection.WRITER) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("dashboard_open_writer_btn")
            )
        }

        // Active Temporal Warnings List Preview
        if (warnings.isNotEmpty()) {
            item {
                Text(
                    text = Strings.get("warnings_count", language),
                    style = MaterialTheme.typography.titleMedium,
                    color = LuxuryTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            items(warnings.take(3)) { warning ->
                LuxuryGlassCard(
                    glowColor = if (warning.severity == WarningSeverity.CRITICAL) LuxuryWarning else LuxuryAuroraViolet,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (warning.severity == WarningSeverity.CRITICAL) LuxuryWarning.copy(alpha = 0.2f) else LuxuryAuroraViolet.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = warning.severity.name,
                                    color = if (warning.severity == WarningSeverity.CRITICAL) LuxuryWarning else LuxuryAuroraViolet,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = warning.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LuxuryTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = warning.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxuryTextSecondary
                        )
                    }
                }
            }
        }

        // Quick Navigation Launchpad
        item {
            Text(
                text = if (language == AppLanguage.ARABIC) "مساحات العمل الإضافية" else "Creative Workspaces",
                style = MaterialTheme.typography.titleMedium,
                color = LuxuryTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LuxuryWorkspaceTile(
                    title = Strings.get("nav_characters", language),
                    subtitle = if (language == AppLanguage.ARABIC) "إدارة الشخصيات والعلاقات والأقواس الدرامية" else "Manage character profiles, relationships & arcs",
                    icon = Icons.Default.People,
                    color = LuxuryCyberIndigo
                ) { viewModel.navigateTo(AppSection.CHARACTERS) }

                LuxuryWorkspaceTile(
                    title = Strings.get("nav_codex", language),
                    subtitle = if (language == AppLanguage.ARABIC) "توثيق التكنولوجيا والأساطير والمواقع والمصطلحات" else "Document technology, lore, magic systems & locations",
                    icon = Icons.Default.AutoStories,
                    color = LuxurySubtleGold
                ) { viewModel.navigateTo(AppSection.CODEX) }

                LuxuryWorkspaceTile(
                    title = Strings.get("nav_graphs", language),
                    subtitle = if (language == AppLanguage.ARABIC) "الشبكة السببية التفاعلية للأسباب والنتائج" else "Interactive node canvas for cause/effect & character bonds",
                    icon = Icons.Default.Hub,
                    color = LuxuryAuroraViolet
                ) { viewModel.navigateTo(AppSection.GRAPHS) }

                LuxuryWorkspaceTile(
                    title = Strings.get("nav_settings", language),
                    subtitle = if (language == AppLanguage.ARABIC) "تصدير الرواية، النسخ الاحتياطي، وإعدادات اللغة" else "Publishing export, backups, and app language",
                    icon = Icons.Default.Settings,
                    color = LuxuryElectricCyan
                ) { viewModel.navigateTo(AppSection.SETTINGS) }
            }
        }
    }
}

@Composable
fun LuxuryMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    glowColor: Color,
    onClick: () -> Unit
) {
    LuxuryGlassCard(
        glowColor = glowColor,
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
            Surface(
                color = glowColor.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = glowColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = LuxuryTextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = LuxuryTextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun LuxuryWorkspaceTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    LuxuryGlassCard(
        glowColor = color,
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
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LuxuryTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxuryTextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = LuxuryTextMuted
            )
        }
    }
}
