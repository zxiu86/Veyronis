package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.EntityType
import com.example.ui.AppLanguage
import com.example.ui.AppSection
import com.example.ui.Strings
import com.example.ui.VeyronisViewModel
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.components.UniversalEntityInspectorSheet
import com.example.ui.theme.*

enum class SearchCategoryFilter(val labelEn: String, val labelAr: String) {
    ALL("All Entities", "جميع العناصر"),
    MANUSCRIPT("Manuscript", "المخطوطات"),
    CHARACTERS("Characters", "الشخصيات"),
    LOCATIONS("Locations", "الأماكن"),
    EVENTS("Events", "الأحداث"),
    CODEX("Codex / Lore", "الموسوعة"),
    LEXICON("Lexicon", "المصطلحات"),
    RULES("World Rules", "القواعد"),
    DECISIONS("Decisions", "القرارات")
}

data class SearchResultItem(
    val entityType: EntityType,
    val entityId: Long,
    val title: String,
    val subtitle: String,
    val snippet: String,
    val typeColor: Color,
    val icon: ImageVector,
    val onNavigate: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    viewModel: VeyronisViewModel,
    modifier: Modifier = Modifier
) {
    val allScenes by viewModel.allScenes.collectAsStateWithLifecycle()
    val allCharacters by viewModel.allCharacters.collectAsStateWithLifecycle()
    val allLocations by viewModel.allLocations.collectAsStateWithLifecycle()
    val allCodex by viewModel.allCodexEntries.collectAsStateWithLifecycle()
    val allLexicon by viewModel.allLexiconTerms.collectAsStateWithLifecycle()
    val allEvents by viewModel.allStoryEvents.collectAsStateWithLifecycle()
    val allRules by viewModel.allWorldRules.collectAsStateWithLifecycle()
    val allDecisions by viewModel.allDecisions.collectAsStateWithLifecycle()
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchCategoryFilter.ALL) }

    var inspectedEntityPair by remember { mutableStateOf<Pair<EntityType, Long>?>(null) }

    val results: List<SearchResultItem> = remember(
        searchQuery, selectedFilter, allScenes, allCharacters, allLocations, allCodex, allLexicon, allEvents, allRules, allDecisions, language
    ) {
        if (searchQuery.isBlank()) return@remember emptyList()

        val list = mutableListOf<SearchResultItem>()

        // 1. Scenes
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.MANUSCRIPT) {
            for (scene in allScenes) {
                if (scene.title.contains(searchQuery, ignoreCase = true) || scene.content.contains(searchQuery, ignoreCase = true)) {
                    val snippet = if (scene.content.contains(searchQuery, ignoreCase = true)) {
                        val idx = scene.content.indexOf(searchQuery, ignoreCase = true)
                        val start = (idx - 30).coerceAtLeast(0)
                        val end = (idx + searchQuery.length + 50).coerceAtMost(scene.content.length)
                        "..." + scene.content.substring(start, end).replace("\n", " ") + "..."
                    } else (if (language == AppLanguage.ARABIC) "تطابق في العنوان" else "Title match")
                    list.add(
                        SearchResultItem(
                            entityType = EntityType.SCENE,
                            entityId = scene.id,
                            title = scene.title,
                            subtitle = "${scene.wordCount} words • Scene #${scene.orderIndex + 1}",
                            snippet = snippet,
                            typeColor = LuxuryPrimary,
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            onNavigate = { viewModel.navigateToEntity(EntityType.SCENE, scene.id) }
                        )
                    )
                }
            }
        }

        // 2. Characters
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.CHARACTERS) {
            for (char in allCharacters) {
                if (char.name.contains(searchQuery, ignoreCase = true) ||
                    char.aliases.contains(searchQuery, ignoreCase = true) ||
                    char.abilities.contains(searchQuery, ignoreCase = true) ||
                    char.personality.contains(searchQuery, ignoreCase = true)
                ) {
                    list.add(
                        SearchResultItem(
                            entityType = EntityType.CHARACTER,
                            entityId = char.id,
                            title = char.name,
                            subtitle = "${if (language == AppLanguage.ARABIC) "الحالة" else "Status"}: ${char.currentStatus} • ${char.origin}",
                            snippet = char.personality.ifBlank { char.appearance }.take(100),
                            typeColor = Color(0xFF38BDF8),
                            icon = Icons.Default.Person,
                            onNavigate = { viewModel.navigateToEntity(EntityType.CHARACTER, char.id) }
                        )
                    )
                }
            }
        }

        // 3. Locations
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.LOCATIONS) {
            for (loc in allLocations) {
                if (loc.name.contains(searchQuery, ignoreCase = true) ||
                    loc.aliases.contains(searchQuery, ignoreCase = true) ||
                    loc.description.contains(searchQuery, ignoreCase = true) ||
                    loc.realmOrWorld.contains(searchQuery, ignoreCase = true)
                ) {
                    list.add(
                        SearchResultItem(
                            entityType = EntityType.LOCATION,
                            entityId = loc.id,
                            title = loc.name,
                            subtitle = "${loc.type} • ${loc.realmOrWorld}",
                            snippet = loc.description.take(100),
                            typeColor = Color(0xFF10B981),
                            icon = Icons.Default.Place,
                            onNavigate = { viewModel.navigateToEntity(EntityType.LOCATION, loc.id) }
                        )
                    )
                }
            }
        }

        // 4. Codex
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.CODEX) {
            for (entry in allCodex) {
                if (entry.title.contains(searchQuery, ignoreCase = true) ||
                    entry.summary.contains(searchQuery, ignoreCase = true) ||
                    entry.description.contains(searchQuery, ignoreCase = true)
                ) {
                    list.add(
                        SearchResultItem(
                            entityType = EntityType.CODEX,
                            entityId = entry.id,
                            title = entry.title,
                            subtitle = "[${entry.category}] ${entry.summary}",
                            snippet = entry.description.take(100),
                            typeColor = Color(0xFFF59E0B),
                            icon = Icons.Default.AutoStories,
                            onNavigate = { viewModel.navigateToEntity(EntityType.CODEX, entry.id) }
                        )
                    )
                }
            }
        }

        // 5. Lexicon
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.LEXICON) {
            for (term in allLexicon) {
                if (term.term.contains(searchQuery, ignoreCase = true) ||
                    term.shortDefinition.contains(searchQuery, ignoreCase = true) ||
                    term.category.contains(searchQuery, ignoreCase = true)
                ) {
                    list.add(
                        SearchResultItem(
                            entityType = EntityType.LEXICON,
                            entityId = term.id,
                            title = term.term,
                            subtitle = "[${term.category}] ${term.shortDefinition}",
                            snippet = term.shortDefinition.take(100),
                            typeColor = Color(0xFF06B6D4),
                            icon = Icons.Default.Translate,
                            onNavigate = { viewModel.navigateToEntity(EntityType.LEXICON, term.id) }
                        )
                    )
                }
            }
        }

        // 6. Events
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.EVENTS) {
            for (event in allEvents) {
                if (event.title.contains(searchQuery, ignoreCase = true) ||
                    event.summary.contains(searchQuery, ignoreCase = true) ||
                    event.locationNames.contains(searchQuery, ignoreCase = true)
                ) {
                    list.add(
                        SearchResultItem(
                            entityType = EntityType.EVENT,
                            entityId = event.id,
                            title = event.title,
                            subtitle = "${if (language == AppLanguage.ARABIC) "كوني" else "Cosmic"}: ${event.cosmicTimestamp} • ${event.summary}",
                            snippet = event.locationNames.ifBlank { event.causes }.take(100),
                            typeColor = Color(0xFF6366F1),
                            icon = Icons.Default.Event,
                            onNavigate = { viewModel.navigateToEntity(EntityType.EVENT, event.id) }
                        )
                    )
                }
            }
        }

        // 7. World Rules
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.RULES) {
            for (rule in allRules) {
                if (rule.name.contains(searchQuery, ignoreCase = true) ||
                    rule.description.contains(searchQuery, ignoreCase = true) ||
                    rule.category.contains(searchQuery, ignoreCase = true)
                ) {
                    list.add(
                        SearchResultItem(
                            entityType = EntityType.WORLD_RULE,
                            entityId = rule.id,
                            title = rule.name,
                            subtitle = "[${rule.category}] ${rule.description}",
                            snippet = rule.notes.ifBlank { rule.energyType }.take(100),
                            typeColor = Color(0xFF8B5CF6),
                            icon = Icons.Default.Gavel,
                            onNavigate = { viewModel.navigateToEntity(EntityType.WORLD_RULE, rule.id) }
                        )
                    )
                }
            }
        }

        // 8. Decisions
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.DECISIONS) {
            for (dec in allDecisions) {
                if (dec.title.contains(searchQuery, ignoreCase = true) ||
                    dec.description.contains(searchQuery, ignoreCase = true) ||
                    dec.motivation.contains(searchQuery, ignoreCase = true)
                ) {
                    list.add(
                        SearchResultItem(
                            entityType = EntityType.DECISION,
                            entityId = dec.id,
                            title = dec.title,
                            subtitle = "Impact: ${dec.impactLevel} • ${dec.motivation}",
                            snippet = dec.consequenceSummary.take(100),
                            typeColor = Color(0xFFEF4444),
                            icon = Icons.Default.Psychology,
                            onNavigate = { viewModel.navigateToEntity(EntityType.DECISION, dec.id) }
                        )
                    )
                }
            }
        }

        list
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("global_search_input"),
            placeholder = {
                Text(
                    text = if (language == AppLanguage.ARABIC) "ابحث في كل عناصر الكون (شخصيات، أماكن، مخطوطات، أحداث، لور)..." else "Search across universe (Characters, Locations, Lore, Events, Scenes)...",
                    color = LuxuryTextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = LuxuryPrimary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = LuxuryTextSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LuxurySurfaceElevated,
                unfocusedContainerColor = LuxurySurfaceElevated,
                focusedBorderColor = LuxuryPrimary,
                unfocusedBorderColor = LuxurySurfaceHighlight
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (filter in SearchCategoryFilter.entries) {
                val isSelected = selectedFilter == filter
                Surface(
                    color = if (isSelected) LuxuryPrimaryContainer else LuxurySurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isSelected) LuxuryPrimary else LuxurySurfaceHighlight),
                    modifier = Modifier
                        .clickable { selectedFilter = filter }
                        .testTag("search_filter_${filter.name.lowercase()}")
                ) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) filter.labelAr else filter.labelEn,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) LuxuryPrimary else LuxuryTextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results or Empty State
        if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.TravelExplore,
                        contentDescription = null,
                        tint = LuxuryTextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (language == AppLanguage.ARABIC) "محرك البحث والربط الكوني" else "Universal Cross-System Search",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LuxuryTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (language == AppLanguage.ARABIC) "اكتب أي كلمة للوصول الفوري لكل المخطوطات والشخصيات والأماكن والأحداث المترابطة." else "Type any keyword to instantly locate interconnected entities across the universe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxuryTextSecondary,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else if (results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (language == AppLanguage.ARABIC) "لم يتم العثور على نتائج مطابقة لـ \"$searchQuery\"" else "No matching entities found for \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LuxuryTextMuted
                )
            }
        } else {
            Text(
                text = "${results.size} ${if (language == AppLanguage.ARABIC) "عنصر مطابق" else "matches found"}",
                style = MaterialTheme.typography.labelMedium,
                color = LuxuryTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results) { item ->
                    LuxuryGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { item.onNavigate() }
                            .testTag("search_result_${item.entityType.name.lowercase()}_${item.entityId}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = item.typeColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, item.typeColor.copy(alpha = 0.4f)),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = item.typeColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LuxuryTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = item.typeColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.ARABIC) item.entityType.labelAr else item.entityType.labelEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = item.typeColor,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LuxuryTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (item.snippet.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.snippet,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LuxuryTextMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { inspectedEntityPair = Pair(item.entityType, item.entityId) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Inspect",
                                    tint = LuxuryTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    inspectedEntityPair?.let { (type, id) ->
        UniversalEntityInspectorSheet(
            viewModel = viewModel,
            entityType = type,
            entityId = id,
            onDismiss = { inspectedEntityPair = null }
        )
    }
}
