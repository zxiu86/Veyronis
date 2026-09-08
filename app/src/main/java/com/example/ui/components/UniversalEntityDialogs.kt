package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.domain.DeletionImpactSummary
import com.example.ui.AppLanguage
import com.example.ui.AppSection
import com.example.ui.Strings
import com.example.ui.VeyronisViewModel
import com.example.ui.theme.*

/**
 * Universal Inspector Bottom Sheet: Deep 360-degree inspection for any entity in the Veyronis universe.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalEntityInspectorSheet(
    viewModel: VeyronisViewModel,
    entityType: EntityType,
    entityId: Long,
    onDismiss: () -> Unit
) {
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()
    val allCharacters by viewModel.allCharacters.collectAsStateWithLifecycle()
    val allLocations by viewModel.allLocations.collectAsStateWithLifecycle()
    val allEvents by viewModel.allStoryEvents.collectAsStateWithLifecycle()
    val allScenes by viewModel.allScenes.collectAsStateWithLifecycle()
    val allCodex by viewModel.allCodexEntries.collectAsStateWithLifecycle()
    val allLexicon by viewModel.allLexiconTerms.collectAsStateWithLifecycle()
    val allRules by viewModel.allWorldRules.collectAsStateWithLifecycle()
    val allRelationships by viewModel.allUniversalRelationships.collectAsStateWithLifecycle()
    val allDecisions by viewModel.allDecisions.collectAsStateWithLifecycle()
    val allCausalEdges by viewModel.allCausalEdges.collectAsStateWithLifecycle()
    val characterRelationships by viewModel.allRelationships.collectAsStateWithLifecycle()

    var showAddRelationDialog by remember { mutableStateOf(false) }
    var showDeleteImpactDialog by remember { mutableStateOf(false) }

    // Resolve entity details
    val (title, subtitle, colorHex, icon) = remember(entityType, entityId, allCharacters, allLocations, allEvents, allScenes, allCodex, allLexicon, allRules) {
        when (entityType) {
            EntityType.CHARACTER -> {
                val c = allCharacters.find { it.id == entityId }
                Tuple4(c?.name ?: "Character", c?.currentStatus ?: "Alive", c?.primaryColorHex ?: "#38BDF8", Icons.Default.Person)
            }
            EntityType.LOCATION -> {
                val l = allLocations.find { it.id == entityId }
                Tuple4(l?.name ?: "Location", "${l?.type} • ${l?.realmOrWorld}", "#10B981", Icons.Default.Place)
            }
            EntityType.EVENT -> {
                val e = allEvents.find { it.id == entityId }
                Tuple4(e?.title ?: "Event", "Epoch ${e?.cosmicTimestamp}", "#6366F1", Icons.Default.Event)
            }
            EntityType.SCENE -> {
                val s = allScenes.find { it.id == entityId }
                Tuple4(s?.title ?: "Scene", "${s?.wordCount ?: 0} words", "#EC4899", Icons.AutoMirrored.Filled.MenuBook)
            }
            EntityType.CODEX -> {
                val c = allCodex.find { it.id == entityId }
                Tuple4(c?.title ?: "Lore", c?.category ?: "Lore", "#F59E0B", Icons.Default.AutoStories)
            }
            EntityType.LEXICON -> {
                val lx = allLexicon.find { it.id == entityId }
                Tuple4(lx?.term ?: "Term", lx?.category ?: "Term", "#06B6D4", Icons.Default.Translate)
            }
            EntityType.WORLD_RULE -> {
                val r = allRules.find { it.id == entityId }
                Tuple4(r?.name ?: "Rule", r?.category ?: "Physics", "#8B5CF6", Icons.Default.Gavel)
            }
            EntityType.DECISION -> {
                val d = allDecisions.find { it.id == entityId }
                Tuple4(d?.title ?: "Decision", "Impact: ${d?.impactLevel}", "#EF4444", Icons.Default.Psychology)
            }
            else -> Tuple4("Entity #$entityId", entityType.name, "#64748B", Icons.Default.Extension)
        }
    }

    // Connected universal relationships
    val directRels = remember(allRelationships, entityType, entityId) {
        allRelationships.filter {
            (it.sourceType == entityType.name && it.sourceId == entityId) ||
            (it.targetType == entityType.name && it.targetId == entityId)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = LuxuryVoidBackground.copy(alpha = 0.96f),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Surface(
                color = LuxuryPrimary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(48.dp)
                    .height(5.dp)
            ) {}
        }
    ) {
        val themeColor = remember(colorHex) {
            try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { LuxuryPrimary }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 6.dp)
                .navigationBarsPadding()
        ) {
            // Header with glowing icon container
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = themeColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.2.dp, themeColor.copy(alpha = 0.55f)),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = icon, contentDescription = null, tint = themeColor, modifier = Modifier.size(26.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = LuxuryTextPrimary,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = themeColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, themeColor.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) entityType.labelAr else entityType.labelEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = themeColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = LuxuryTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Jump to Tool Action Button with ripple and glowing badge
                IconButton(
                    onClick = {
                        viewModel.navigateToEntity(entityType, entityId)
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(LuxuryPrimaryContainer)
                        .border(1.dp, LuxuryPrimary.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open in Tool",
                        tint = LuxuryPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = LuxurySurfaceHighlight)
            Spacer(modifier = Modifier.height(14.dp))

            // Action Pills (Add Connection, Delete Impact)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LuxurySecondaryButton(
                    text = if (language == AppLanguage.ARABIC) "+ إضافة ربط" else "+ Add Link",
                    icon = Icons.Default.AddLink,
                    onClick = { showAddRelationDialog = true },
                    modifier = Modifier.weight(1f)
                )
                LuxurySecondaryButton(
                    text = if (language == AppLanguage.ARABIC) "فحص التأثير" else "Impact Analysis",
                    icon = Icons.Default.Shield,
                    onClick = { showDeleteImpactDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connected Relationships Section
            Text(
                text = if (language == AppLanguage.ARABIC) "الروابط الكونية المتصلة (${directRels.size})" else "Connected Universe Links (${directRels.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LuxuryTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (directRels.isEmpty()) {
                Surface(
                    color = LuxurySurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LuxurySurfaceHighlight),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "لا توجد روابط مسجلة بعد. استخدم 'إضافة ربط' لتوصيل هذا العنصر بالنظام." else "No connections recorded yet. Tap '+ Add Link' to connect this entity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxuryTextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(directRels) { rel ->
                        val isTarget = rel.sourceType == entityType.name && rel.sourceId == entityId
                        val otherType = if (isTarget) rel.targetType else rel.sourceType
                        val otherId = if (isTarget) rel.targetId else rel.sourceId

                        Surface(
                            color = LuxurySurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (rel.status == "Suggested") LuxuryWarning.copy(alpha = 0.4f) else LuxurySurfaceHighlight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = rel.relationType,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = LuxuryPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = if (rel.status == "Suggested") LuxuryWarning.copy(alpha = 0.2f) else LuxurySuccess.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = rel.status,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (rel.status == "Suggested") LuxuryWarning else LuxurySuccess,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "$otherType #$otherId ${if (rel.notes.isNotBlank()) "• " + rel.notes else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LuxuryTextSecondary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (rel.status == "Suggested") {
                                        IconButton(onClick = { viewModel.confirmUniversalRelationship(rel.id) }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Check, contentDescription = "Confirm", tint = LuxurySuccess, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteUniversalRelationship(rel) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = LuxuryWarning, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showAddRelationDialog) {
        AddUniversalRelationshipDialog(
            viewModel = viewModel,
            sourceType = entityType,
            sourceId = entityId,
            onDismiss = { showAddRelationDialog = false }
        )
    }

    if (showDeleteImpactDialog) {
        val impact = remember(entityType, entityId) {
            viewModel.centralEntityEngine.analyzeDeletionImpact(
                entityType = entityType,
                entityId = entityId,
                entityTitle = title,
                scenes = allScenes,
                events = allEvents,
                decisions = allDecisions,
                relationships = allRelationships,
                causalEdges = allCausalEdges,
                characters = allCharacters
            )
        }
        DeletionImpactDialog(
            impact = impact,
            language = language,
            onDismiss = { showDeleteImpactDialog = false }
        )
    }
}

/**
 * Universal Dialog for manually creating a link between entities.
 */
