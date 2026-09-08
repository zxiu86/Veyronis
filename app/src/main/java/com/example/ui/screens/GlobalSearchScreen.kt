package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppLanguage
import com.example.ui.AppSection
import com.example.ui.Strings
import com.example.ui.VeyronisViewModel
import com.example.ui.theme.*

enum class SearchCategoryFilter {
    ALL,
    MANUSCRIPT,
    CHARACTERS,
    CODEX,
    EVENTS,
    RULES
}

data class SearchResultItem(
    val title: String,
    val subtitle: String,
    val type: String,
    val typeColor: Color,
    val targetSection: AppSection,
    val onAction: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    viewModel: VeyronisViewModel,
    modifier: Modifier = Modifier
) {
    val allScenes by viewModel.allScenes.collectAsStateWithLifecycle()
    val allCharacters by viewModel.allCharacters.collectAsStateWithLifecycle()
    val allCodex by viewModel.allCodexEntries.collectAsStateWithLifecycle()
    val allEvents by viewModel.allStoryEvents.collectAsStateWithLifecycle()
    val allRules by viewModel.allWorldRules.collectAsStateWithLifecycle()
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchCategoryFilter.ALL) }

    val results: List<SearchResultItem> = remember(
        searchQuery, selectedFilter, allScenes, allCharacters, allCodex, allEvents, allRules, language
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
                            title = scene.title,
                            subtitle = snippet,
                            type = if (language == AppLanguage.ARABIC) "مشهد" else "Scene",
                            typeColor = VeyronisPrimary,
                            targetSection = AppSection.WRITER,
                            onAction = {
                                viewModel.selectScene(scene)
                                viewModel.navigateTo(AppSection.WRITER)
                            }
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
                    char.abilities.contains(searchQuery, ignoreCase = true)
                ) {
                    list.add(
                        SearchResultItem(
                            title = char.name,
                            subtitle = "${if (language == AppLanguage.ARABIC) "الحالة" else "Status"}: ${char.currentStatus} • ${char.origin}",
                            type = if (language == AppLanguage.ARABIC) "شخصية" else "Character",
                            typeColor = VeyronisSecondary,
                            targetSection = AppSection.CHARACTERS,
                            onAction = { viewModel.navigateTo(AppSection.CHARACTERS) }
                        )
                    )
                }
            }
        }

        // 3. Codex
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.CODEX) {
            for (entry in allCodex) {
                if (entry.title.contains(searchQuery, ignoreCase = true) ||
                    entry.summary.contains(searchQuery, ignoreCase = true) ||
                    entry.description.contains(searchQuery, ignoreCase = true)
                ) {
                    list.add(
                        SearchResultItem(
                            title = entry.title,
                            subtitle = "[${entry.category}] ${entry.summary}",
                            type = if (language == AppLanguage.ARABIC) "موسوعة" else "Codex",
                            typeColor = VeyronisTertiary,
                            targetSection = AppSection.CODEX,
                            onAction = { viewModel.navigateTo(AppSection.CODEX) }
                        )
                    )
                }
            }
        }

        // 4. Events
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.EVENTS) {
            for (event in allEvents) {
                if (event.title.contains(searchQuery, ignoreCase = true) ||
                    event.summary.contains(searchQuery, ignoreCase = true) ||
                    event.locationNames.contains(searchQuery, ignoreCase = true)
                ) {
                    list.add(
                        SearchResultItem(
                            title = event.title,
                            subtitle = "${if (language == AppLanguage.ARABIC) "كوني" else "Cosmic"}: ${event.cosmicTimestamp} • ${event.summary}",
                            type = if (language == AppLanguage.ARABIC) "حدث" else "Event",
                            typeColor = Color(0xFFE879F9),
                            targetSection = AppSection.EVENTS,
                            onAction = { viewModel.navigateTo(AppSection.EVENTS) }
                        )
                    )
                }
            }
        }

        // 5. World Rules
        if (selectedFilter == SearchCategoryFilter.ALL || selectedFilter == SearchCategoryFilter.RULES) {
            for (rule in allRules) {
                if (rule.name.contains(searchQuery, ignoreCase = true) ||
                    rule.description.contains(searchQuery, ignoreCase = true)
                ) {
                    list.add(
                        SearchResultItem(
                            title = rule.name,
                            subtitle = "${rule.category} • ${rule.description}",
                            type = if (language == AppLanguage.ARABIC) "قاعدة عالم" else "World Rule",
                            typeColor = Color(0xFF34D399),
                            targetSection = AppSection.WORLD_RULES,
                            onAction = { viewModel.navigateTo(AppSection.WORLD_RULES) }
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
            .background(VeyronisBackground)
            .padding(16.dp)
    ) {
        Text(
            text = Strings.get("search_title", language),
            style = MaterialTheme.typography.headlineSmall,
            color = VeyronisTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (language == AppLanguage.ARABIC) "فهرسة شاملة للمخطوطات والشخصيات والموسوعة والأحداث وقوانين الفيزياء" else "Cross-index manuscripts, characters, codex entries, events, and physics rules",
            style = MaterialTheme.typography.bodySmall,
            color = VeyronisTextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(Strings.get("search_placeholder", language), color = VeyronisTextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VeyronisPrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = Strings.get("search_clear", language), tint = VeyronisTextSecondary)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VeyronisPrimary,
                focusedTextColor = VeyronisTextPrimary,
                unfocusedTextColor = VeyronisTextPrimary,
                unfocusedContainerColor = VeyronisPanel,
                focusedContainerColor = VeyronisPanel
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("omni_search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (filter in SearchCategoryFilter.values()) {
                val isSelected = filter == selectedFilter
                val labelText = when (filter) {
                    SearchCategoryFilter.ALL -> Strings.get("search_filter_all", language)
                    SearchCategoryFilter.MANUSCRIPT -> Strings.get("nav_writer", language)
                    SearchCategoryFilter.CHARACTERS -> Strings.get("nav_characters", language)
                    SearchCategoryFilter.CODEX -> Strings.get("nav_codex", language)
                    SearchCategoryFilter.EVENTS -> Strings.get("nav_events", language)
                    SearchCategoryFilter.RULES -> Strings.get("nav_rules", language)
                }
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(labelText) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VeyronisPrimary,
                        selectedLabelColor = VeyronisTextPrimary,
                        containerColor = VeyronisPanel,
                        labelColor = VeyronisTextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (language == AppLanguage.ARABIC) "أدخل كلمة مفتاحية أعلاه للمسح عبر جميع أرشيفات الكون." else "Enter a keyword above to scan across all universe archives.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VeyronisTextMuted
                )
            }
        } else if (results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (language == AppLanguage.ARABIC) "لا توجد نتائج مطابقة لـ '$searchQuery'." else "No results found for '$searchQuery'.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VeyronisTextMuted
                )
            }
        } else {
            Text(
                text = "${results.size} ${Strings.get("search_results_count", language)}",
                style = MaterialTheme.typography.labelMedium,
                color = VeyronisTextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VeyronisPanel),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { item.onAction() }
                            .testTag("search_result_${item.title.lowercase().replace(" ", "_")}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = item.typeColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = item.type.uppercase(),
                                            color = item.typeColor,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = VeyronisTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VeyronisTextSecondary,
                                    maxLines = 2
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Jump",
                                tint = VeyronisTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