/**
 * Universal Dialog for manually creating a link between entities.
 */
@Composable
fun AddUniversalRelationshipDialog(
    viewModel: VeyronisViewModel,
    sourceType: EntityType,
    sourceId: Long,
    onDismiss: () -> Unit
) {
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()
    val allCharacters by viewModel.allCharacters.collectAsStateWithLifecycle()
    val allLocations by viewModel.allLocations.collectAsStateWithLifecycle()
    val allEvents by viewModel.allStoryEvents.collectAsStateWithLifecycle()
    val allScenes by viewModel.allScenes.collectAsStateWithLifecycle()
    val allCodex by viewModel.allCodexEntries.collectAsStateWithLifecycle()
    val allLexicon by viewModel.allLexiconTerms.collectAsStateWithLifecycle()
    val allRules by viewModel.allWorldRules.collectAsStateWithLifecycle()
    val allDecisions by viewModel.allDecisions.collectAsStateWithLifecycle()

    var selectedTargetType by remember { mutableStateOf(EntityType.CHARACTER) }
    var selectedTargetId by remember { mutableStateOf(0L) }
    var relationType by remember { mutableStateOf("RelatesTo") }
    var notes by remember { mutableStateOf("") }
    var intensity by remember { mutableIntStateOf(3) }

    // Available target items based on type
    val targetItems = remember(selectedTargetType, allCharacters, allLocations, allEvents, allScenes, allCodex, allLexicon, allRules, allDecisions) {
        when (selectedTargetType) {
            EntityType.CHARACTER -> allCharacters.map { Pair(it.id, it.name) }
            EntityType.LOCATION -> allLocations.map { Pair(it.id, "${it.name} (${it.realmOrWorld})") }
            EntityType.EVENT -> allEvents.map { Pair(it.id, it.title) }
            EntityType.SCENE -> allScenes.map { Pair(it.id, it.title) }
            EntityType.CODEX -> allCodex.map { Pair(it.id, "${it.title} [${it.category}]") }
            EntityType.LEXICON -> allLexicon.map { Pair(it.id, it.term) }
            EntityType.WORLD_RULE -> allRules.map { Pair(it.id, it.name) }
            EntityType.DECISION -> allDecisions.map { Pair(it.id, it.title) }
            else -> emptyList()
        }
    }

    LaunchedEffect(targetItems) {
        if (targetItems.isNotEmpty() && (selectedTargetId == 0L || targetItems.none { it.first == selectedTargetId })) {
            selectedTargetId = targetItems.first().first
        }
    }

    LuxuryDialog(
        onDismissRequest = onDismiss,
        title = if (language == AppLanguage.ARABIC) "ربط عنصر بالمنظومة الكونية" else "Create Universal Relationship",
        subtitle = "${if (language == AppLanguage.ARABIC) "المصدر" else "Source"}: ${sourceType.name} #$sourceId",
        icon = Icons.Default.AddLink,
        iconColor = LuxuryElectricCyan,
        actionButtons = {
            TextButton(onClick = onDismiss) {
                Text(if (language == AppLanguage.ARABIC) "إلغاء" else "Cancel", color = LuxuryTextSecondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            LuxuryGradientButton(
                text = if (language == AppLanguage.ARABIC) "تثبيت الرابط 🔗" else "Confirm Link 🔗",
                onClick = {
                    if (selectedTargetId != 0L) {
                        viewModel.addUniversalRelationship(
                            sourceType = sourceType,
                            sourceId = sourceId,
                            targetType = selectedTargetType,
                            targetId = selectedTargetId,
                            relationType = relationType.ifBlank { "RelatesTo" },
                            notes = notes,
                            intensity = intensity
                        )
                        onDismiss()
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Target Type Selector
            Text(
                text = if (language == AppLanguage.ARABIC) "نوع العنصر الهدف:" else "Target Entity Type:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = LuxuryTextSecondary
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val supported = listOf(
                    EntityType.CHARACTER, EntityType.LOCATION, EntityType.EVENT,
                    EntityType.SCENE, EntityType.CODEX, EntityType.LEXICON,
                    EntityType.WORLD_RULE, EntityType.DECISION
                )
                items(supported) { type ->
                    val isSel = selectedTargetType == type
                    Surface(
                        color = if (isSel) LuxuryPrimaryContainer else LuxurySurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.2.dp, if (isSel) LuxuryPrimary else LuxurySurfaceHighlight),
                        modifier = Modifier.clickable {
                            selectedTargetType = type
                            selectedTargetId = 0L
                        }
                    ) {
                        Text(
                            text = if (language == AppLanguage.ARABIC) type.labelAr else type.labelEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSel) LuxuryPrimary else LuxuryTextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Target Item Selection List
            Text(
                text = if (language == AppLanguage.ARABIC) "اختر العنصر الهدف:" else "Select Target Entity:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = LuxuryTextSecondary
            )

            if (targetItems.isEmpty()) {
                Text(
                    text = if (language == AppLanguage.ARABIC) "لا توجد عناصر متاحة من هذا النوع." else "No entities available in this category.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LuxuryTextMuted
                )
            } else {
                Surface(
                    color = LuxurySurfaceElevated.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LuxurySurfaceHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(6.dp)
                    ) {
                        for ((id, label) in targetItems) {
                            val isSel = selectedTargetId == id
                            Surface(
                                color = if (isSel) LuxuryPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                                border = if (isSel) BorderStroke(1.dp, LuxuryPrimary.copy(alpha = 0.4f)) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTargetId = id }
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSel,
                                        onClick = { selectedTargetId = id },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = LuxuryPrimary,
                                            unselectedColor = LuxuryTextMuted
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSel) LuxuryTextPrimary else LuxuryTextSecondary,
                                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Relationship Type Picker
            val commonRelationTypes = listOf(
                "AppearsIn", "LocatedAt", "Mentions", "Causes", "ConsequenceOf",
                "Friend", "Enemy", "Mentor", "Rival", "GovernedBy", "RelatesTo",
                "Participant", "Violates", "CreatedIn"
            )
            Text(
                text = if (language == AppLanguage.ARABIC) "طبيعة العلاقة:" else "Relationship Nature:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = LuxuryTextSecondary
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(commonRelationTypes) { rel ->
                    val isSel = relationType == rel
                    Surface(
                        color = if (isSel) LuxurySecondaryContainer else LuxurySurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isSel) LuxurySecondary else LuxurySurfaceHighlight),
                        modifier = Modifier.clickable { relationType = rel }
                    ) {
                        Text(
                            text = rel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSel) LuxurySecondary else LuxuryTextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(if (language == AppLanguage.ARABIC) "ملاحظات سياقية (اختياري)" else "Contextual Notes (Optional)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LuxuryTextPrimary,
                    unfocusedTextColor = LuxuryTextPrimary,
                    focusedBorderColor = LuxuryPrimary,
                    unfocusedBorderColor = LuxurySurfaceHighlight,
                    focusedContainerColor = LuxurySurfaceElevated,
                    unfocusedContainerColor = LuxurySurfaceElevated
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Deletion Impact Warning Dialog showing downstream effects on scenes, events, decisions, and causal edges.
 */
@Composable
fun DeletionImpactDialog(
    impact: DeletionImpactSummary,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    LuxuryDialog(
        onDismissRequest = onDismiss,
        title = if (language == AppLanguage.ARABIC) "تحليل التأثير الكوني" else "Universe Impact Analysis",
        subtitle = impact.entityTitle,
        icon = Icons.Default.Warning,
        iconColor = LuxuryWarning,
        actionButtons = {
            LuxuryGradientButton(
                text = if (language == AppLanguage.ARABIC) "حسناً، فهمت" else "Understood",
                onClick = onDismiss
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Text(
                text = if (language == AppLanguage.ARABIC)
                    "تقرير الارتباطات والتبعيات المتأثرة في حال تعديل أو حذف '${impact.entityTitle}':"
                else "Downstream dependencies affected if modifying '${impact.entityTitle}':",
                style = MaterialTheme.typography.bodyMedium,
                color = LuxuryTextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ImpactStatBadge(title = if (language == AppLanguage.ARABIC) "مشاهد" else "Scenes", count = impact.affectedScenesCount, modifier = Modifier.weight(1f))
                ImpactStatBadge(title = if (language == AppLanguage.ARABIC) "أحداث" else "Events", count = impact.affectedEventsCount, modifier = Modifier.weight(1f))
                ImpactStatBadge(title = if (language == AppLanguage.ARABIC) "قرارات" else "Decisions", count = impact.affectedDecisionsCount, modifier = Modifier.weight(1f))
                ImpactStatBadge(title = if (language == AppLanguage.ARABIC) "روابط" else "Links", count = impact.affectedRelationshipsCount, modifier = Modifier.weight(1f))
            }

            if (impact.warningMessages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (language == AppLanguage.ARABIC) "تنبيهات الاتساق المترتبة:" else "Continuity Warnings:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = LuxuryWarning
                )
                Spacer(modifier = Modifier.height(8.dp))
                for (msg in impact.warningMessages) {
                    Surface(
                        color = LuxuryWarning.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, LuxuryWarning.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    ) {
                        Text(
                            text = "• $msg",
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxuryTextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImpactStatBadge(title: String, count: Int, modifier: Modifier = Modifier) {
    Surface(
        color = LuxurySurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (count > 0) LuxuryWarning.copy(alpha = 0.4f) else LuxurySurfaceHighlight),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (count > 0) LuxuryWarning else LuxuryTextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = LuxuryTextSecondary
            )
        }
    }
}
